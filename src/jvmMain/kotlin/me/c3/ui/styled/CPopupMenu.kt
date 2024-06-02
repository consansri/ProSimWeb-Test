package me.c3.ui.styled

import me.c3.ui.styled.params.FontType
import javax.swing.JPopupMenu

class CPopupMenu( fontType: FontType = FontType.BASIC) : JPopupMenu() {

    init {
        this.setUI(CPopupMenuUI( fontType))
    }

}