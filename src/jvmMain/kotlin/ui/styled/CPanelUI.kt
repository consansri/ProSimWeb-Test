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

class CPanelUI() : BasicPanelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

    }

    override fun paint(g: Graphics?, c: JComponent?) {
        super.paint(g, c)

        val g2d = g?.create() as? Graphics2D ?: return
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        c?.let {
            g2d.color = c.background

            g2d.fillRoundRect(0,0, c.width, c.height, 10,10)
        }


        g2d.dispose()
    }

}