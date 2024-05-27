package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextField

class CTextField(tm: ThemeManager, sm: ScaleManager, fontType: FontType) : JTextField() {

    init {
        this.setUI(CTextFieldUI(tm, sm, fontType))
    }

}