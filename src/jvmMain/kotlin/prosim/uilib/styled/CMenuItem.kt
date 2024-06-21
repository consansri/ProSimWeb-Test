package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.JMenuItem

class CMenuItem( text: String, fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    constructor( fontType: FontType) : this( "", fontType)

    init {
        this.setUI(CMenuItemUI( fontType))
    }

}