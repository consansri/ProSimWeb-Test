package me.c3.ui.styled

import me.c3.ui.components.styled.CTabbedPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTabbedPaneUI

class CTabbedPaneUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val primary: Boolean, private val fontType: FontType) : BasicTabbedPaneUI() {

    private var selectedColor = themeManager.curr.globalLaF.borderColor

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val pane = c as? CTabbedPane ?: return
        pane.border = BorderFactory.createEmptyBorder()

        themeManager.addThemeChangeListener {
            setDefaults(pane)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(pane)
        }
        setDefaults(pane)
    }

    private fun setDefaults(pane: CTabbedPane) {
        pane.background = if (primary) themeManager.curr.globalLaF.bgPrimary else themeManager.curr.globalLaF.bgSecondary
        pane.foreground = themeManager.curr.textLaF.base
        selectedColor = themeManager.curr.globalLaF.borderColor
        pane.font = fontType.getFont(themeManager, scaleManager)
        pane.repaint()
    }

    override fun paintTabBorder(g: Graphics?, tabPlacement: Int, tabIndex: Int, x: Int, y: Int, w: Int, h: Int, isSelected: Boolean) {
        if (isSelected) {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - scaleManager.curr.borderScale.markedThickness, w, scaleManager.curr.borderScale.markedThickness)
            g2d.dispose()
        } else {
            val g2d = g?.create() as? Graphics2D ?: return
            g2d.color = selectedColor
            g2d.fillRect(x, y + h - scaleManager.curr.borderScale.thickness, w, scaleManager.curr.borderScale.thickness)
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