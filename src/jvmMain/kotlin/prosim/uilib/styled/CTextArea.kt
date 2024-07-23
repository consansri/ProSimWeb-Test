package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JTextArea
import javax.swing.border.Border

class CTextArea(val fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.THICKNESS) : JTextArea() {

    init {
        this.setUI(CTextAreaUI(fontType))
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getBorder(): Border {
        return try {
            borderMode.getBorder()
        } catch (e: NullPointerException){
            super.getBorder()
        }
    }

    override fun getForeground(): Color {
        return if (primary) UIStates.theme.get().textLaF.base else UIStates.theme.get().textLaF.baseSecondary
    }

    override fun getCaretColor(): Color {
        return  UIStates.theme.get().textLaF.base
    }
}