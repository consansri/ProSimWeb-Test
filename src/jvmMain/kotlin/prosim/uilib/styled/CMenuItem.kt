package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JMenuItem
import javax.swing.border.Border

class CMenuItem(text: String = "", val fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    init {
        this.setUI(CMenuItemUI())
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getBorder(): Border {
        return UIStates.scale.get().BORDER_INSET_MEDIUM
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

}