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
        isOpaque = false
        background = Color(0,0,0,0)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            FontType.CODE.getFont()
        }
    }

    override fun getBorder(): Border {
        return try {
            borderMode.getBorder()
        } catch (e: NullPointerException){
            BorderMode.NONE.getBorder()
        }
    }

    override fun getForeground(): Color {
        return if (primary) UIStates.theme.get().COLOR_FG_0 else UIStates.theme.get().COLOR_FG_1
    }

    override fun getCaretColor(): Color {
        return  UIStates.theme.get().COLOR_FG_0
    }
}