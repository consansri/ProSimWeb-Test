package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.lang.ref.WeakReference
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class CTextButtonUI( private val fontType: FontType): BasicButtonUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CTextButton ?: return

        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.border = UIStates.scale.get().borderScale.getInsetBorder()

        // Apply hover effect
        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!button.isDeactivated) {
                    button.background = UIStates.theme.get().iconLaF.iconBgHover
                    button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                button.background = null // Reset background to default
            }
        })

        UIStates.theme.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }

        UIStates.scale.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }
        setDefaults(button)
    }

    fun setDefaults(button: CTextButton) {
        val currTheme = UIStates.theme.get()
        val currScale = UIStates.scale.get()
        button.border = UIStates.scale.get().borderScale.getInsetBorder()
        button.font = fontType.getFont()
        button.foreground = if (button.primary) currTheme.textLaF.base else currTheme.textLaF.baseSecondary
        button.background = Color(0, 0, 0, 0)
        button.repaint()
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? AbstractButton ?: return
        val g2 = g as? Graphics2D ?: return

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(getInset(), getInset(), width - getInset() * 2, height - getInset() * 2, getCornerRadius(), getCornerRadius())

        // Paint button
        super.paint(g, c)
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? AbstractButton ?: return super.getPreferredSize(c)
        val preferredSize = super.getPreferredSize(button)
        return Dimension(preferredSize.width + getInset() * 2, preferredSize.height + getInset() * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }
    private fun getCornerRadius(): Int = UIStates.scale.get().controlScale.cornerRadius
    private fun getInset(): Int = UIStates.scale.get().borderScale.insets

}