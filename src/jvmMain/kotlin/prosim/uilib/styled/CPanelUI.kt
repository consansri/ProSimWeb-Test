package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

class CPanelUI() : CComponentUI<CPanel>() {

    override fun setDefaults(c: CPanel, theme: Theme, scaling: Scaling, icons: Icons) {
        c.isOpaque = false
        c.background = Color(0, 0, 0, 0)
    }

    override fun onInstall(c: CPanel) {
        // nothing needs to be installed
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