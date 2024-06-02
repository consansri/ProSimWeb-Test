package me.c3.ui.styled

import javax.swing.JPanel
import javax.swing.border.AbstractBorder

open class CRawPanel( border: AbstractBorder? = null): JPanel() {

    init {
        this.setUI(CRawPanelUI( border))
    }
}