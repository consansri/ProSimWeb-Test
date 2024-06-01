package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import javax.swing.JTextField

class CTextField( fontType: FontType) : JTextField() {

    init {
        this.setUI(CTextFieldUI( fontType))
    }

}