package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.AbstractBorder

open class CRawPanel(themeManager: ThemeManager, scaleManager: ScaleManager, border: AbstractBorder? = null): JPanel() {

    init {
        this.setUI(CRawPanelUI(themeManager, scaleManager, border))
    }
}