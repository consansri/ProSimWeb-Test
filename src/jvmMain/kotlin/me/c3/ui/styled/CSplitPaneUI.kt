package me.c3.ui.styled

import me.c3.ui.States
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.plaf.basic.BasicSplitPaneUI

class CSplitPaneUI() : BasicSplitPaneUI() {
    var dividerColor: Color = Color(0, 0, 0, 0)
        set(value) {
            field = value
            divider.background = value
            splitPane.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CSplitPane ?: return
        pane.border = BorderFactory.createEmptyBorder()
        divider.border = BorderFactory.createEmptyBorder()

        States.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        States.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        setDefaults(pane)
    }

    private fun setDefaults(cPane: CSplitPane) {
        dividerColor = States.theme.get().globalLaF.borderColor
        cPane.setDividerSize(States.scale.get().dividerScale.thickness)
    }

    override fun createDefaultDivider(): BasicSplitPaneDivider {
        return CSplitPaneDivider()
    }

    inner class CSplitPaneDivider() : BasicSplitPaneDivider(this) {
        override fun setPreferredSize(size: Dimension?) {
            super.setPreferredSize(size)
            // Adjust the preferred size of the divider
            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                super.setPreferredSize(Dimension(size?.width ?: 0, dividerSize))
            } else {
                super.setPreferredSize(Dimension(dividerSize, size?.height ?: 0))
            }
        }

        override fun paint(g: Graphics?) {
            super.paint(g)

            g?.let {
                // Customize the appearance of the divider
                g.color = background // Set your desired color
                g.fillRect(0, 0, width, height)
            }
        }
    }
}