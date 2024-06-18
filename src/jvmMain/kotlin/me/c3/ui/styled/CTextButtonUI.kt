package me.c3.ui.styled

import me.c3.ui.States
import me.c3.ui.styled.params.FontType
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
        button.border = States.scale.get().borderScale.getInsetBorder()

        // Apply hover effect
        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!button.isDeactivated) {
                    button.background = States.theme.get().iconLaF.iconBgHover
                    button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                button.background = null // Reset background to default
            }
        })

        States.theme.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }

        States.scale.addEvent(WeakReference(button)) { _ ->
            setDefaults(button)
        }
        setDefaults(button)
    }

    fun setDefaults(button: CTextButton) {
        val currTheme = States.theme.get()
        val currScale = States.scale.get()
        button.border = States.scale.get().borderScale.getInsetBorder()
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
    private fun getCornerRadius(): Int = States.scale.get().controlScale.cornerRadius
    private fun getInset(): Int = States.scale.get().borderScale.insets

}