package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.theme.core.Theme
import java.awt.*
import javax.swing.JComponent

class CTextButtonUI(private val fontType: FontType, private val borderMode: BorderMode) : CComponentUI<CTextButton>() {

    override fun setDefaults(c: CTextButton, theme: Theme, scaling: Scaling, icons: Icons) {
        c.isOpaque = false
        c.border = borderMode.getBorder()
        c.font = fontType.getFont()
        c.foreground = if (c.primary) theme.textLaF.base else theme.textLaF.baseSecondary
        c.background = Color(0, 0, 0, 0)
    }

    override fun onInstall(c: CTextButton) {
        // Nothing needs to be done
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

    override fun getPreferredSize(c: JComponent?): Dimension {
        return getMinimumSize(c)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        val button = c as? CTextButton ?: return super.getPreferredSize(c)
        val size = Dimension(
            button.getFontMetrics(fontType.getFont()).stringWidth(button.text) + c.insets.left + c.insets.right,
            button.getFontMetrics(fontType.getFont()).height + c.insets.top + c.insets.bottom
        )
        return size
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    private fun getCornerRadius(): Int = UIStates.scale.get().controlScale.cornerRadius
}