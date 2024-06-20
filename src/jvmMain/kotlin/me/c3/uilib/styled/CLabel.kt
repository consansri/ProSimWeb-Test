package me.c3.uilib.styled

import me.c3.uilib.styled.params.FontType
import java.awt.Color
import javax.swing.JLabel

open class CLabel( content: String, fontType: FontType) : JLabel(content) {

    init {
        this.setUI(CLabelUI( fontType))
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        foreground = color
    }
}