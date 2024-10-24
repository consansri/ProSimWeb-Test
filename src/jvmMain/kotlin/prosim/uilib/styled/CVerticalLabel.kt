package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JLabel
import javax.swing.border.Border

class CVerticalLabel(text: String, val fontType: FontType, val primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI(primary, fontType))
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
        return if (primary) UIStates.theme.get().COLOR_FG_0 else UIStates.theme.get().COLOR_FG_1
    }

}