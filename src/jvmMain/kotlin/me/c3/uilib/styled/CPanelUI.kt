package me.c3.uilib.styled

import me.c3.uilib.UIStates
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPanelUI

class CPanelUI() : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        c?.isOpaque = false
        val cPanel = c as? CPanel ?: return

        UIStates.theme.addEvent(WeakReference(cPanel)) { _ ->
            setDefaults(cPanel)
        }
        UIStates.scale.addEvent(WeakReference(cPanel)) { _ ->
            setDefaults(cPanel)
        }
        setDefaults(cPanel)
    }

    private fun setDefaults(cPanel: CPanel) {
        cPanel.background = Color(0, 0, 0, 0)
        cPanel.border = cPanel.borderMode.getBorder()

        if (cPanel.isOverlay) {
            cPanel.border = UIStates.scale.get().borderScale.getInsetBorder()
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

        g2d.color = if (cPanel.isOverlay) UIStates.theme.get().globalLaF.bgOverlay else if (cPanel.primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary
        if (cPanel.roundedCorners) {
            g2d.fillRoundRect(0, 0, c.width, c.height, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)
        } else {
            g2d.fillRect(0, 0, c.width, c.height)
        }

        if (cPanel.isOverlay) {
            g2d.color = UIStates.theme.get().globalLaF.borderColor
            g2d.drawRoundRect(0, 0, c.width - 1, c.height - 1, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)
        }

        super.paint(g2d, c)

        g2d.dispose()
    }


}