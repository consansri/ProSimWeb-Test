package me.c3.ui.styled.editor

import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.Timer

class CEditorArea(themeManager: ThemeManager, scaleManager: ScaleManager) : JComponent() {

    val lineBreakIDs: MutableList<Int> = mutableListOf()
    private val styledText: MutableList<StyledChar> = mutableListOf()
    var caretPos = 0
        set(value) {
            field = value
            caretPosChanged()
        }

    var caretLine = 0
    var caretColumn = 0

    var selectionStart: Int = -1
        set(value) {
            field = value
            CoroutineScope(Dispatchers.Default).launch {
                selectionStartLine = if (selectionStart == -1) -1 else lineOf(selectionStart)
                selectionStartColumn = if (selectionStart == -1) -1 else columnOf(selectionStart)
            }
        }
    var selectionEnd: Int = -1
        set(value) {
            field = value
            CoroutineScope(Dispatchers.Default).launch {
                selectionEndLine = if (selectionEnd == -1) -1 else lineOf(selectionEnd)
                selectionEndColumn = if (selectionEnd == -1) -1 else columnOf(selectionEnd)
            }
        }

    var selectionStartLine = -1
    var selectionStartColumn = -1
    var selectionEndLine = -1
    var selectionEndColumn = -1

    var tabSize = scaleManager.curr.fontScale.tabSize
    val scrollPane = CScrollPane(themeManager, scaleManager, true, this)

    init {
        setUI(CEditorAreaUI(themeManager, scaleManager))
        addKeyListener(EditorKeyListener())
        addMouseListener(EditorMouseListener())
        addMouseMotionListener(EditorMouseDragListener())
    }

    /**
     * Text Editing
     */

    // Insertion / Deletion
    private fun insertText(pos: Int, newText: String) {
        styledText.addAll(pos, newText.map { StyledChar(it) })
        //text = text.substring(0, pos) + newText + text.substring(pos)
        caretPos += newText.length
        updateLineBreakIDs()
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
            updateLineBreakIDs()
            resetSelection()
            revalidate()
            repaint()
        }
    }

    private fun deleteSelected() {
        if (selectionStart != -1 && selectionEnd != -1) deleteText(getAbsSelection().lowIndex, getAbsSelection().highIndex)
    }

    private fun indent() {
        val absSelection = getAbsSelection()
        // Implement this Function
        if (absSelection.lowLine == absSelection.highLine) {
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

            if (selectionStart < selectionEnd) {
                selectionStart += before
                selectionEnd += (before + inSelection)
                caretPos += (before + inSelection)
            } else {
                selectionStart += (before + inSelection)
                selectionEnd += before
                caretPos += before
            }

            revalidate()
            repaint()
        }
    }

    private fun removeIndent() {
        val absSelection = getAbsSelection()
        // Implement this Function
        if (absSelection.lowLine == absSelection.highLine) {
            removeLineIndent(caretLine - 1)
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

            if (selectionStart < selectionEnd) {
                selectionStart -= before
                selectionEnd -= (before + inSelection)
                caretPos -= (before + inSelection)
            } else {
                selectionStart -= (before + inSelection)
                selectionEnd -= before
                caretPos -= before
            }

        }
        revalidate()
        repaint()
    }

    private fun addLineIndent(lineID: Int): Int {
        val tabInsertPos = if (lineID <= 0) 0 else lineBreakIDs.getOrNull(lineID - 1)
        tabInsertPos?.let { insertNotNull ->
            if (tabInsertPos == 0) {
                styledText.addAll(insertNotNull, " ".repeat(tabSize).map { StyledChar(it) })
                updateLineBreakIDs()
                return tabSize
            } else {
                if (insertNotNull < styledText.size - 1) {
                    styledText.addAll(insertNotNull + 1, " ".repeat(tabSize).map { StyledChar(it) })
                    updateLineBreakIDs()
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
                updateLineBreakIDs()
                return removed
            } else {
                if (insertNotNull < styledText.size - 1) {
                    var removed = 0
                    val subList = styledText.subList(insertNotNull + 1, styledText.size)
                    while (subList.firstOrNull()?.content == ' ' && removed < tabSize) {
                        subList.removeFirst()
                        removed++
                    }
                    updateLineBreakIDs()
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
            val lines = splitAtLineBreak(styledText)
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
            val lines = splitAtLineBreak(styledText)
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
        val currColumn = caretColumn
        val currLineID = caretLine - 1
        val lines = splitAtLineBreak(styledText)
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
        val lines = splitAtLineBreak(styledText)

        // Check for valid line index
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
        if (selectionStart == -1) {
            selectionStart = caretPos
        }

        when (e.keyCode) {
            KeyEvent.VK_LEFT -> moveCaretLeft()
            KeyEvent.VK_UP -> moveCaretUp()
            KeyEvent.VK_RIGHT -> moveCaretRight()
            KeyEvent.VK_DOWN -> moveCaretDown()
            KeyEvent.VK_HOME -> moveCaretHome()
            KeyEvent.VK_END -> moveCaretEnd()
        }

        selectionEnd = caretPos

        if (selectionStart == selectionEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun handleMouseSelection(e: MouseEvent) {
        if (selectionStart == -1) {
            selectionStart = caretPos
        }

        moveCaretTo(e)

        selectionEnd = caretPos

        if (selectionStart == selectionEnd) {
            resetSelection()
        }

        repaint()
    }

    private fun resetSelection() {
        selectionStart = -1
        selectionEnd = -1
    }

    private fun updateLineBreakIDs() {
        lineBreakIDs.clear()
        for ((id, content) in styledText.withIndex()) {
            if (content.content == '\n') {
                lineBreakIDs.add(id)
            }
        }
    }

    /**
     * ScrollPane Controls
     */

    private fun caretPosChanged() {
        SwingUtilities.invokeLater {
            caretLine = lineOf(caretPos)
            caretColumn = columnOf(caretPos)

            val visibleRect = scrollPane.viewport.viewRect
            val lineHeight = getFontMetrics(font).height
            val charWidth = getFontMetrics(font).charWidth(' ')
            val caretY = lineHeight * caretLine
            val caretX = charWidth * caretColumn

            // Smooth Scrolling with Timer
            val scrollSpeed = lineHeight * 3 // Adjust scroll speed as needed (pixels per timer tick)
            val timer = Timer(0, null) // Timer interval in milliseconds

            var targetScrollY = Math.max(0, Math.min(caretY - visibleRect.height / 2, scrollPane.verticalScrollBar.maximum - visibleRect.height))
            if (caretY + lineHeight > visibleRect.y + visibleRect.height) {
                targetScrollY = caretY - visibleRect.height + lineHeight
            }

            var currentScrollY = scrollPane.verticalScrollBar.value

            var targetScrollX = Math.max(0, Math.min(caretX - visibleRect.width / 2, scrollPane.horizontalScrollBar.maximum - visibleRect.width + insets.left + insets.right))
            if (caretX + charWidth > visibleRect.x + visibleRect.width - insets.right) {
                targetScrollX = caretX - visibleRect.width + charWidth + insets.left
            }

            var currentScrollX = scrollPane.horizontalScrollBar.value

            timer.addActionListener {
                val scrollDiffY = targetScrollY - currentScrollY
                val scrollDiffX = targetScrollX - currentScrollX

                val scrollAmountY = Math.min(Math.abs(scrollDiffY), scrollSpeed).toInt() * if (scrollDiffY > 0) 1 else -1
                currentScrollY += scrollAmountY
                scrollPane.verticalScrollBar.value = currentScrollY

                val scrollAmountX = Math.min(Math.abs(scrollDiffX), scrollSpeed).toInt() * if (scrollDiffX > 0) 1 else -1
                currentScrollX += scrollAmountX
                scrollPane.horizontalScrollBar.value = currentScrollX

                if (scrollDiffY == 0 && scrollDiffX == 0) {
                    timer.stop()
                    repaint()
                }
            }

            timer.start()
        }
    }

    /**
     * Sub Calculations
     */

    private fun splitAtLineBreak(list: List<StyledChar>): List<List<StyledChar>> {
        if (list.isEmpty()) return listOf(listOf())
        val result = mutableListOf<List<StyledChar>>()
        var sublist = mutableListOf<StyledChar>()
        var lastWasLineBreak = false
        for (char in list) {
            if (char.content == '\n') {
                result.add(sublist)
                sublist = mutableListOf()
                lastWasLineBreak = true
            } else {
                lastWasLineBreak = false
                sublist.add(char)
            }
        }
        if (sublist.isNotEmpty() || lastWasLineBreak) {
            result.add(sublist)
        }
        return result
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
        return Dimension(preferredWidth + 3 * charWidth, preferredHeight + 3 * lineHeight)
    }

    /**
     * Inline Functions
     */

    fun getMaxLineLength(): Int = splitAtLineBreak(ArrayList(styledText)).ifEmpty { listOf(listOf()) }.maxOf { it.size }
    fun getLineCount(): Int = splitAtLineBreak(ArrayList(styledText)).size
    private fun lineOf(pos: Int): Int = splitAtLineBreak(styledText.subList(0, pos)).size
    private fun columnOf(pos: Int): Int = splitAtLineBreak(styledText.subList(0, caretPos)).lastOrNull()?.size ?: 0
    fun getAbsSelection(): AbsSelection = if (selectionStart < selectionEnd) {
        AbsSelection(selectionStart, selectionEnd, selectionStartLine, selectionEndLine, selectionStartColumn, selectionEndColumn)
    } else {
        AbsSelection(selectionEnd, selectionStart, selectionEndLine, selectionStartLine, selectionEndColumn, selectionStartColumn)
    }

    fun getStyledText(): List<StyledChar> = styledText

    /**
     * Data Classes
     */

    data class StyledChar(val content: Char, val style: Style? = null)
    data class AbsSelection(val lowIndex: Int, val highIndex: Int, val lowLine: Int, val highLine: Int, val lowColumn: Int, val highColumn: Int)
    data class Style(val fgColor: Color? = null, val bgColor: Color? = null)

    /**
     * Input Listeners
     */

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

                KeyEvent.VK_BACK_SPACE -> if (selectionStart != -1 && selectionEnd != -1) deleteSelected() else (if (caretPos > 0) deleteText(caretPos - 1, caretPos))
                KeyEvent.VK_DELETE -> if (selectionStart != -1 && selectionEnd != -1) deleteSelected() else (if (caretPos <= styledText.size) deleteText(caretPos, caretPos + 1))
                KeyEvent.VK_HOME -> if (e.isShiftDown) handleShiftSelection(e) else moveCaretHome()
                KeyEvent.VK_END -> if (e.isShiftDown) handleShiftSelection(e) else moveCaretEnd()
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

        override fun mousePressed(e: MouseEvent) {
            if (shiftIsPressed) {
                handleMouseSelection(e)
            } else {
                selectionStart = -1
                selectionEnd = -1
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