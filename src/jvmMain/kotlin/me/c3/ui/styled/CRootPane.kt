package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JRootPane

class CRootPane(tm: ThemeManager, sm: ScaleManager) : JRootPane() {
    var cornerRadius = sm.curr.borderScale.cornerRadius
        set(value) {
            field = value
            (ui as? CRootPaneUI)?.cornerRadius = value
            repaint()
        }

    init {
        setUI(CRootPaneUI(tm, sm))
    }
}