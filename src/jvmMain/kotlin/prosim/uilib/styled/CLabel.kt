package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.Border

open class CLabel(content: String, val fontType: FontType, val borderMode: BorderMode = BorderMode.INSET) : JLabel(content) {

    var customFG: Color? = null
        set(value) {
            field = value
            repaint()
        }

    init {
        horizontalAlignment = SwingConstants.CENTER
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        customFG = color
    }

    override fun getBorder(): Border {
        return borderMode.getBorder()
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().COLOR_FG_0
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    override fun getPreferredSize(): Dimension {
        val fm = getFontMetrics(font)
        val width = fm.stringWidth(text) + insets.left + insets.right
        val height = fm.height + insets.top + insets.bottom
        return Dimension(width, height)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }
}