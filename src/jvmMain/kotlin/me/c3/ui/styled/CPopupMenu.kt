package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JPopupMenu

class CPopupMenu(tm: ThemeManager, sm: ScaleManager, fontType: FontType = FontType.BASIC) : JPopupMenu() {

    init {
        this.setUI(CPopupMenuUI(tm, sm, fontType))
    }

}