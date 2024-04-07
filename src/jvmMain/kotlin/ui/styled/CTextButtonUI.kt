package me.c3.ui.styled

import me.c3.ui.components.styled.CTextButton
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class CTextButtonUI: BasicButtonUI() {

    companion object {
        const val INSET = 2
        const val CORNER_RADIUS = 10
        val HOVER_COLOR = Color(0x55777777, true)
    }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CTextButton ?: return

        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.border = BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET)

        // Apply hover effect
        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!button.isDeactivated) {
                    button.background = HOVER_COLOR
                    button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                button.background = null // Reset background to default
            }
        })
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? AbstractButton ?: return
        val g2 = g as? Graphics2D ?: return

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(INSET, INSET, width - INSET * 2, height - INSET * 2, CORNER_RADIUS, CORNER_RADIUS)

        // Paint button
        super.paint(g, c)
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? AbstractButton ?: return super.getPreferredSize(c)
        val preferredSize = super.getPreferredSize(button)
        return Dimension(preferredSize.width + INSET * 2, preferredSize.height + INSET * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

}