package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import javax.swing.JPanel
import javax.swing.border.AbstractBorder

open class CRawPanel( border: AbstractBorder? = null): JPanel() {

    init {
        this.setUI(CRawPanelUI( border))
    }
}