package me.c3.ui.styled

import emulator.kit.nativeLog
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JRootPane
import javax.swing.border.EmptyBorder
import javax.swing.plaf.basic.BasicRootPaneUI

class CRootPaneUI(private val inset: Int, private val cornerRadius: Int) : BasicRootPaneUI() {

    override fun installDefaults(c: JRootPane?) {
        super.installDefaults(c)
        c?.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
        c?.repaint()
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        nativeLog("Paint RootPane background!")
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val bounds = c?.bounds ?: return
        g2d.color = c.background
        g2d.fillRoundRect(0, 0, bounds.width, bounds.height, cornerRadius, cornerRadius)

        g2d.stroke = BasicStroke(inset.toFloat())
        g2d.color = Color.RED
        g2d.drawRoundRect(inset, inset, bounds.width - 2 * inset, bounds.height - 2 * inset, cornerRadius, cornerRadius)

        super.paint(g2d, c)

        g2d.dispose()
    }


}