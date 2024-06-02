package me.c3.ui.styled

import me.c3.ui.States
import javax.swing.JRootPane

class CRootPane() : JRootPane() {
    var cornerRadius = States.scale.get().borderScale.cornerRadius
        set(value) {
            field = value
            (ui as? CRootPaneUI)?.cornerRadius = value
            repaint()
        }

    init {
        setUI(CRootPaneUI())
    }
}