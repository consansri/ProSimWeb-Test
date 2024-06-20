package me.c3.uilib.styled

import me.c3.uilib.UIManager
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicPopupMenuUI

class CPopupMenuUI( private val fontType: FontType) : BasicPopupMenuUI() {

    private var cornerRadius = UIManager.scale.get().controlScale.cornerRadius
    private var borderColor = UIManager.theme.get().globalLaF.borderColor
    private var background = UIManager.theme.get().globalLaF.bgOverlay

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val optionPane = c as? CPopupMenu ?: return

        UIManager.theme.addEvent(WeakReference(optionPane)) { _ ->
            setDefaults(optionPane)
        }

        UIManager.scale.addEvent(WeakReference(optionPane)) { _ ->
            setDefaults(optionPane)
        }

        setDefaults(optionPane)
    }


    private fun setDefaults(cPopupMenu: CPopupMenu) {
        cornerRadius = UIManager.scale.get().controlScale.cornerRadius
        borderColor = UIManager.theme.get().globalLaF.borderColor
        background = UIManager.theme.get().globalLaF.bgOverlay
        cPopupMenu.isOpaque = false
        cPopupMenu.background = Color(0, 0, 0, 0)
        cPopupMenu.foreground = UIManager.theme.get().textLaF.base
        cPopupMenu.font = fontType.getFont()
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