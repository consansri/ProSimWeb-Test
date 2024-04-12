package me.c3.ui.styled

import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Panel
import java.awt.RenderingHints
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
        cPanel.background = if (cPanel.primary) themeManager.curr.globalLaF.bgPrimary else themeManager.curr.globalLaF.bgSecondary
        cPanel.border = when (cPanel.borderMode) {
            CPanel.BorderMode.INSET -> scaleManager.curr.borderScale.getInsetBorder()
            CPanel.BorderMode.NORTH -> DirectionalBorder(themeManager, scaleManager, north = true)
            CPanel.BorderMode.SOUTH -> DirectionalBorder(themeManager, scaleManager, south = true)
            CPanel.BorderMode.WEST -> DirectionalBorder(themeManager, scaleManager, west = true)
            CPanel.BorderMode.EAST -> DirectionalBorder(themeManager, scaleManager, east = true)
            CPanel.BorderMode.NONE -> BorderFactory.createEmptyBorder()
        }
        cPanel.repaint()
    }

    override fun paint(g: Graphics, c: JComponent?) {
        val g2d = g.create() as? Graphics2D
        g2d?.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val cPanel = c as? CPanel

        cPanel?.let {
            g2d?.color = c.background
            if (cPanel.roundedCorners) {
                g2d?.fillRoundRect(0, 0, c.width, c.height, 10, 10)
            } else {
                g2d?.fillRect(0, 0, c.width, c.height)
            }
        }
        super.paint(g2d, c)

        g2d?.dispose()
    }



}