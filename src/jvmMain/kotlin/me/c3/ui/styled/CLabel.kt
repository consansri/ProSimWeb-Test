package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
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