package me.c3.uilib.styled

import me.c3.ui.States
import java.awt.*
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JRootPane
import javax.swing.plaf.basic.BasicRootPaneUI

class CRootPaneUI() : BasicRootPaneUI() {

    private var inset: Int = States.scale.get().borderScale.insets
    var cornerRadius: Int = States.scale.get().borderScale.cornerRadius

    override fun installDefaults(c: JRootPane?) {
        super.installDefaults(c)

        val cRootPane = c as? CRootPane ?: return

        States.theme.addEvent(WeakReference(cRootPane)) { _ ->
            setDefaults(cRootPane)
        }

        States.scale.addEvent(WeakReference(cRootPane)) { _ ->
            setDefaults(cRootPane)
        }

        setDefaults(cRootPane)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
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

    private fun setDefaults(cRootPane: CRootPane) {
        inset = States.scale.get().borderScale.insets
        cRootPane.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
        cRootPane.background = States.theme.get().globalLaF.bgSecondary
        cRootPane.repaint()
    }

}