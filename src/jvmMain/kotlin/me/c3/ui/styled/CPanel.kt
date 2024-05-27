package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.CPanelUI
import me.c3.ui.theme.ThemeManager
import javax.swing.JPanel

open class CPanel(tm: ThemeManager, sm: ScaleManager, primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false, val isOverlay: Boolean = false) : JPanel() {

    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            repaint()
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            repaint()
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CPanelUI(tm, sm))
    }


}