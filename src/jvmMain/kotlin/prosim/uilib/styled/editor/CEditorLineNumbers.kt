package prosim.uilib.styled.editor

import prosim.uilib.UIStates
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class CEditorLineNumbers( private val editor: CEditorArea) : JComponent() {

    var fm: FontMetrics = getFontMetrics(UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.codeSize))
    var selBg = Color(0, 0, 0, 0)
    var lineCount: Int = 1
        set(value) {
            field = value
            maxWidth = fm.stringWidth(value.toString().padStart(minLineCount, ' '))
            revalidate()
            repaint()
        }

    private val minLineCount = 3
    private var maxWidth = fm.stringWidth(lineCount.toString().padStart(minLineCount, ' '))

    private val lineContent = mutableListOf<LineContent>()

    fun mark(vararg content: LineContent) {
        lineContent.clear()
        lineContent.addAll(content)
        revalidate()
        repaint()
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        g2d.color = foreground
        g2d.drawLine(width - 1, 0, width - 1, height)

        val maxDigits = lineCount.toString().padStart(minLineCount, ' ').length
        val maxContentWidth = if (lineContent.isNotEmpty()) {
            lineContent.maxOf { it.getWidth(fm) }
        } else {
            0
        }

        var x = insets.left
        var y = insets.top + fm.ascent

        g2d.color = selBg
        g2d.fillRect(0, y + (editor.caret.getLineInfo().lineNumber - 1) * fm.height - fm.ascent, width, fm.height)

        g2d.color = foreground
        for (i in 1..lineCount) {
            val content = lineContent.firstOrNull { it.line == i }
            content?.let {
                g2d.color = it.color
                if (content is LineContent.Text) {
                    g2d.drawString(content.text, x, y)
                }
                g2d.color = foreground
            }

            x += maxContentWidth

            val paddedNumber = i.toString().padStart(maxDigits, ' ')
            g2d.drawString(paddedNumber, x, y)

            x = insets.left
            y += fm.height
        }

        g2d.dispose()
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(preferredSize.width, editor.preferredSize.height)
    }

    override fun getPreferredSize(): Dimension {
        return if (lineContent.isNotEmpty()) {
            Dimension(lineContent.maxOf { it.getWidth(fm) } + maxWidth + insets.left + insets.right, editor.preferredSize.height)
        } else {
            Dimension(maxWidth + insets.left + insets.right * 2, editor.preferredSize.height)
        }
    }

    sealed class LineContent(val line: Int, val color: Color) {
        abstract fun getWidth(fm: FontMetrics): Int
        class Text(line: Int, color: Color, val text: String) : LineContent(line, color) {
            override fun getWidth(fm: FontMetrics): Int = fm.stringWidth(text)
        }
    }

    abstract class LineClickListener(private val lineNumbers: CEditorLineNumbers) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            val line = (e.y - lineNumbers.insets.top) / lineNumbers.fm.height
            lineClick(line)
        }

        abstract fun lineClick(lineNumber: Int)

    }

}