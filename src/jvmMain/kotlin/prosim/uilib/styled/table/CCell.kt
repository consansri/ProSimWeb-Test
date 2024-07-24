package prosim.uilib.styled.table

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JPanel

abstract class CCell(val fontType: FontType) : JPanel() {

    var customBG: Color? = null
        set(value) {
            field = value
            repaint()
        }

    var customFG: Color? = null
        set(value) {
            field = value
            repaint()
        }

    init {
        isOpaque = false
        border = BorderFactory.createEmptyBorder()
    }

    override fun paintComponent(g: Graphics?) {
        val g2d = g as? Graphics2D ?: return super.paintComponent(g)

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val fm = getFontMetrics(font)

        val string = textToDraw()

        val y = insets.top + fm.ascent
        val x = insets.left

        g2d.font = font
        g2d.color = background
        g2d.fillRect(0, 0, bounds.width, bounds.height)

        val contentBounds = fm.getStringBounds(string, g2d)
        preferredSize = contentBounds.bounds.size

        val offsetx = (bounds.width - contentBounds.bounds.width) / 2
        val offsetY = (bounds.height - contentBounds.bounds.height) / 2
        g2d.color = foreground

        g2d.drawString(string, x + offsetx, y + offsetY)
    }

    abstract fun textToDraw(): String

    override fun getMinimumSize(): Dimension {
        val string = textToDraw()
        val fm = getFontMetrics(font)
        val width = fm.stringWidth(string)
        val height = fm.height
        return Dimension(width, height)
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().COLOR_FG_0
    }

    override fun getBackground(): Color {
        return customBG ?: Color(0, 0, 0, 0)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            FontType.DATA.getFont()
        }
    }

}