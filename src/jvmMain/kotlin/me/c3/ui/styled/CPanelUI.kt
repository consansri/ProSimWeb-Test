package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import java.awt.*
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPanelUI

class CPanelUI() : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        c?.isOpaque = false
        val cPanel = c as? CPanel ?: return

        ThemeManager.addThemeChangeListener {
            setDefaults(cPanel)
        }
        ScaleManager.addScaleChangeEvent {
            setDefaults(cPanel)
        }
        setDefaults(cPanel)
    }

    private fun setDefaults(cPanel: CPanel) {
        cPanel.background = Color(0, 0, 0, 0)
        cPanel.border = cPanel.borderMode.getBorder()

        if (cPanel.isOverlay) {
            cPanel.border = ScaleManager.curr.borderScale.getInsetBorder()
        }

        cPanel.repaint()
    }

    override fun paint(g: Graphics, c: JComponent?) {

        val g2d = g.create() as? Graphics2D
        if (g2d == null) {
            super.paint(g, c)
            return
        }

        val cPanel = c as? CPanel
        if (cPanel == null) {
            super.paint(g, c)
            return
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.color = if (cPanel.isOverlay) ThemeManager.curr.globalLaF.bgOverlay else if (cPanel.primary) ThemeManager.curr.globalLaF.bgPrimary else ThemeManager.curr.globalLaF.bgSecondary
        if (cPanel.roundedCorners) {
            g2d.fillRoundRect(0, 0, c.width, c.height, ScaleManager.curr.borderScale.cornerRadius, ScaleManager.curr.borderScale.cornerRadius)
        } else {
            g2d.fillRect(0, 0, c.width, c.height)
        }

        if (cPanel.isOverlay) {
            g2d.color = ThemeManager.curr.globalLaF.borderColor
            g2d.drawRoundRect(0, 0, c.width - 1, c.height - 1, ScaleManager.curr.borderScale.cornerRadius, ScaleManager.curr.borderScale.cornerRadius)
        }

        super.paint(g2d, c)

        g2d.dispose()
    }


}