package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTabbedPaneUI

class CTabbedPaneUI(private val tm: ThemeManager, private val sm: ScaleManager, private val primary: Boolean, private val fontType: FontType) : BasicTabbedPaneUI() {

    private var selectedColor = tm.curr.globalLaF.borderColor

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTabbedPane ?: return
        pane.border = BorderFactory.createEmptyBorder()

        tm.addThemeChangeListener {
            setDefaults(pane)
        }

        sm.addScaleChangeEvent {
            setDefaults(pane)
        }
        setDefaults(pane)
    }

    private fun setDefaults(pane: CTabbedPane) {
        pane.background = if (primary) tm.curr.globalLaF.bgPrimary else tm.curr.globalLaF.bgSecondary
        pane.foreground = tm.curr.textLaF.base
        selectedColor = tm.curr.globalLaF.borderColor
        pane.font = fontType.getFont(tm, sm)
        pane.repaint()
    }

    override fun paintTabBorder(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        if (isSelected) {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - sm.curr.borderScale.markedThickness, w, sm.curr.borderScale.markedThickness)
            g2d.dispose()
        } else {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - sm.curr.borderScale.thickness, w, sm.curr.borderScale.thickness)
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