package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JLabel

class CVerticalLabel(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, text: String, fontType: FontType, primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI(themeManager, scaleManager, primary, fontType))
    }

}