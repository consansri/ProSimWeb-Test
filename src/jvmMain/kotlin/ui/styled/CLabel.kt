package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CLabelUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import javax.swing.JLabel
import javax.swing.SwingUtilities

open class CLabel(themeManager: ThemeManager, scaleManager: ScaleManager, content: String) : JLabel(content) {

    init {
        this.setUI(CLabelUI(themeManager, scaleManager))
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        foreground = color
    }
}