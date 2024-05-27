package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPanelUI

class CPanelUI(private val tm: ThemeManager, private val sm: ScaleManager) : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        c?.isOpaque = false
        val cPanel = c as? CPanel ?: return

        tm.addThemeChangeListener {
            setDefaults(cPanel)
        }
        sm.addScaleChangeEvent {
            setDefaults(cPanel)
        }
        setDefaults(cPanel)
    }

    private fun setDefaults(cPanel: CPanel) {
        cPanel.background = Color(0, 0, 0, 0)
        cPanel.border = cPanel.borderMode.getBorder(tm, sm)

        if (cPanel.isOverlay) {
            cPanel.border = sm.curr.borderScale.getInsetBorder()
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

        g2d.color = if (cPanel.isOverlay) tm.curr.globalLaF.bgOverlay else if (cPanel.primary) tm.curr.globalLaF.bgPrimary else tm.curr.globalLaF.bgSecondary
        if (cPanel.roundedCorners) {
            g2d.fillRoundRect(0, 0, c.width, c.height, sm.curr.borderScale.cornerRadius, sm.curr.borderScale.cornerRadius)
        } else {
            g2d.fillRect(0, 0, c.width, c.height)
        }

        if (cPanel.isOverlay) {
            g2d.color = tm.curr.globalLaF.borderColor
            g2d.drawRoundRect(0, 0, c.width - 1, c.height - 1, sm.curr.borderScale.cornerRadius, sm.curr.borderScale.cornerRadius)
        }

        super.paint(g2d, c)

        g2d.dispose()
    }


}