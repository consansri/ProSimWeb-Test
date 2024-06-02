package me.c3.ui.styled

import me.c3.ui.styled.params.FontType
import javax.swing.JLabel

class CVerticalLabel( text: String, fontType: FontType, primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI( primary, fontType))
    }

}