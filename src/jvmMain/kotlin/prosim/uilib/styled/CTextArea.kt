package prosim.uilib.styled

import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import javax.swing.JTextArea

class CTextArea( fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.THICKNESS): JTextArea() {

    init {
        this.setUI(CTextAreaUI( fontType))
    }

}