package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JPopupMenu

class CPopupMenu(val fontType: FontType = FontType.BASIC) : JPopupMenu() {

    init {
        this.setUI(CPopupMenuUI())
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

}