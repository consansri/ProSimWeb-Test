package me.c3.ui.styled.editor

import emulator.kit.nativeError
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.util.Stack
import javax.swing.JComponent
import kotlin.time.measureTime

class CEditorArea(themeManager: ThemeManager, scaleManager: ScaleManager, val location: Location, val maxStackSize: Int = 30, var stackQueryMillis: Long = 500) : JComponent() {

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

    // Interactable Components
    var fileInterface: FileInterface? = null
        set(value) {
            field = value
            styledText.clear()
            caretPos = 0
            resetSelection()
            value?.let {
                insertText(0, value.getRawContent())
            }
            revalidate()
            repaint()
        }

    var infoLogger: InfoLogger? = null
    var highlighter: Highlighter? = null
    val scrollPane: CScrollPane = CScrollPane(themeManager, scaleManager, true, this)
    val lineNumbers: CEditorLineNumbers = CEditorLineNumbers(themeManager, scaleManager, this)

    // State History
    private val textStateHistory = Stack<List<StyledChar>>()
    private val undoneTextStateHistory = Stack<List<StyledChar>>()
    private var lastSave: Long = 0

    // Caret
    var caretPos = 0
        set(value) {
            field = value
            selScope.launch { caretPosChanged() }
        }

    var caretLine = 0
    var caretColumn = 0

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

    // StyleSettings
    var tabSize = scaleManager.curr.fontScale.tabSize
    var scrollMarginLines = 4
    var scrollMarginChars = 10

    /**
     * Initialization
     */

    init {
        setUI(CEditorAreaUI(themeManager, scaleManager))
        textStateHistory.push(styledText.toList())
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
            if (caretPos >= previousState.size) {
                caretPos = previousState.size
            }
            resetSelection()
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
            if (caretPos >= nextState.size) {
                caretPos = nextState.size
            }
            resetSelection()
            revalidate()
            repaint()
        }
    }

    private fun queryStateChange() {
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
    private fun insertText(pos: Int, newText: String) {
        if (pos > styledText.size) {
            nativeError("Text Insertion invalid for position $pos and current size ${styledText.size}!")
            return
        }
        styledText.addAll(pos, newText.map { StyledChar(if (it == '\t') ' ' else it) })
        //text = text.substring(0, pos) + newText + text.substring(pos)
        caretPos += newText.length
        queryStateChange()
        contentChanged()
        resetSelection()
        revalidate()
        repaint()
    }

    private fun deleteText(startIndex: Int, endIndex: Int) {
        if (startIndex < endIndex && endIndex <= styledText.size) { //if (startIndex < endIndex && endIndex <= text.length) {
            styledText.subList(startIndex, endIndex).clear() //text = text.substring(0, startIndex) + text.substring(endIndex)
            if (caretPos > startIndex) {
                caretPos -= endIndex - startIndex
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

    private fun indent() {
        val absSelection = getAbsSelection()
        // Implement this Function
        if (absSelection.lowIndex == absSelection.highIndex) {
            val spacesToInsert = tabSize - (caretColumn % tabSize)
            insertText(caretPos, " ".repeat(spacesToInsert))
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
                caretPos += (before + inSelection)
            } else {
                selStart += (before + inSelection)
                selEnd += before
                caretPos += before
            }
            queryStateChange()
            revalidate()
            repaint()
        }
    }

    private fun removeIndent() {
        val absSelection = getAbsSelection()
        // Implement this Function
        if (absSelection.lowIndex == absSelection.highIndex) {
            caretPos -= removeLineIndent(caretLine - 1)
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
                caretPos -= (before + inSelection)
            } else {
                selStart -= (before + inSelection)
                selEnd -= before
                caretPos -= before
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

    // Caret
    private fun moveCaretLeft() {
        if (caretPos > 0) {
            caretPos--
            repaint()
        }
    }

    private fun moveCaretRight() {
        if (caretPos < styledText.size) { //if (caretPos < text.length) {
            caretPos++
            repaint()
        }
    }

    private fun moveCaretUp() {
        if (caretPos > 0) {
            val lines = splitListAtIndices(styledText, lineBreakIDs)
            val currLine = lineOf(caretPos) - 1

            // Check if there's a line above
            if (currLine > 0) {
                val lineAbove = lines[currLine - 1]
                val newAfter = caretColumn.coerceAtMost(lineAbove.size)
                caretPos -= caretColumn + 1 + (lineAbove.size - newAfter)
            }
            repaint()
        }
    }

    private fun moveCaretDown() {
        if (caretPos < styledText.size) {
            val lines = splitListAtIndices(styledText, lineBreakIDs)
            val currLine = caretLine - 1

            // Check if there's a line below
            if (currLine < lines.size - 1) {
                val after = lines[currLine].size - caretColumn
                val lineBelow = lines[currLine + 1]
                val newBefore = caretColumn.coerceAtMost(lineBelow.size) + 1
                caretPos += after + newBefore
            }
            repaint()
        }
    }

    private fun moveCaretHome() {
        val currColumn = columnOf(caretPos)
        if (currColumn > 0) {
            caretPos -= currColumn
            repaint()
        }
    }

    private fun moveCaretEnd() {
        val lines = splitListAtIndices(styledText, lineBreakIDs)
        val currColumn = caretColumn
        val currLineID = caretLine - 1
        if (currLineID < lines.size) {
            val after = lines[currLineID].size - currColumn
            caretPos += after
            repaint()
        }
    }

    private fun moveCaretTo(e: MouseEvent) {
        val fm = getFontMetrics(font)
        val lineID = e.y / fm.height
        val columnID = e.x / fm.charWidth(' ')
        if (lineID >= 0 && columnID >= 0) moveCaretTo(lineID, columnID)
    }

    private fun moveCaretTo(lineIndex: Int, columnIndex: Int) {
        // Check for valid line index
        val lines = splitListAtIndices(styledText, lineBreakIDs)
        if (lineIndex < 0 || lineIndex >= lines.size) {
            return // Clamp to existing line count
        }

        // Check for valid column index within the target line
        val targetLine = lines[lineIndex]
        val validColumnIndex = columnIndex.coerceAtMost(targetLine.size)

        // Calculate new caret position based on line breaks
        var newCaretPos = 0
        for (i in 0 until lineIndex) {
            newCaretPos += lines[i].size + 1 // Add 1 for newline character
        }
        newCaretPos += validColumnIndex

        // Update caret position and reset selection
        caretPos = newCaretPos
        repaint()
    }

    // Selection
    private fun handleShiftSelection(e: KeyEvent) {
        if (selStart == -1) {
            selStart = caretPos
        }

        when (e.keyCode) {
            KeyEvent.VK_LEFT -> moveCaretLeft()
            KeyEvent.VK_UP -> moveCaretUp()
            KeyEvent.VK_RIGHT -> moveCaretRight()
            KeyEvent.VK_DOWN -> moveCaretDown()
            KeyEvent.VK_HOME -> moveCaretHome()
            KeyEvent.VK_END -> moveCaretEnd()
        }

        selEnd = caretPos

        if (selStart == selEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun handleMouseSelection(e: MouseEvent) {
        if (selStart == -1) {
            selStart = caretPos
        }

        moveCaretTo(e)

        selEnd = caretPos

        if (selStart == selEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun selectAll() {
        selStart = 0
        selEnd = styledText.size
        caretPos = styledText.size
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
        caretPos = buffered
        repaint()
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
    }

    private suspend fun lineCountChanged() {
        lineNumbers.lineCount = getLineCount()
    }

    private suspend fun caretPosChanged() {
        val caretCodePosition = getAdvancedPosition(caretPos)
        caretLine = caretCodePosition.line
        caretColumn = caretCodePosition.column

        lineNumbers.repaint()

        val visibleRect = scrollPane.viewport.viewRect
        val lineHeight = getFontMetrics(font).height
        val charWidth = getFontMetrics(font).charWidth(' ')
        val caretY = lineHeight * caretLine
        val caretX = charWidth * caretColumn

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
        infoLogger?.printCaretInfo(getCaretPosition(), absSelection.getLowPosition(), absSelection.getHighPosition())
    }

    /**
     * Highlighting
     */

    private fun debounceHighlighting() {
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

    private fun <T> splitListAtIndices(list: List<T>, indices: List<Int>): List<List<T>> {
        val result = mutableListOf<List<T>>()

        var startIndex = 0
        for (index in indices) {
            if (index < list.size) {
                result.add(list.subList(startIndex, index))
                startIndex = index + 1
            }
        }

        when {
            startIndex < list.size -> result.add(list.subList(startIndex, list.size))
            startIndex == list.size -> result.add(listOf())
        }

        return result
    }

    private fun getAdvancedPosition(index: Int): InfoLogger.CodePosition {
        return if (index in styledText.indices) {
            val lines = splitListAtIndices(styledText.subList(0, index), lineBreakIDs)
            InfoLogger.CodePosition(index, lines.size, lines.lastOrNull()?.size ?: 0)
        } else {
            // TODO("Fix index out of bounds better than just returning invalid values!")
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
        val preferredWidth = insets.left + getMaxLineLength() * charWidth + insets.right
        val preferredHeight = getLineCount() * lineHeight + insets.top + insets.bottom
        return Dimension(preferredWidth + scrollMarginChars * charWidth, preferredHeight + scrollMarginLines * lineHeight)
    }

    /**
     * Inline Functions
     */
    fun getMaxLineLength(): Int = splitListAtIndices(styledText, lineBreakIDs).ifEmpty { listOf(listOf()) }.maxOf { it.size }
    fun getLineCount(): Int = lineBreakIDs.size + 1
    private fun lineOf(pos: Int): Int = splitListAtIndices(styledText.subList(0, pos), lineBreakIDs).size
    private fun columnOf(pos: Int): Int = splitListAtIndices(styledText.subList(0, pos), lineBreakIDs).lastOrNull()?.size ?: 0
    fun getAbsSelection(): AbsSelection = if (selStart < selEnd) AbsSelection(selStart, selEnd, selStartLine, selEndLine, selStartColumn, selEndColumn) else AbsSelection(selEnd, selStart, selEndLine, selStartLine, selEndColumn, selStartColumn)
    fun getStyledText(): List<StyledChar> = styledText
    fun getLineBreakIDs() = ArrayList(lineBreakIDs)
    fun getCaretPosition(): InfoLogger.CodePosition = InfoLogger.CodePosition(caretPos, caretLine, caretColumn)

    /**
     * Data Classes
     */

    data class StyledChar(val content: Char, val style: Style? = null) {
        override fun toString(): String {
            return content.toString()
        }
    }

    data class AbsSelection(val lowIndex: Int, val highIndex: Int, val lowLine: Int, val highLine: Int, val lowColumn: Int, val highColumn: Int) {
        fun getLowPosition(): InfoLogger.CodePosition = InfoLogger.CodePosition(lowIndex, lowLine, lowColumn)
        fun getHighPosition(): InfoLogger.CodePosition = InfoLogger.CodePosition(highIndex, highLine, highColumn)
    }

    data class Style(val fgColor: Color? = null, val bgColor: Color? = null)

    /**
     * IO
     */

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

    enum class Location {
        ANYWHERE,
        IN_SCROLLPANE
    }

    inner class EditorKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent) {
            // Character Insertion
            when {
                e.keyChar.isISOControl() -> {

                }

                e.keyChar.isDefined() -> {
                    val newChar = e.keyChar.toString()
                    deleteSelected()
                    insertText(caretPos, newChar)
                }
            }
        }

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_TAB -> {
                    if (e.isShiftDown) {
                        removeIndent()
                    } else {
                        indent()
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
                    if (e.isControlDown) {
                        if (e.isShiftDown) {
                            redo()
                        } else {
                            undo()
                        }
                    }
                }

                KeyEvent.VK_V -> {
                    if (e.isControlDown) {
                        val content = getClipboardContent()
                        deleteSelected()
                        content?.let { text ->
                            insertText(caretPos, text)
                        }
                    }
                }

                KeyEvent.VK_X -> {
                    if (e.isControlDown) {
                        if (selStart != selEnd) {
                            val absSelection = getAbsSelection()
                            copyToClipboard(styledText.subList(absSelection.lowIndex, absSelection.highIndex).joinToString("") { it.content.toString() })
                            deleteSelected()
                        }
                    }
                }

                KeyEvent.VK_ENTER -> {
                    deleteSelected()
                    insertText(caretPos, "\n")
                }

                KeyEvent.VK_LEFT -> if (e.isShiftDown) handleShiftSelection(e) else {
                    resetSelection()
                    moveCaretLeft()
                }

                KeyEvent.VK_RIGHT -> if (e.isShiftDown) handleShiftSelection(e) else {
                    resetSelection()
                    moveCaretRight()
                }

                KeyEvent.VK_UP -> if (e.isShiftDown) handleShiftSelection(e) else {
                    resetSelection()
                    moveCaretUp()
                }

                KeyEvent.VK_DOWN -> if (e.isShiftDown) handleShiftSelection(e) else {
                    resetSelection()
                    moveCaretDown()
                }

                KeyEvent.VK_BACK_SPACE -> if (selStart != -1 && selEnd != -1) deleteSelected() else (if (caretPos > 0) deleteText(caretPos - 1, caretPos))
                KeyEvent.VK_DELETE -> if (selStart != -1 && selEnd != -1) deleteSelected() else (if (caretPos <= styledText.size) deleteText(caretPos, caretPos + 1))
                KeyEvent.VK_HOME -> if (e.isShiftDown) handleShiftSelection(e) else {
                    if (selStart < selEnd) {
                        swapSelection()
                    } else {
                        resetSelection()
                        moveCaretHome()
                    }
                }

                KeyEvent.VK_END -> if (e.isShiftDown) handleShiftSelection(e) else {
                    if (selStart > selEnd) {
                        swapSelection()
                    } else {
                        resetSelection()
                        moveCaretEnd()
                    }
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
            this@CEditorArea.requestFocus()
            cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
        }

        override fun mousePressed(e: MouseEvent) {
            if (shiftIsPressed) {
                handleMouseSelection(e)
            } else {
                selStart = -1
                selEnd = -1
                moveCaretTo(e)
            }
        }
    }

    inner class EditorMouseDragListener : MouseMotionAdapter() {

        override fun mouseDragged(e: MouseEvent) {
            handleMouseSelection(e)
        }

        override fun mouseMoved(e: MouseEvent?) {}
    }
}