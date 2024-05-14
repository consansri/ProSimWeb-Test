package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextArea

class CTextArea(themeManager: ThemeManager, scaleManager: ScaleManager, fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.BASIC): JTextArea() {

    init {
        this.setUI(CTextAreaUI(themeManager, scaleManager, fontType))
    }

}