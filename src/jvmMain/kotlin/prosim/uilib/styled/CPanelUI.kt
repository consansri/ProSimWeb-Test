package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class CPanelUI() : ComponentUI() {

    override fun installUI(c: JComponent?) {
        c as? CPanel ?: return super.installUI(c)

        c.isOpaque = false
        c.background = Color(0, 0, 0, 0)
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

        c.paintComponents(g2d)

        g2d.dispose()
    }

}