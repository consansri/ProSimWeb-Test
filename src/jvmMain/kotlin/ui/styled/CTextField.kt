package me.c3.ui.styled

import me.c3.ui.UIManager
import javax.swing.JTextField

class CTextField(uiManager: UIManager, mode: CTextFieldUI.Type) : JTextField() {

    init {
        this.setUI(CTextFieldUI(uiManager, mode))
    }

}