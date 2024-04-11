package me.c3.ui.styled

import me.c3.ui.components.styled.CPanel
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Panel
import java.awt.RenderingHints
import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPanelUI

class CPanelUI(private val roundCorners: Boolean) : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        c?.isOpaque = false
    }

    override fun paint(g: Graphics, c: JComponent?) {

        val g2d = g.create() as? Graphics2D
        g2d?.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        c?.let {
            g2d?.color = c.background
            if (roundCorners) {
                g2d?.fillRoundRect(0, 0, c.width, c.height, 10, 10)
            } else {
                g2d?.fillRect(0, 0, c.width, c.height)
            }
        }
        super.paint(g2d, c)


        g2d?.dispose()
    }

}