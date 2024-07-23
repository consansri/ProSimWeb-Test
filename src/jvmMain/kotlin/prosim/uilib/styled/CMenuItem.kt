package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JMenuItem
import javax.swing.border.Border

class CMenuItem(text: String, val fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    constructor(fontType: FontType) : this("", fontType)

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
        return UIStates.scale.get().controlScale.getNormalInsetBorder()
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().textLaF.base
    }

}