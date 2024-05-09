package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JRootPane

class CRootPane(themeManager: ThemeManager, scaleManager: ScaleManager) : JRootPane() {
    var cornerRadius = scaleManager.curr.borderScale.cornerRadius
        set(value) {
            field = value
            (ui as? CRootPaneUI)?.cornerRadius = value
            repaint()
        }

    init {
        setUI(CRootPaneUI(themeManager, scaleManager))
    }
}