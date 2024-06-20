package me.c3.uilib.styled

import me.c3.uilib.styled.params.BorderMode
import me.c3.uilib.styled.params.FontType
import javax.swing.JTextArea

class CTextArea( fontType: FontType, val primary: Boolean = true, val borderMode: BorderMode = BorderMode.THICKNESS): JTextArea() {

    init {
        this.setUI(CTextAreaUI( fontType))
    }

}