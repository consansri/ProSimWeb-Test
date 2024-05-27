package me.c3.ui.styled.borders

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.AbstractBorder
import javax.swing.border.LineBorder

class DirectionalBorder(
    private val tm: ThemeManager,
    private val sm: ScaleManager,
    private val north: Boolean = false,
    private val west: Boolean = false,
    private val south: Boolean = false,
    private val east: Boolean = false
) : AbstractBorder() {

    var lineBorder = LineBorder(tm.curr.globalLaF.borderColor, sm.curr.borderScale.thickness)
    var thickness = sm.curr.borderScale.thickness

    init {
        sm.addScaleChangeEvent {
            val color = tm.curr.globalLaF.borderColor
            this.lineBorder = LineBorder(color, it.borderScale.thickness)
            this.thickness = it.borderScale.thickness
        }

        tm.addThemeChangeListener {
            val thickness = sm.curr.borderScale.thickness
            this.lineBorder = LineBorder(it.globalLaF.borderColor, thickness)
            this.thickness = thickness
        }
    }

    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        if (north) lineBorder.paintBorder(c, g, x, y, width, thickness)
        if (west) lineBorder.paintBorder(c, g, x, y, thickness, height)
        if (south) lineBorder.paintBorder(c, g, x, y + height - thickness, width, thickness)
        if (east) lineBorder.paintBorder(c, g, x + width - thickness, y, thickness, height)
    }

    override fun getBorderInsets(c: Component): Insets {
        return sm.curr.borderScale.getInsets()
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        val themeInsets = sm.curr.borderScale.getInsets()
        insets.set(themeInsets.top, themeInsets.left, themeInsets.bottom, themeInsets.right)
        return insets
    }

}