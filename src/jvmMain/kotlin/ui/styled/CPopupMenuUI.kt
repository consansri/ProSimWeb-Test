package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPopupMenuUI

class CPopupMenuUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager) : BasicPopupMenuUI() {

    private var cornerRadius = scaleManager.curr.controlScale.cornerRadius
    private var borderColor = themeManager.curr.globalLaF.borderColor
    private var background = themeManager.curr.globalLaF.bgOverlay

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val optionPane = c as? CPopupMenu ?: return

        themeManager.addThemeChangeListener {
            setDefaults(optionPane)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(optionPane)
        }

        setDefaults(optionPane)
    }


    private fun setDefaults(cPopupMenu: CPopupMenu) {
        cornerRadius = scaleManager.curr.controlScale.cornerRadius
        borderColor = themeManager.curr.globalLaF.borderColor
        background = themeManager.curr.globalLaF.bgOverlay
        cPopupMenu.isOpaque = false
        cPopupMenu.background = Color(0, 0, 0, 0)
        cPopupMenu.foreground = themeManager.curr.textLaF.base
        cPopupMenu.font = themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
        cPopupMenu.border = BorderFactory.createEmptyBorder()
    }

    override fun paint(g: Graphics?, c: JComponent?) {

        val pane = c as? CPopupMenu
        if (pane == null) {
            super.paint(g, c)
            return
        }

        val g2d = g?.create() as? Graphics2D
        if (g2d == null) {
            super.paint(g, c)
            return
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw Background
        g2d.color = background
        g2d.fillRoundRect(0, 0, pane.width - 1, pane.height - 1, cornerRadius, cornerRadius)

        // Draw border
        g2d.color = borderColor
        g2d.drawRoundRect(0, 0, pane.width - 1, pane.height - 1, cornerRadius, cornerRadius)

        // Paint Menu Items
        super.paint(g2d, pane)

        g2d.dispose()


    }

}