package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JMenuItem

class CMenuItem(tm: ThemeManager, sm: ScaleManager, text: String, fontType: FontType = FontType.BASIC) : JMenuItem(text) {

    constructor(tm: ThemeManager, sm: ScaleManager, fontType: FontType) : this(tm, sm, "", fontType)

    init {
        this.setUI(CMenuItemUI(tm, sm, fontType))
    }

}