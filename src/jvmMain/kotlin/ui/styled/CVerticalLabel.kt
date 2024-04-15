package me.c3.ui.styled

import me.c3.ui.components.styled.CLabel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JLabel

class CVerticalLabel(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, text: String, primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI(themeManager, scaleManager, primary))
    }

}