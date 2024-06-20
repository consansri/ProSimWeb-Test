package me.c3.uilib.styled

import me.c3.uilib.UIManager
import javax.swing.JRootPane

class CRootPane() : JRootPane() {
    var cornerRadius = UIManager.scale.get().borderScale.cornerRadius
        set(value) {
            field = value
            (ui as? CRootPaneUI)?.cornerRadius = value
            repaint()
        }

    init {
        setUI(CRootPaneUI())
    }
}