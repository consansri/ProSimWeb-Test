package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JSplitPane
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.plaf.basic.BasicSplitPaneUI

class CSplitPaneUI() : BasicSplitPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CSplitPane ?: return
        pane.border = BorderFactory.createEmptyBorder()
        divider.border = BorderFactory.createEmptyBorder()
    }

    override fun createDefaultDivider(): BasicSplitPaneDivider {
        return CSplitPaneDivider()
    }

    private inner class CSplitPaneDivider() : BasicSplitPaneDivider(this) {
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

        override fun getBackground(): Color {
            return UIStates.theme.get().COLOR_BORDER
        }
    }
}