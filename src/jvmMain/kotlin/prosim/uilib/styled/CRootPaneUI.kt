package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.*
import javax.swing.JComponent
import javax.swing.JRootPane
import javax.swing.plaf.basic.BasicRootPaneUI

class CRootPaneUI() : BasicRootPaneUI() {

    val cornerRadius: Int
        get() = UIStates.scale.get().SIZE_CORNER_RADIUS

    override fun installDefaults(c: JRootPane?) {
        super.installDefaults(c)

        val cRootPane = c as? CRootPane ?: return

        setDefaults(cRootPane)
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val bounds = c?.bounds ?: return
        g2d.color = c.background
        g2d.fillRoundRect(0, 0, bounds.width, bounds.height, cornerRadius, cornerRadius)

        val inset = c.insets.top

        g2d.stroke = BasicStroke(inset.toFloat())
        g2d.color = Color.RED
        g2d.drawRoundRect(inset, inset, bounds.width - 2 * inset, bounds.height - 2 * inset, cornerRadius, cornerRadius)

        super.paint(g2d, c)

        g2d.dispose()
    }

    private fun setDefaults(cRootPane: CRootPane) {

        cRootPane.repaint()
    }

}