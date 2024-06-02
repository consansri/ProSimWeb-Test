package me.c3.ui.styled

import me.c3.ui.styled.params.FontType
import javax.swing.JTextField

class CTextField(fontType: FontType) : JTextField() {

    init {
        this.setUI(CTextFieldUI(fontType))
    }

    constructor(text: String, fontType: FontType) : this(fontType) {
        this.text = text
    }
}