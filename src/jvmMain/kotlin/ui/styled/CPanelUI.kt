package me.c3.ui.styled

import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.*
import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPanelUI

class CPanelUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager) : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        c?.isOpaque = false
        val cPanel = c as? CPanel ?: return

        themeManager.addThemeChangeListener {
            setDefaults(cPanel)
        }
        scaleManager.addScaleChangeEvent {
            setDefaults(cPanel)
        }
        setDefaults(cPanel)
    }

    private fun setDefaults(cPanel: CPanel) {
        cPanel.background = Color(0, 0, 0, 0)
        cPanel.border = when (cPanel.borderMode) {
            CPanel.BorderMode.INSET -> scaleManager.curr.borderScale.getInsetBorder()
            CPanel.BorderMode.BASIC -> scaleManager.curr.borderScale.getThicknessBorder()
            CPanel.BorderMode.NORTH -> DirectionalBorder(themeManager, scaleManager, north = true)
            CPanel.BorderMode.SOUTH -> DirectionalBorder(themeManager, scaleManager, south = true)
            CPanel.BorderMode.WEST -> DirectionalBorder(themeManager, scaleManager, west = true)
            CPanel.BorderMode.EAST -> DirectionalBorder(themeManager, scaleManager, east = true)
            CPanel.BorderMode.NONE -> BorderFactory.createEmptyBorder()
        }

        if (cPanel.isOverlay) {
            cPanel.border = scaleManager.curr.borderScale.getInsetBorder()
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

        g2d.color = if (cPanel.isOverlay) themeManager.curr.globalLaF.bgOverlay else if (cPanel.primary) themeManager.curr.globalLaF.bgPrimary else themeManager.curr.globalLaF.bgSecondary
        if (cPanel.roundedCorners) {
            g2d.fillRoundRect(0, 0, c.width, c.height, scaleManager.curr.borderScale.cornerRadius, scaleManager.curr.borderScale.cornerRadius)
        } else {
            g2d.fillRect(0, 0, c.width, c.height)
        }

        if (cPanel.isOverlay) {
            g2d.color = themeManager.curr.globalLaF.borderColor
            g2d.drawRoundRect(0, 0, c.width - 1, c.height - 1, scaleManager.curr.borderScale.cornerRadius, scaleManager.curr.borderScale.cornerRadius)
        }

        super.paint(g2d, c)

        g2d.dispose()
    }


}