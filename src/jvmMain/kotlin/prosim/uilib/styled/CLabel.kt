package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.Border

open class CLabel(content: String, val fontType: FontType) : JLabel(content) {

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
        return UIStates.scale.get().borderScale.getInsetBorder()
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().textLaF.base
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }
}