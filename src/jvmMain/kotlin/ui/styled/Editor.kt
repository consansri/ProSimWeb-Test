package me.c3.ui.styled

import emulator.kit.nativeLog
import me.c3.ui.UIManager
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

class Editor(themeManager: ThemeManager, scaleManager: ScaleManager) : JComponent() {

    val styledText: MutableList<StyledChar> = mutableListOf()

    //private var text: String = "This is some basic Text!"
    var caretPos = 0
        set(value) {
            field = value
            nativeLog("line: ${currLine()}, column: ${currColumn()}")

        }

    var selectionStart: Int = -1
    var selectionEnd: Int = -1

    init {
        setUI(EditorUI(themeManager, scaleManager))
        addKeyListener(EditorKeyListener())
        addMouseListener(EditorMouseListener())
        addMouseMotionListener(EditorMouseDragListener())
    }

    private fun insertText(pos: Int, newText: String) {
        deleteSelected()
        styledText.addAll(pos, newText.map { StyledChar(it) })
        //text = text.substring(0, pos) + newText + text.substring(pos)
        caretPos += newText.length
        resetSelection()
        repaint()
    }

    private fun deleteText(startIndex: Int, endIndex: Int) {
        if (startIndex < endIndex && endIndex <= styledText.size) { //if (startIndex < endIndex && endIndex <= text.length) {
            styledText.subList(startIndex, endIndex).clear() //text = text.substring(0, startIndex) + text.substring(endIndex)
            if (caretPos > startIndex) {
                caretPos -= endIndex - startIndex
            }
            resetSelection()
            repaint()
        }
    }

    private fun deleteSelected() {
        if (selectionStart != -1 && selectionEnd != -1) deleteText(getAbsSelection().lowIndex, getAbsSelection().highIndex)
    }

    private fun resetSelection() {
        selectionStart = -1
        selectionEnd = -1
    }

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
            val currLine = currLine() - 1

            // Check if there's a line above
            if (currLine > 0) {
                val currColumn = currColumn()
                val lineAbove = lines[currLine - 1]
                val newAfter = currColumn.coerceAtMost(lineAbove.size)
                caretPos -= currColumn + 1 + (lineAbove.size - newAfter)
            }
            repaint()
        }
    }

    private fun moveCaretDown() {
        if (caretPos < styledText.size) {
            val lines = splitAtLineBreak(styledText)
            val currLine = currLine() - 1

            // Check if there's a line below
            if (currLine < lines.size - 1) {
                val currColumn = currColumn()
                val after = lines[currLine].size - currColumn
                val lineBelow = lines[currLine + 1]
                val newBefore = currColumn.coerceAtMost(lineBelow.size) + 1
                caretPos += after + newBefore
            }
            repaint()
        }
    }

    private fun moveCaretHome() {
        val currColumn = currColumn()
        if (currColumn > 0) {
            caretPos -= currColumn
        }
    }

    private fun moveCaretEnd() {
        val currColumn = currColumn()
        val currLineID = currLine() - 1
        val lines = splitAtLineBreak(styledText)
        if (currLineID < lines.size) {
            val after = lines[currLineID].size - currColumn
            caretPos += after
        }
    }

    private fun moveCaretTo(e: MouseEvent) {
        val fm = getFontMetrics(font)
        val lineID = e.y / fm.height
        val columnID = e.x / fm.charWidth(' ')
        moveCaretTo(lineID, columnID)
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

        repaint()
    }

    private fun handleMouseSelection(e: MouseEvent) {
        if (selectionStart == -1) {
            selectionStart = caretPos
        }

        moveCaretTo(e)

        selectionEnd = caretPos

        repaint()
    }

    fun splitAtLineBreak(list: List<StyledChar>): List<List<StyledChar>> {
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

    fun getLineCount(): Int {
        val currContent = ArrayList(styledText)
        return splitAtLineBreak(currContent).size
    }

    fun getMaxLineLength(): Int {
        val lines = splitAtLineBreak(ArrayList(styledText))
        return if (lines.isNotEmpty()) lines.maxOf { it.size } else 0
    }

    fun getScrollPane(uiManager: UIManager): CScrollPane {
        return CScrollPane(uiManager.themeManager, uiManager.scaleManager, true, this)
    }

    override fun isFocusable(): Boolean {
        return true
    }

    override fun getMinimumSize(): Dimension {
        val fontMetrics = this.getFontMetrics(this.font)
        val charWidth = fontMetrics.charWidth(' ')
        val lineHeight = fontMetrics.ascent + fontMetrics.descent
        return Dimension(this.getMaxLineLength() * charWidth, this.getLineCount() * lineHeight)
    }

    fun currLine(): Int {
        return splitAtLineBreak(styledText.subList(0, caretPos)).size
    }

    fun currColumn(): Int {
        return splitAtLineBreak(styledText.subList(0, caretPos)).lastOrNull()?.size ?: 0
    }

    fun getAbsSelection(): AbsSelection = if (selectionStart < selectionEnd) AbsSelection(selectionStart, selectionEnd) else AbsSelection(selectionEnd, selectionStart)

    data class StyledChar(val content: Char, val style: Style? = null)
    data class AbsSelection(val lowIndex: Int, val highIndex: Int)
    data class Style(val fgColor: Color? = null, val bgColor: Color? = null)

    inner class EditorKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent) {
            // Character Insertion
            when {
                e.keyChar.isISOControl() -> {

                }

                e.keyChar.isDefined() -> {
                    val newChar = e.keyChar.toString()
                    insertText(caretPos, newChar)
                }
            }
        }

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_ENTER -> {
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
            repaint()
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

        override fun mouseMoved(e: MouseEvent?) {
            super.mouseMoved(e)
        }
    }

}