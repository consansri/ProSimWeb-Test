package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CTextButtonUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.SwingUtilities

open class CTextButton(themeManager: ThemeManager, scaleManager: ScaleManager, text: String) : JButton(text) {
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
        this.setUI(CTextButtonUI(themeManager, scaleManager))
    }




}