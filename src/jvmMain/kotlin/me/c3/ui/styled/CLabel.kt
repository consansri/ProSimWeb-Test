package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CLabelUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JLabel

open class CLabel(tm: ThemeManager, sm: ScaleManager, content: String, fontType: FontType) : JLabel(content) {

    init {
        this.setUI(CLabelUI(tm, sm, fontType))
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        foreground = color
    }
}