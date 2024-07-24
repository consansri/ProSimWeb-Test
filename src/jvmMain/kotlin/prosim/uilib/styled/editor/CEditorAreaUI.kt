package prosim.uilib.styled.editor

import prosim.uilib.UIStates
import java.awt.*
import javax.swing.JComponent
import javax.swing.Timer
import javax.swing.plaf.ComponentUI

class CEditorAreaUI(
    private val lineOverhead: Int = 3
) : ComponentUI() {

    val selectionColor: Color
        get() = UIStates.theme.get().COLOR_SELECTION
    val searchResultColor: Color
        get() = UIStates.theme.get().COLOR_SEARCH_RESULT
    val defaultSelectionColor: Color
        get() = Color(selectionColor.red, selectionColor.green, selectionColor.blue, 127)
    val defaultSearchResultColor: Color
        get() = Color(searchResultColor.red, searchResultColor.green, searchResultColor.blue, 127)
    val caretLineBG: Color
        get() = Color(selectionColor.red, selectionColor.green, selectionColor.blue, 15)

    private var caretTimer: Timer? = null
    private var caretColor: Color? = null

    override fun installUI(c: JComponent?) {
        c as? CEditorArea ?: return super.installUI(c)

        // Setup Base Editor Defaults
        c.isOpaque = false
        c.focusTraversalKeysEnabled = false

        // Setup Sub Components
        c.scrollPane.verticalScrollBar.unitIncrement = c.getFontMetrics(c.font).height
        c.scrollPane.horizontalScrollBar.unitIncrement = c.getFontMetrics(c.font).charWidth(' ')
        c.lineNumbers.isOpaque = false

        caretColor = c.foreground
        // Setup Caret Timer
        caretTimer?.stop()
        caretTimer = null
        caretTimer = Timer(500) {
            caretColor = if (c.caret.isMoving || caretColor == null) {
                c.foreground
            } else {
                null
            }
            c.repaint()
        }
        caretTimer?.start()
    }


    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return
        val editor = c as? CEditorArea ?: return
        when (editor.location) {
            CEditorArea.Location.ANYWHERE -> paintAll(g2d, editor)
            CEditorArea.Location.IN_SCROLLPANE -> paintEfficient(g2d, editor)
        }
        g2d.dispose()
    }

    /**
     * Performance
     * (painting all)
     *
     * 223 ms for 3000 lines of code!
     */
    private fun paintAll(g2d: Graphics2D, editor: CEditorArea) {
        val fm = editor.getFontMetrics(editor.font)
        val styledText = editor.getStyledText()
        val absSelection = editor.getAbsSelection()

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val caretLine = editor.caret.getLineInfo().lineNumber
        val ascent = fm.ascent
        val lineHeight = fm.height

        var x = editor.insets.left
        var y = editor.insets.top + ascent
        var lineCounter = 0

        if (absSelection.lowIndex == -1 || absSelection.highIndex == -1) {
            g2d.color = caretLineBG
            g2d.fillRect(0, y + (caretLine - 1) * lineHeight - ascent, editor.width, lineHeight)
        }

        val charWidth = fm.charWidth(' ')

        // Render styled characters
        for ((i, char) in styledText.withIndex()) {
            val style = char.style

            // Draw Selection
            if (i in absSelection.lowIndex until absSelection.highIndex) {
                g2d.color = defaultSelectionColor
                if (char.content == '\n') {
                    g2d.fillRect(x, y - ascent, editor.bounds.width - x - editor.insets.right, lineHeight)
                } else {
                    g2d.fillRect(x, y - ascent, charWidth, lineHeight)
                }
            }

            // Draw the cursor
            if (i == editor.caret.getIndex()) {
                if (editor.hasFocus()) {
                    drawCaret(g2d, x, y, fm)
                }
            }

            // Draw Characters
            g2d.color = style?.fgColor ?: editor.foreground

            when (char.content) {
                '\n' -> {
                    x = editor.insets.left
                    y += lineHeight
                    lineCounter += 1
                }

                else -> {
                    g2d.drawString(char.content.toString(), x, y)
                    x += charWidth
                }
            }
        }

        if (editor.getStyledText().size == editor.caret.getIndex()) {
            if (editor.hasFocus()) {
                drawCaret(g2d, x, y, fm)
            }
        }
    }

    /**
     * Performance
     * (only painting visible area)
     *
     * 1 ms for 3000 lines of code!
     */
    private fun paintEfficient(g2d: Graphics2D, editor: CEditorArea) {
        val fm = editor.getFontMetrics(editor.font)
        val styledText = editor.getStyledText()
        val absSelection = editor.getAbsSelection()
        val lineBreakIDs = editor.getLineBreakIDs()
        val searchResults = editor.findAndReplace.getResults()

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Get the visible rectangle of the viewport
        val visibleRect = editor.scrollPane.viewport.viewRect

        val caretLine = editor.caret.getLineInfo().lineNumber
        val ascent = fm.ascent
        val lineHeight = fm.height

        var x = editor.insets.left
        var y = editor.insets.top + ascent
        var lineCounter = 0

        // Calculate the first and last visible line indices
        val firstVisibleLineID = visibleRect.y / lineHeight - 1
        val lastVisibleLineID = (visibleRect.y + visibleRect.height) / lineHeight

        val firstLineID = firstVisibleLineID - 1 - lineOverhead
        val lastLineID = lastVisibleLineID + lineOverhead

        val firstLineBreakPos = lineBreakIDs.getOrNull(firstLineID) ?: 0
        val firstPos = if (firstLineBreakPos == 0) 0 else firstLineBreakPos
        val lastPos = lineBreakIDs.getOrNull(lastLineID) ?: (styledText.size - 1)

        // Draw CurrentLine BG
        if (absSelection.lowIndex == -1 || absSelection.highIndex == -1) {
            g2d.color = caretLineBG
            g2d.fillRect(0, y + (caretLine - 1) * lineHeight - ascent, editor.width, lineHeight)
        }

        // Add y offset
        if (firstLineID >= 0) {
            y += firstLineID * lineHeight
        }

        // Iterate over only the visible lines
        for (index in firstPos..lastPos) {
            val char = styledText[index]
            val style = char.style
            val charWidth = fm.charWidth(char.content)

            // Draw SearchResultBackground
            val inResult = searchResults.firstOrNull { it.range.contains(index) }
            if (inResult != null) {
                g2d.color = defaultSearchResultColor
                if (char.content == '\n') {
                    g2d.fillRect(x, y - ascent, editor.bounds.width - x - editor.insets.right, lineHeight)
                } else {
                    g2d.fillRect(x, y - ascent, charWidth, lineHeight)
                }
            }

            // Draw Selection
            if (index in absSelection.lowIndex until absSelection.highIndex) {
                g2d.color = defaultSelectionColor
                if (char.content == '\n') {
                    g2d.fillRect(x, y - ascent, editor.bounds.width - x - editor.insets.right, lineHeight)
                } else {
                    g2d.fillRect(x, y - ascent, charWidth, lineHeight)
                }
            }

            // Draw the cursor
            if (index == editor.caret.getIndex()) {
                if (editor.hasFocus()) {
                    drawCaret(g2d, x, y, fm)
                }
            }

            // Draw Characters
            g2d.color = style?.fgColor ?: editor.foreground

            when (char.content) {
                '\n' -> {
                    x = editor.insets.left
                    y += lineHeight
                    lineCounter += 1
                }

                else -> {
                    g2d.drawString(char.content.toString(), x, y)
                    if (char.style?.underline != null) {
                        g2d.color = char.style.underline
                        val underlineY = y + 2
                        g2d.drawLine(x, underlineY, x + charWidth, underlineY)
                    }
                    x += charWidth
                }
            }
        }

        if (styledText.size == editor.caret.getIndex() && editor.hasFocus()) {
            drawCaret(g2d, x, y, fm)
        }
    }

    private fun drawCaret(g2d: Graphics2D, x: Int, y: Int, fm: FontMetrics) {
        caretColor?.let {
            g2d.color = it
            g2d.drawLine(x, y + fm.descent, x, y + fm.descent - fm.height)
        }
    }
}