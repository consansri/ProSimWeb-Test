package prosim.uilib.styled.editor

import emulator.kit.assembler.CodeStyle
import emulator.kit.nativeError
import emulator.kit.nativeWarn
import kotlinx.coroutines.*
import prosim.uilib.UIStates
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.border.Border

class CEditorArea(val location: Location, val maxStackSize: Int = 30, var stackQueryMillis: Long = 500) : JComponent() {

    // Current State
    private val styledText: MutableList<StyledChar> = mutableListOf()
    private val lineBreakIDs: MutableList<Int> = mutableListOf()

    // Scopes
    private val selScope = CoroutineScope(Dispatchers.Main)
    private val historyScope = CoroutineScope(Dispatchers.Default)
    private val lineNumberScope = CoroutineScope(Dispatchers.Default)
    private val highlighterScope = CoroutineScope(Dispatchers.Default)

    // Jobs
    private var debounceJob: Job? = null
    private val debounceJobInterval: Long = 1500
    private var caretMovesJob: Job? = null

    // Interactable Components
    var fileInterface: FileInterface? = null
        set(value) {
            field = value
            styledText.clear()
            caret.resetPos()
            resetSelection()
            value?.let {
                insertText(0, value.getRawContent())
            }
            revalidate()
            repaint()
        }

    var infoLogger: InfoLogger? = null
    var highlighter: Highlighter? = null
    var shortCuts: ShortCuts? = null
    val scrollPane: CScrollPane = CScrollPane(this, true)
    val lineNumbers: CEditorLineNumbers = CEditorLineNumbers(this)
    val findAndReplace: CEditorAnalyzer = CEditorAnalyzer(this)

    // State History
    private val textStateHistory = Stack<List<StyledChar>>()
    private val undoneTextStateHistory = Stack<List<StyledChar>>()
    private var lastSave: Long = 0

    // Caret
    val caret = Caret()

    // Selection
    private var selStart: Int = -1
        set(value) {
            field = value
            selScope.launch {
                if (value >= 0) {
                    val codePosition = getAdvancedPosition(value)
                    selStartLine = codePosition.line
                    selStartColumn = codePosition.column
                } else {
                    selStartLine = -1
                    selStartColumn = -1
                }
            }
        }

    private var selEnd: Int = -1
        set(value) {
            field = value
            selScope.launch {
                if (value >= 0) {
                    val codePosition = getAdvancedPosition(value)
                    selEndLine = codePosition.line
                    selEndColumn = codePosition.column
                } else {
                    selEndLine = -1
                    selEndColumn = -1
                }
            }
        }

    private var selStartLine = -1
    private var selStartColumn = -1
    private var selEndLine = -1
    private var selEndColumn = -1

    // Settings
    val tabSize: Int = 4

    var scrollMarginLines = 2
    var scrollMarginChars = 10
    var isEditable = true

    /**
     * Initialization
     */

    init {
        setUI(CEditorAreaUI())
        addKeyListener(EditorKeyListener())
        addMouseListener(EditorMouseListener())
        addMouseMotionListener(EditorMouseDragListener())
    }

    /**
     * Text State Manipulation
     */

    fun undo() {
        if (textStateHistory.isNotEmpty()) {
            val currentState = styledText.toList()
            undoneTextStateHistory.push(currentState)
            val previousState = textStateHistory.pop()
            styledText.clear()
            styledText.addAll(previousState)
            if (caret.getIndex() > previousState.size) {
                caret.moveCaretTo(previousState.size)
            }
            contentChanged()
            resetSelection()
            debounceHighlighting()
            revalidate()
            repaint()
        }
    }

    fun redo() {
        if (undoneTextStateHistory.isNotEmpty()) {
            val currentState = styledText.toList()
            textStateHistory.push(currentState)
            val nextState = undoneTextStateHistory.pop()
            styledText.clear()
            styledText.addAll(nextState)
            if (caret.getIndex() > nextState.size) {
                caret.moveCaretTo(nextState.size)
            }
            contentChanged()
            resetSelection()
            debounceHighlighting()
            revalidate()
            repaint()
        }
    }

    fun queryStateChange() {
        historyScope.launch {
            if (textStateHistory.size > 1) {
                // POP Last State if it is younger than stackQueryInterval
                val currStateTime = System.currentTimeMillis()
                val timeDiff = currStateTime - lastSave
                if (timeDiff <= stackQueryMillis) {
                    textStateHistory.pop()
                }
            }

            val currentState = styledText.toList()
            textStateHistory.push(currentState)
            lastSave = System.currentTimeMillis()

            if (textStateHistory.size > maxStackSize) {
                val excees = textStateHistory.size - maxStackSize
                repeat(excees) {
                    textStateHistory.removeAt(0)
                }
            }
            undoneTextStateHistory.clear()

            debounceHighlighting()

            val currentStateAsString = currentState.joinToString("") { it.content.toString() }
            fileInterface?.contentChanged(currentStateAsString)
        }
    }

    /**
     * Text Editing
     */

    // Insertion / Deletion
    fun replaceAll(styledContent: List<StyledChar>) {
        val prevCaretPos = caret.caretPos
        styledText.clear()
        styledText.addAll(styledContent)
        caret.resetPos()
        caret.moveCaretRight(prevCaretPos)
        queryStateChange()
        contentChanged()
        resetSelection()
        revalidate()
        repaint()
    }

    fun replaceAll(content: String) {
        replaceAll(content.toStyledContent())
    }

    fun replace(range: IntRange, newText: String) {
        if (range.first !in styledText.indices || range.last !in styledText.indices) {
            nativeError("Text Replacement out of Bounds for Range $range and current size ${styledText.size}")
            return
        }

        // Remove Range
        for (index in range.reversed()) {
            styledText.removeAt(index)
        }

        // Insert newText
        styledText.addAll(range.first, newText.map { StyledChar(it) })
        queryStateChange()
        contentChanged()
        resetSelection()
        revalidate()
        repaint()
    }

    fun insertText(pos: Int, newText: String) {
        if (pos > styledText.size) {
            nativeError("Text Insertion invalid for position $pos and current size ${styledText.size}!")
            return
        }
        styledText.addAll(pos, newText.map { StyledChar(if (it == '\t') ' ' else it) })
        caret.moveCaretRight(newText.length)
        queryStateChange()
        contentChanged()
        resetSelection()
        revalidate()
        repaint()
    }

    fun deleteText(startIndex: Int, endIndex: Int) {
        if (startIndex < endIndex && endIndex <= styledText.size) {
            styledText.subList(startIndex, endIndex).clear()
            if (caret.getIndex() > startIndex) {
                caret.moveCaretLeft(endIndex - startIndex)
            }
            queryStateChange()
            contentChanged()
            resetSelection()
            revalidate()
            repaint()
        }
    }

    private fun deleteSelected() {
        if (selStart != -1 && selEnd != -1) deleteText(getAbsSelection().lowIndex, getAbsSelection().highIndex)
    }

    // Indentation
    private fun indent() {
        val absSelection = getAbsSelection()
        if (absSelection.lowIndex == absSelection.highIndex) {
            val caretLine = caret.getLineInfo()
            val caretIndex = caret.getIndex()
            val spacesToInsert = tabSize - (caretLine.columnID % tabSize)
            insertText(caretIndex, " ".repeat(spacesToInsert))
        } else {
            // Multi-line selection
            val startLine = absSelection.lowLine
            val endLine = absSelection.highLine

            var before = 0
            var inSelection = 0

            for (lineID in startLine - 1..<endLine) {
                if (lineID == startLine - 1) {
                    before += addLineIndent(lineID)
                } else {
                    inSelection += addLineIndent(lineID)
                }
            }

            if (selStart < selEnd) {
                selStart += before
                selEnd += (before + inSelection)
                caret.moveCaretRight(before + inSelection)
            } else {
                selStart += (before + inSelection)
                selEnd += before
                caret.moveCaretRight(before)
            }
            queryStateChange()
            revalidate()
            repaint()
        }
    }

    private fun removeIndent() {
        val absSelection = getAbsSelection()
        if (absSelection.lowIndex == absSelection.highIndex) {
            val caretLineInfo = caret.getLineInfo()
            caret.moveCaretLeft(removeLineIndent(caretLineInfo.lineNumber - 1))
        } else {
            // Multi-line selection
            val startLine = absSelection.lowLine
            val endLine = absSelection.highLine
            var before = 0
            var inSelection = 0
            for (lineID in startLine - 1..<endLine) {
                if (lineID == startLine - 1) {
                    before += removeLineIndent(lineID)
                } else {
                    inSelection += removeLineIndent(lineID)
                }
            }

            if (selStart < selEnd) {
                selStart -= before
                selEnd -= (before + inSelection)
                caret.moveCaretLeft(before + inSelection)
            } else {
                selStart -= (before + inSelection)
                selEnd -= before
                caret.moveCaretLeft(before)
            }
        }
        queryStateChange()
        revalidate()
        repaint()
    }

    private fun addLineIndent(lineID: Int): Int {
        val tabInsertPos = if (lineID <= 0) 0 else lineBreakIDs.getOrNull(lineID - 1)
        tabInsertPos?.let { insertNotNull ->
            if (tabInsertPos == 0) {
                styledText.addAll(insertNotNull, " ".repeat(tabSize).map { StyledChar(it) })
                contentChanged()
                return tabSize
            } else {
                if (insertNotNull < styledText.size - 1) {
                    styledText.addAll(insertNotNull + 1, " ".repeat(tabSize).map { StyledChar(it) })
                    contentChanged()
                    return tabSize
                }
            }
        }
        return 0
    }

    private fun removeLineIndent(lineID: Int): Int {
        val tabInsertPos = if (lineID <= 0) 0 else lineBreakIDs.getOrNull(lineID - 1)
        tabInsertPos?.let { insertNotNull ->
            if (tabInsertPos == 0) {
                var removed = 0
                val subList = styledText.subList(insertNotNull, styledText.size)
                while (subList.firstOrNull()?.content == ' ' && removed < tabSize) {
                    subList.removeFirst()
                    removed++
                }
                contentChanged()
                return removed
            } else {
                if (insertNotNull < styledText.size - 1) {
                    var removed = 0
                    val subList = styledText.subList(insertNotNull + 1, styledText.size)
                    while (subList.firstOrNull()?.content == ' ' && removed < tabSize) {
                        subList.removeFirst()
                        removed++
                    }
                    contentChanged()
                    return removed
                }
            }

        }
        return 0
    }

    /**
     * Text Selection
     */

    private fun handleShiftSelection(e: KeyEvent) {
        if (selStart == -1) {
            selStart = caret.getIndex()
        }

        when (e.keyCode) {
            KeyEvent.VK_LEFT -> caret.moveCaretLeft()
            KeyEvent.VK_UP -> caret.moveCaretUp()
            KeyEvent.VK_RIGHT -> caret.moveCaretRight()
            KeyEvent.VK_DOWN -> caret.moveCaretDown()
            KeyEvent.VK_HOME -> caret.moveCaretHome()
            KeyEvent.VK_END -> caret.moveCaretEnd()
        }

        selEnd = caret.getIndex()

        if (selStart == selEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun handleMouseSelection(e: MouseEvent) {
        if (selStart == -1) {
            selStart = caret.getIndex()
        }

        caret.moveCaretTo(e)

        selEnd = caret.getIndex()

        if (selStart == selEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun handleMouseDoubleClick(e: MouseEvent) {
        caret.moveCaretTo(e)
        selectSymbol()
    }

    fun select(range: IntRange) {
        selStart = range.first
        selEnd = range.last + 1
        caret.moveCaretTo(range.last + 1)
    }

    private fun selectAll() {
        selStart = 0
        selEnd = styledText.size
        caret.moveCaretTo(styledText.size)
        repaint()
    }

    private fun getSymbolBounds(index: Int): Pair<Int, Int>? {
        if (index < 0 || index >= styledText.size) return null

        // Find the start index of the word
        var startIndex = index
        while (startIndex > 0 && styledText[startIndex - 1].isSymbolChar()) {
            startIndex--
        }

        // Find the end index of the word
        var endIndex = index
        while (endIndex < styledText.size - 1 && styledText[endIndex].isSymbolChar()) {
            endIndex++
        }

        // Return the bounds of the word
        return Pair(startIndex, endIndex)
    }

    private fun selectSymbol() {
        val index = caret.getIndex()
        val wordBounds = getSymbolBounds(index)
        wordBounds?.let {
            selStart = it.first
            selEnd = it.second
            caret.moveCaretTo(it.second)
        } ?: {
            resetSelection()
        }
        repaint()
    }

    private fun resetSelection() {
        selStart = -1
        selEnd = -1
    }

    private fun swapSelection() {
        val buffered = selStart
        selStart = selEnd
        selEnd = buffered
        caret.moveCaretTo(buffered)
        repaint()
    }

    private fun getSelectedAsString(): String {
        if (selStart == -1 || selEnd == -1) return ""
        val absSelection = getAbsSelection()
        return styledText.subList(absSelection.lowIndex, absSelection.highIndex).joinToString("") { it.content.toString() }
    }

    /**
     * State Change Events
     */

    private fun contentChanged() {
        lineBreakIDs.clear()
        for ((id, content) in styledText.withIndex()) {
            if (content.content == '\n') {
                lineBreakIDs.add(id)
            }
        }
        lineNumberScope.launch {
            lineCountChanged()
        }
        findAndReplace.updateResults()
    }

    private suspend fun lineCountChanged() {
        lineNumbers.lineCount = getLineCount()
    }

    fun scrollTo(index: Int) {
        val visibleRect = scrollPane.viewport.viewRect
        val lineHeight = getFontMetrics(font).height
        val charWidth = getFontMetrics(font).charWidth(' ')
        val pos = getAdvancedPosition(index)
        val posY = lineHeight * pos.line
        val posX = charWidth * pos.column

        val scrollMarginY = scrollMarginLines * lineHeight
        val scrollMarginX = scrollMarginChars * charWidth

        // Implement Smooth Scrolling if index would be out of visibleRect with scrollMargin
        var diffY: Int? = null
        var diffX: Int? = null
        if (posY < visibleRect.y + scrollMarginY) {
            diffY = posY - (visibleRect.y + scrollMarginY)
        }
        if (posY > visibleRect.y + visibleRect.height - scrollMarginY) {
            diffY = posY - (visibleRect.y + visibleRect.height - scrollMarginY)
        }

        if (posX < visibleRect.x + scrollMarginX) {
            diffX = posX - (visibleRect.x + scrollMarginX)
        }
        if (posX > visibleRect.x + visibleRect.width - scrollMarginX) {
            diffX = posX - (visibleRect.x + visibleRect.width - scrollMarginX)
        }

        diffY?.let {
            scrollPane.verticalScrollBar.value += diffY
        }

        diffX?.let {
            scrollPane.horizontalScrollBar.value += diffX
        }
    }

    private suspend fun caretPosChanged() {
        caretMovesJob?.cancel()
        caret.isMoving = true

        lineNumbers.repaint()

        val caretLineInfo = caret.getLineInfo()
        val visibleRect = scrollPane.viewport.viewRect
        val lineHeight = getFontMetrics(font).height
        val charWidth = getFontMetrics(font).charWidth(' ')
        val caretY = lineHeight * caretLineInfo.lineNumber
        val caretX = charWidth * caretLineInfo.columnID

        val scrollMarginY = scrollMarginLines * lineHeight
        val scrollMarginX = scrollMarginChars * charWidth

        // Implement Smooth Scrolling if caret would be out of visibleRect with scrollMargin
        var diffY: Int? = null
        var diffX: Int? = null
        if (caretY < visibleRect.y + scrollMarginY) {
            diffY = caretY - (visibleRect.y + scrollMarginY)
        }
        if (caretY > visibleRect.y + visibleRect.height - scrollMarginY) {
            diffY = caretY - (visibleRect.y + visibleRect.height - scrollMarginY)
        }

        if (caretX < visibleRect.x + scrollMarginX) {
            diffX = caretX - (visibleRect.x + scrollMarginX)
        }
        if (caretX > visibleRect.x + visibleRect.width - scrollMarginX) {
            diffX = caretX - (visibleRect.x + visibleRect.width - scrollMarginX)
        }

        diffY?.let {
            scrollPane.verticalScrollBar.value += diffY
        }

        diffX?.let {
            scrollPane.horizontalScrollBar.value += diffX
        }

        val absSelection = getAbsSelection()
        infoLogger?.printCaretInfo(InfoLogger.CodePosition(caret.getIndex(), caret.getLineInfo().lineNumber, caret.getLineInfo().columnID), absSelection.getLowPosition(), absSelection.getHighPosition())

        caretMovesJob = CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            caret.isMoving = false
        }
    }

    /**
     * Highlighting
     */

    fun debounceHighlighting() {
        debounceJob?.cancel()

        debounceJob = highlighterScope.launch {
            delay(debounceJobInterval)

            val contentToHighlight = ArrayList(styledText)
            val newStyled = highlighter?.highlight(contentToHighlight.joinToString("") { it.content.toString() }) ?: return@launch
            if (styledText.size == newStyled.size) {
                styledText.clear()
                styledText.addAll(newStyled)
                revalidate()
                repaint()
            }
        }
    }

    /**
     * Sub Calculations
     */

    /**
     *
     */
    private fun <T> splitListAtIndices(list: List<T>, indices: List<Int>): List<List<T>> {
        val result = mutableListOf<List<T>>()

        var startIndex = 0
        for (index in indices) {
            if (index >= startIndex && index < list.size) {
                result.add(list.subList(startIndex, index + 1))
                startIndex = index + 1
            } else {
                break
            }
        }

        if (startIndex <= list.size) {
            result.add(list.subList(startIndex, list.size))
        }

        return result
    }

    private fun getAdvancedPosition(index: Int): InfoLogger.CodePosition {
        return if (index in 0..styledText.size) {
            // Split the content  and append a placeholder to be able to calculate line and column for a none existing element (last position at the end)
            val lines = splitListAtIndices(styledText.subList(0, index), lineBreakIDs)
            val lineNumber = lines.size
            val columnID = (lines.lastOrNull()?.size ?: 0)
            InfoLogger.CodePosition(index, lineNumber, columnID)
        } else {
            nativeWarn("CEditorArea.getAdvancedPosition(): Index out of Bounds exception!")
            InfoLogger.CodePosition(index, -1, -1)
        }
    }

    /**
     * Overrides
     */

    override fun isFocusable(): Boolean = true

    override fun getPreferredSize(): Dimension {
        val fontMetrics = getFontMetrics(font)
        val charWidth = fontMetrics.charWidth(' ')
        val lineHeight = fontMetrics.ascent + fontMetrics.descent
        val preferredWidth = try {
            insets.left + getMaxLineLength() * charWidth + insets.right
        } catch (e: ConcurrentModificationException) {
            insets.left + splitListAtIndices(ArrayList(styledText), lineBreakIDs).ifEmpty { listOf(listOf()) }.maxOf { it.size } * charWidth + insets.right
        }
        val preferredHeight = getLineCount() * lineHeight + insets.top + insets.bottom
        return Dimension(preferredWidth + scrollMarginChars * charWidth, preferredHeight + scrollMarginLines * lineHeight)
    }

    /**
     * Inline Functions
     */
    fun getMaxLineLength(): Int = splitListAtIndices(styledText, lineBreakIDs).ifEmpty { listOf(listOf()) }.maxOf { it.size }
    fun getLineCount(): Int = lineBreakIDs.size + 1
    private fun lineOf(pos: Int): Int = splitListAtIndices(styledText.subList(0, pos), lineBreakIDs).size
    private fun columnOf(pos: Int): Int = (splitListAtIndices(styledText.subList(0, pos), lineBreakIDs).lastOrNull()?.size ?: 1) - 1
    fun getAbsSelection(): AbsSelection = if (selStart < selEnd) AbsSelection(selStart, selEnd, selStartLine, selEndLine, selStartColumn, selEndColumn) else AbsSelection(selEnd, selStart, selEndLine, selStartLine, selEndColumn, selStartColumn)
    fun getStyledText(): List<StyledChar> = styledText
    fun getLineBreakIDs() = ArrayList(lineBreakIDs)
    fun getText(): String = getStyledText().joinToString("") { it.content.toString() }


    /**
     * IO
     */

    private fun String.toStyledContent(): List<StyledChar> = this.map { StyledChar(it) }

    private fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }

    private fun getClipboardContent(): String? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val clipboardData = clipboard.getContents(null)
        return if (clipboardData != null && clipboardData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            clipboardData.getTransferData(DataFlavor.stringFlavor) as String
        } else {
            null
        }
    }

    /**
     * Classes
     */

    enum class Location {
        ANYWHERE,
        IN_SCROLLPANE
    }

    data class StyledChar(val content: Char, val style: Style? = null) {
        override fun toString(): String {
            return content.toString()
        }

        fun isLetterOrDigit(): Boolean {
            return content.isLetterOrDigit()
        }

        fun isSymbolChar(): Boolean {
            return content.isLetterOrDigit() || content == '_' || content == '$' || content == '.'
        }

        fun isWhiteSpace(): Boolean {
            return content.isWhitespace()
        }
    }

    data class AbsSelection(val lowIndex: Int, val highIndex: Int, val lowLine: Int, val highLine: Int, val lowColumn: Int, val highColumn: Int) {
        fun getLowPosition(): InfoLogger.CodePosition = InfoLogger.CodePosition(lowIndex, lowLine, lowColumn)
        fun getHighPosition(): InfoLogger.CodePosition = InfoLogger.CodePosition(highIndex, highLine, highColumn)
    }

    data class Style(val fgColor: Color? = null, val bgColor: Color? = null, val underline: Color? = null)

    data class LineInfo(val lineNumber: Int, val columnID: Int)

    enum class CaretMoveMode {
        INDEXMODE,
        LINEMODE,
        BOTHVALID
    }

    inner class Caret {
        var mode: CaretMoveMode = CaretMoveMode.BOTHVALID
            set(value) {
                if (field != value) {
                    when (field) {
                        CaretMoveMode.INDEXMODE -> {
                            calcLine()
                        }

                        CaretMoveMode.LINEMODE -> {
                            calcIndex()
                        }

                        CaretMoveMode.BOTHVALID -> {}
                    }
                }
                field = value
            }

        // State for INDEXMODE
        var caretPos: Int = 0

        // State for LINEMODE
        var caretLine: Int = 1
        var caretColumn: Int = 0

        var isMoving: Boolean = false

        private var linesBuffer: List<List<StyledChar>> = emptyList()
        private var index: Int = 0

        /**
         * Calculates the
         */
        private fun calcLine() {
            val advPos = getAdvancedPosition(caretPos)
            caretLine = advPos.line
            caretColumn = advPos.column
        }

        private fun calcIndex() {
            linesBuffer = splitListAtIndices(styledText, lineBreakIDs)

            // sum up previous line lengths
            index = 0
            for (line in 1..<caretLine) {
                index += linesBuffer[line - 1].size
            }

            // add maximum caretcolumn
            index += when {
                caretColumn < linesBuffer[caretLine - 1].size -> caretColumn
                caretColumn >= linesBuffer[caretLine - 1].size -> linesBuffer[caretLine - 1].size - 1
                else -> {
                    0
                }
            }

            // add 1 if it is the last element
            if (caretLine == linesBuffer.size && caretColumn >= linesBuffer[caretLine - 1].size) {
                index += 1
            }

            caretPos = index
        }

        fun moveCaretLeft() {
            mode = CaretMoveMode.INDEXMODE
            if (caretPos > 0) {
                caretPos--
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretRight() {
            mode = CaretMoveMode.INDEXMODE
            if (caretPos < styledText.size) { //if (caretPos < text.length) {
                caretPos++
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretLeft(steps: Int) {
            mode = CaretMoveMode.INDEXMODE
            if (caretPos - steps >= 0) {
                caretPos -= steps
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretRight(steps: Int) {
            mode = CaretMoveMode.INDEXMODE
            if (caretPos < styledText.size) {
                caretPos += steps
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretUp() {
            mode = CaretMoveMode.LINEMODE
            if (caretLine > 1) {
                caretLine--
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretDown() {
            mode = CaretMoveMode.LINEMODE
            if (caretLine < getLineCount()) {
                caretLine++
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretHome() {
            mode = CaretMoveMode.LINEMODE
            if (caretColumn > 0) {
                caretColumn = 0
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretEnd() {
            mode = CaretMoveMode.LINEMODE
            val lines = splitListAtIndices(styledText, lineBreakIDs)
            val isLastLine = caretLine == lines.size
            val currLine = lines[caretLine - 1]
            if (caretColumn < currLine.size - 1) {
                caretColumn = currLine.size - if (!isLastLine) 1 else 0
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretTo(e: MouseEvent) {
            val fm = getFontMetrics(font)
            var lineID = if (e.y < 0) {
                0
            } else {
                e.y / fm.height
            }

            if (lineID >= getLineCount()) {
                lineID = getLineCount() - 1
            }

            var columnID = if (e.x < 0) {
                0
            } else {
                e.x / fm.charWidth(' ')
            }

            if (columnID >= getMaxLineLength()) {
                columnID = getMaxLineLength()
            }
            if (lineID >= 0 && columnID >= 0) moveCaretTo(lineID + 1, columnID)
        }


        fun moveCaretTo(index: Int) {
            mode = CaretMoveMode.INDEXMODE
            if (index > 0 && index <= styledText.size) {
                caretPos = index
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun moveCaretTo(lineNumber: Int, columnIndex: Int) {
            mode = CaretMoveMode.LINEMODE
            if (lineNumber > 0 && columnIndex >= 0) {
                caretLine = lineNumber
                caretColumn = columnIndex
                repaint()
                selScope.launch { caretPosChanged() }
            }
        }

        fun resetPos() {
            mode = CaretMoveMode.INDEXMODE
            caretPos = 0
            repaint()
            selScope.launch { caretPosChanged() }
        }

        fun getLineInfo(): LineInfo {
            mode = CaretMoveMode.BOTHVALID
            return LineInfo(caretLine, caretColumn)
        }

        fun getIndex(): Int {
            mode = CaretMoveMode.BOTHVALID
            return caretPos
        }
    }

    inner class EditorKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent) {
            // Character Insertion
            when {
                e.keyChar.isISOControl() -> {

                }

                e.keyChar.isDefined() -> {
                    if (isEditable) {
                        val newChar = e.keyChar.toString()
                        deleteSelected()
                        insertText(caret.getIndex(), newChar)
                    }
                }
            }
        }

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_TAB -> {
                    if (isEditable) {
                        if (e.isShiftDown) {
                            removeIndent()
                        } else {
                            indent()
                        }
                    }
                }

                KeyEvent.VK_A -> {
                    if (e.isControlDown) {
                        selectAll()
                    }
                }

                KeyEvent.VK_C -> {
                    if (e.isControlDown) {
                        if (selStart != selEnd) {
                            val absSelection = getAbsSelection()
                            copyToClipboard(styledText.subList(absSelection.lowIndex, absSelection.highIndex).joinToString("") { it.content.toString() })
                        }
                    }
                }

                KeyEvent.VK_Z -> {
                    if (e.isControlDown && isEditable) {
                        if (e.isShiftDown) {
                            redo()
                        } else {
                            undo()
                        }
                    }
                }

                KeyEvent.VK_V -> {
                    if (e.isControlDown && isEditable) {
                        val content = getClipboardContent()
                        deleteSelected()
                        content?.let { text ->
                            insertText(caret.getIndex(), text)
                        }
                    }
                }

                KeyEvent.VK_X -> {
                    if (e.isControlDown && isEditable) {
                        if (selStart != selEnd) {
                            val absSelection = getAbsSelection()
                            copyToClipboard(styledText.subList(absSelection.lowIndex, absSelection.highIndex).joinToString("") { it.content.toString() })
                            deleteSelected()
                        }
                    }
                }

                KeyEvent.VK_ENTER -> {
                    if (isEditable) {
                        deleteSelected()
                        insertText(caret.getIndex(), "\n")
                    }
                }

                KeyEvent.VK_LEFT -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        resetSelection()
                        caret.moveCaretLeft()
                    }
                }

                KeyEvent.VK_RIGHT -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        resetSelection()
                        caret.moveCaretRight()
                    }
                }

                KeyEvent.VK_UP -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        resetSelection()
                        caret.moveCaretUp()
                    }
                }

                KeyEvent.VK_DOWN -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        resetSelection()
                        caret.moveCaretDown()
                    }
                }

                KeyEvent.VK_BACK_SPACE -> {
                    if (isEditable) {
                        if (selStart != -1 && selEnd != -1) deleteSelected() else {
                            if (caret.getIndex() > 0) deleteText(caret.getIndex() - 1, caret.getIndex())
                        }
                    }
                }

                KeyEvent.VK_DELETE -> if (isEditable) {
                    if (selStart != -1 && selEnd != -1) deleteSelected() else {
                        if (caret.getIndex() <= styledText.size) deleteText(caret.getIndex(), caret.getIndex() + 1)
                    }
                }

                KeyEvent.VK_HOME -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        if (selStart < selEnd) {
                            swapSelection()
                        } else {
                            resetSelection()
                            caret.moveCaretHome()
                        }
                    }
                }

                KeyEvent.VK_END -> {
                    if (e.isShiftDown) handleShiftSelection(e) else {
                        if (selStart > selEnd) {
                            swapSelection()
                        } else {
                            resetSelection()
                            caret.moveCaretEnd()
                        }
                    }
                }

                KeyEvent.VK_F -> {
                    if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.FIND)
                }

                KeyEvent.VK_R -> {
                    if (e.isControlDown) findAndReplace.open(getSelectedAsString(), CEditorAnalyzer.Mode.REPLACE)
                }

                // Custom Use
                KeyEvent.VK_S -> {
                    if (e.isControlDown) shortCuts?.ctrlS()
                }
            }
        }

        override fun keyReleased(e: KeyEvent?) {}
    }

    inner class EditorMouseListener : MouseAdapter() {

        var shiftIsPressed = false

        init {
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent?) {
                    shiftIsPressed = e?.isShiftDown ?: false
                }

                override fun keyReleased(e: KeyEvent?) {
                    shiftIsPressed = e?.isShiftDown ?: false
                }
            })
        }

        override fun mouseExited(e: MouseEvent?) {
            cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
        }

        override fun mouseEntered(e: MouseEvent?) {
            cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        }

        override fun mousePressed(e: MouseEvent) {
            this@CEditorArea.requestFocus()
            if (shiftIsPressed) {
                handleMouseSelection(e)
            } else {
                if (e.clickCount == 2) {
                    handleMouseDoubleClick(e)
                } else {
                    selStart = -1
                    selEnd = -1
                    caret.moveCaretTo(e)
                }
            }
        }
    }

    inner class EditorMouseDragListener : MouseMotionAdapter() {

        override fun mouseDragged(e: MouseEvent) {
            handleMouseSelection(e)
        }

        override fun mouseMoved(e: MouseEvent?) {}
    }

    override fun getBorder(): Border {
        return BorderFactory.createEmptyBorder(0, UIStates.scale.get().SIZE_INSET_MEDIUM, 0, UIStates.scale.get().SIZE_INSET_MEDIUM)
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().COLOR_BG_0
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().getColor(CodeStyle.BASE0)
    }

    override fun getFont(): Font {
        return FontType.CODE.getFont()
    }
}