package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CTabbedPaneUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.File
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

open class CTabbedPane(themeManager: ThemeManager, scaleManager: ScaleManager, val primary: Boolean) : JTabbedPane() {

    init {
        this.setUI(CTabbedPaneUI(themeManager, scaleManager, primary))
    }


    override fun paint(g: Graphics) {
        val g2d = g.create() as Graphics2D

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)
        g2d.dispose()
    }


}