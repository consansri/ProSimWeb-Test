package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JMenuItem

class CMenuItem(themeManager: ThemeManager, scaleManager: ScaleManager, text: String, fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    constructor(themeManager: ThemeManager, scaleManager: ScaleManager, fontType: FontType) : this(themeManager, scaleManager, "", fontType)

    init {
        this.setUI(CMenuItemUI(themeManager, scaleManager, fontType))
    }

}