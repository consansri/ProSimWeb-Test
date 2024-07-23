package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.border.Border

open class CTextField(val fontType: FontType, val primary: Boolean = false) : JTextField() {

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
        horizontalAlignment = SwingConstants.CENTER
    }

    constructor(text: String, fontType: FontType) : this(fontType) {
        this.text = text
    }

    override fun isOpaque(): Boolean {
        return true
    }

    override fun getBackground(): Color {
        return customBG ?: if(primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary
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

    override fun getCaretColor(): Color {
        return UIStates.theme.get().textLaF.base
    }

    override fun getBorder(): Border {
        return BorderFactory.createEmptyBorder()
    }

}