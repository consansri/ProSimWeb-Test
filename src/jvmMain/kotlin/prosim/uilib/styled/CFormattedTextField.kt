package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JFormattedTextField

class CFormattedTextField(val fontType: FontType): JFormattedTextField() {
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
        setUI(CFormattedTextFieldUI(fontType))
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
            super.getFont()
        }
    }

    override fun getCaretColor(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

}