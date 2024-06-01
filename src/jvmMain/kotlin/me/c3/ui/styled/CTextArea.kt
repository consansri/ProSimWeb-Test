package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import javax.swing.JTextArea

class CTextArea( fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.THICKNESS): JTextArea() {

    init {
        this.setUI(CTextAreaUI( fontType))
    }

}