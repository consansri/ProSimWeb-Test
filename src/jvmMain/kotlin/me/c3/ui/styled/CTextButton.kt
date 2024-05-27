package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CTextButtonUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JButton

open class CTextButton(tm: ThemeManager, sm: ScaleManager, text: String, fontType: FontType) : JButton(text) {
    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var primary = true
        set(value) {
            field = value
            (ui as? CTextButtonUI)?.setDefaults(this)
        }

    init {
        this.setUI(CTextButtonUI(tm, sm, fontType))
    }




}