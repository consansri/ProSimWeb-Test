package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JPopupMenu

class CPopupMenu( fontType: FontType = FontType.BASIC) : JPopupMenu() {

    init {
        this.setUI(CPopupMenuUI( fontType))
    }

}