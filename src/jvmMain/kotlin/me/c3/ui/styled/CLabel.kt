package me.c3.ui.components.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CLabelUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JLabel

open class CLabel(themeManager: ThemeManager, scaleManager: ScaleManager, content: String, fontType: FontType) : JLabel(content) {

    init {
        this.setUI(CLabelUI(themeManager, scaleManager, fontType))
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        foreground = color
    }
}