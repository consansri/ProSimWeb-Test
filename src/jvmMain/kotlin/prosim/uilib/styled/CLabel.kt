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

open class CLabel(content: String, val fontType: FontType, val borderMode: BorderMode = BorderMode.MEDIUM) : JLabel(content) {

    var customFG: Color? = null

    var customBG: Color? = null

    init {
        horizontalAlignment = SwingConstants.CENTER
        isFocusable = false
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        customFG = color
    }

    override fun getBorder(): Border {
        return borderMode.getBorder()
    }

    override fun getBackground(): Color {
        return customBG ?: Color(0, 0, 0, 0)
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().COLOR_FG_0
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    override fun getPreferredSize(): Dimension {
        val size = super.getPreferredSize()
        val insets = insets
        return Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }
}