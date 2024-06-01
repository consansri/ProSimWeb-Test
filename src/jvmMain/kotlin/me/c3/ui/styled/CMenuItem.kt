package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import javax.swing.JMenuItem

class CMenuItem( text: String, fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    constructor( fontType: FontType) : this( "", fontType)

    init {
        this.setUI(CMenuItemUI( fontType))
    }

}