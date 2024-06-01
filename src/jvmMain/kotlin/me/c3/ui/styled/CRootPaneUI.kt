package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JRootPane
import javax.swing.plaf.basic.BasicRootPaneUI

class CRootPaneUI() : BasicRootPaneUI() {

    private var inset: Int = ScaleManager.curr.borderScale.insets
    var cornerRadius: Int = ScaleManager.curr.borderScale.cornerRadius

    override fun installDefaults(c: JRootPane?) {
        super.installDefaults(c)

        val cRootPane = c as? CRootPane ?: return

        ThemeManager.addThemeChangeListener {
            setDefaults(cRootPane)
        }

        ScaleManager.addScaleChangeEvent {
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
        inset = ScaleManager.curr.borderScale.insets
        cRootPane.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)
        cRootPane.background = ThemeManager.curr.globalLaF.bgSecondary
        cRootPane.repaint()
    }

}