package me.c3.ui.theme.core.components

import me.c3.ui.components.styled.CTabbedPane
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTabbedPaneUI

class CTabbedPaneUI : BasicTabbedPaneUI() {

    var underlineThickness = 4
        set(value) {
            field = value
            tabPane.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTabbedPane ?: return

        pane.border = BorderFactory.createEmptyBorder()
    }

    override fun paintTabBorder(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        if (isSelected) {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = (tabPane as? CTabbedPane)?.selectedColor
            g2d.fillRect(x, y + h - underlineThickness, w, underlineThickness)
            g2d.dispose()
        }
    }

    override fun paintContentBorder(g: Graphics?, tabPlacement: Int, selectedIndex: Int) {}

    override fun paintTabBackground(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {

    }

    override fun paintFocusIndicator(g: Graphics?, tabPlacement: Int, rects: Array<out Rectangle>?, tabIndex: Int, iconRect: Rectangle?, textRect: Rectangle?, isSelected: Boolean) {

    }


}