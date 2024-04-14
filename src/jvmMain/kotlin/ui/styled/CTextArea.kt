package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextArea

class CTextArea(themeManager: ThemeManager, scaleManager: ScaleManager, areaType: CTextAreaUI.AreaType): JTextArea() {

    init {
        this.setUI(CTextAreaUI(themeManager, scaleManager, areaType))
    }

}