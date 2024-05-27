package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextArea

class CTextArea(tm: ThemeManager, sm: ScaleManager, fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.THICKNESS): JTextArea() {

    init {
        this.setUI(CTextAreaUI(tm, sm, fontType))
    }

}