package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JPopupMenu

class CPopupMenu(themeManager: ThemeManager, scaleManager: ScaleManager, fontType: FontType = FontType.BASIC) : JPopupMenu() {

    init {
        this.setUI(CPopupMenuUI(themeManager, scaleManager, fontType))
    }

}