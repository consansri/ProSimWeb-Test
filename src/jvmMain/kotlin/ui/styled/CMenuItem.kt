package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JMenuItem

class CMenuItem(themeManager: ThemeManager, scaleManager: ScaleManager, text: String) : JMenuItem(text) {

    constructor(themeManager: ThemeManager, scaleManager: ScaleManager) : this(themeManager, scaleManager, "")

    init {
        this.setUI(CMenuItemUI(themeManager, scaleManager))
    }

}