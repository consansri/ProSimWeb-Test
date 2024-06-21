package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JComponent

open class CIconButtonUI() : CComponentUI<CIconButton>() {

    var cornerRadius = UIStates.scale.get().controlScale.cornerRadius
    var hoverColor = UIStates.theme.get().iconLaF.iconBgHover

    override fun onInstall(c: CIconButton) {
        c.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (c.hasHoverEffect) {
                    if (!c.isDeactivated) {
                        c.background = hoverColor
                    }
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                if (c.hasHoverEffect) {
                    c.background = c.iconBg
                }
            }
        })
    }

    override fun setDefaults(c: CIconButton, theme: Theme, scaling: Scaling, icons: Icons) {
        // Set Standard Appearance
        c.isFocusable = true
        c.isOpaque = false
        c.iconBg = UIStates.theme.get().iconLaF.iconBg
        cornerRadius = UIStates.scale.get().controlScale.cornerRadius
        hoverColor = UIStates.theme.get().iconLaF.iconBgHover
        val inset = c.mode.getInset()
        c.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CIconButton ?: return
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val inset = button.mode.getInset()

        val width = button.width
        val height = button.height

        // Paint button background
        val bgColor = if (button.isHovered && !button.isDeactivated) {
            g2.color = button.iconBgHover
        } else {
            g2.color = button.iconBg
        }
        g2.fillRoundRect(inset, inset, width - inset * 2, height - inset * 2, cornerRadius, cornerRadius)

        // Paint button
        val icon = button.getIcon()
        val iconX = (width - icon.iconWidth) / 2
        val iconY = (height - icon.iconHeight) / 2
        icon.paintIcon(button, g2, iconX, iconY)

        //super.paint(g, c)

        g2.dispose()
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? CIconButton ?: return super.getPreferredSize(c)
        val inset = button.mode.getInset()
        val scale = button.mode.size(UIStates.scale.get())
        return Dimension(scale + inset * 2, scale + inset * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }
}