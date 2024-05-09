package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextField

class CTextField(themeManager: ThemeManager, scaleManager: ScaleManager, fontType: FontType) : JTextField() {

    init {
        this.setUI(CTextFieldUI(themeManager, scaleManager, fontType))
    }

}