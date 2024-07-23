package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class CTextButtonUI() : ComponentUI() {

    override fun installUI(c: JComponent?) {
        c as? CTextButton ?: return super.installUI(c)

        c.isOpaque = false
        c.background = Color(0, 0, 0, 0)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        if (c !is CTextButton) return super.paint(g, c)
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val width = c.width
        val height = c.height

        // Paint button background
        if (c.isHovered) {
            g2.color = UIStates.theme.get().iconLaF.iconBgHover
            g2.fillRoundRect(c.insets.left / 2, c.insets.top / 2, width - c.insets.right / 2 - c.insets.left / 2, height - c.insets.bottom / 2 - c.insets.top / 2, getCornerRadius(), getCornerRadius())
        }

        val fm = c.getFontMetrics(c.font)

        val ascent = fm.ascent

        val stringWidth = fm.stringWidth(c.text)
        val x = c.insets.left + (c.width - c.insets.left - c.insets.right - stringWidth) / 2
        val y = c.insets.top + ascent

        // Paint button text
        g2.color = c.foreground
        g2.font = c.font
        g2.drawString(c.text, x, y)
    }

    private fun getCornerRadius(): Int = UIStates.scale.get().controlScale.cornerRadius
}