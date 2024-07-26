package prosim.uilib.styled.tabbed

import prosim.uilib.UIStates
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTabbedPaneUI

class CTabbedPaneUI : BasicTabbedPaneUI() {

    private val selectedColor
        get() = UIStates.theme.get().COLOR_BORDER

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTabbedPane ?: return
        pane.border = BorderFactory.createEmptyBorder()
        tabInsets = Insets(0, 0, 0, 0)
    }

    override fun paintTabBorder(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        if (isSelected) {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - UIStates.scale.get().SIZE_BORDER_THICKNESS_MARKED, w, UIStates.scale.get().SIZE_BORDER_THICKNESS_MARKED)
            g2d.dispose()
        } else {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - UIStates.scale.get().SIZE_BORDER_THICKNESS, w, UIStates.scale.get().SIZE_BORDER_THICKNESS)
            g2d.dispose()
        }
    }

    override fun paintContentBorder(g: Graphics?, tabPlacement: Int, selectedIndex: Int) {}

    override fun paintTabBackground(g: Graphics, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        g.color = tabPane.background
        g.fillRect(x, y, w, h)
    }

    override fun paintFocusIndicator(g: Graphics?, tabPlacement: Int, rects: Array<out Rectangle>?, tabIndex: Int, iconRect: Rectangle?, textRect: Rectangle?, isSelected: Boolean) {}
}