package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JLabel

class CVerticalLabel(private val tm: ThemeManager, private val sm: ScaleManager, text: String, fontType: FontType, primary: Boolean = true) : JLabel(text) {

    init {
        this.setUI(CVerticalLabelUI(tm, sm, primary, fontType))
    }

}