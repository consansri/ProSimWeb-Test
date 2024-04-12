package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JTextField

class CTextField(themeManager: ThemeManager, scaleManager: ScaleManager, mode: CTextFieldUI.Type) : JTextField() {

    init {
        this.setUI(CTextFieldUI(themeManager, scaleManager, mode))
    }

}