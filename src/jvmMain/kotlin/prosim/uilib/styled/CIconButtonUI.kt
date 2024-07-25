package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

open class CIconButtonUI() : ComponentUI() {
    override fun installUI(c: JComponent?) {
        c as? CIconButton ?: return super.installUI(c)

        c.isFocusable = true
        c.isOpaque = false

    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? CIconButton ?: return
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val inset = button.insets

        val width = button.width
        val height = button.height

        g2.rotate(button.rotationAngle, width / 2.0, height / 2.0)

        // Paint button background
        if (button.isHovered && !button.isDeactivated) {
            g2.color = UIStates.theme.get().COLOR_ICON_BG_HOVER
        } else {
            g2.color = button.iconBg
        }
        g2.fillRoundRect(inset.left / 2, inset.top / 2, width - inset.left / 2 - inset.right / 2, height - inset.top / 2 - inset.bottom / 2, UIStates.scale.get().SIZE_CORNER_RADIUS, UIStates.scale.get().SIZE_CORNER_RADIUS)

        // Paint button
        val icon = button.svgIcon
        val iconX = (width - icon.iconWidth) / 2
        val iconY = (height - icon.iconHeight) / 2
        icon.paintIcon(button, g2, iconX, iconY)

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