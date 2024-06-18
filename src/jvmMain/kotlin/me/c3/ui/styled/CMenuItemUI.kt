package me.c3.ui.styled

import me.c3.ui.States
import me.c3.ui.styled.params.FontType
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.lang.ref.WeakReference
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicMenuItemUI

class CMenuItemUI(private val fontType: FontType) : BasicMenuItemUI() {

    private var cornerRadius = States.scale.get().controlScale.cornerRadius
    private var hoverBackground = States.theme.get().iconLaF.iconBgHover

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val optionPane = c as? CMenuItem ?: return

        States.theme.addEvent(WeakReference(optionPane)) { _ ->
            setDefaults(optionPane)
        }

        States.scale.addEvent(WeakReference(optionPane)) { _ ->
            setDefaults(optionPane)
        }

        setDefaults(optionPane)
    }

    private fun setDefaults(item: CMenuItem) {
        cornerRadius = States.scale.get().controlScale.cornerRadius
        item.isOpaque = false
        item.background = Color(0, 0, 0, 0)
        item.font = fontType.getFont()
        item.foreground = States.theme.get().textLaF.base
        item.border = States.scale.get().controlScale.getNormalInsetBorder()
        selectionBackground = Color(0,0,0,0)
        selectionForeground = item.foreground
    }

    override fun paintMenuItem(g: Graphics?, c: JComponent?, checkIcon: Icon?, arrowIcon: Icon?, background: Color?, foreground: Color?, defaultTextIconGap: Int) {
        val item = c as? CMenuItem
        if (item == null) {
            super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground, defaultTextIconGap)
            return
        }

        val g2d = g?.create() as? Graphics2D
        if (g2d == null) {
            super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground, defaultTextIconGap)
            return
        }

        if (item.isArmed || item.isSelected) {
            g2d.color = hoverBackground
            g2d.fillRoundRect(0, 0, item.width - 1, item.height - 1, cornerRadius, cornerRadius)
        }

        super.paintMenuItem(g2d, item, checkIcon, arrowIcon, background, foreground, defaultTextIconGap)
    }

}