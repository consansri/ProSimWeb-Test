package me.c3.uilib.styled

import me.c3.ui.States
import me.c3.uilib.styled.params.FontType
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTabbedPaneUI

class CTabbedPaneUI( private val primary: Boolean, private val fontType: FontType) : BasicTabbedPaneUI() {

    private var selectedColor = States.theme.get().globalLaF.borderColor

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTabbedPane ?: return
        pane.border = BorderFactory.createEmptyBorder()

        States.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        States.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }
        setDefaults(pane)
    }

    private fun setDefaults(pane: CTabbedPane) {
        pane.background = if (primary) States.theme.get().globalLaF.bgPrimary else States.theme.get().globalLaF.bgSecondary
        pane.foreground = States.theme.get().textLaF.base
        selectedColor = States.theme.get().globalLaF.borderColor
        pane.font = fontType.getFont()
        pane.repaint()
    }

    override fun paintTabBorder(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        if (isSelected) {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - States.scale.get().borderScale.markedThickness, w, States.scale.get().borderScale.markedThickness)
            g2d.dispose()
        } else {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - States.scale.get().borderScale.thickness, w, States.scale.get().borderScale.thickness)
            g2d.dispose()
        }
    }

    override fun paintContentBorder(g: Graphics?, tabPlacement: Int, selectedIndex: Int) {

    }

    override fun paintTabBackground(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {

    }

    override fun paintFocusIndicator(g: Graphics?, tabPlacement: Int, rects: Array<out Rectangle>?, tabIndex: Int, iconRect: Rectangle?, textRect: Rectangle?, isSelected: Boolean) {

    }
}