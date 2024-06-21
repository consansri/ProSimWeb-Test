package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JLabel

class CVerticalLabel( text: String, fontType: FontType, primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI( primary, fontType))
    }

}