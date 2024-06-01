package me.c3.ui.styled.borders

import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.AbstractBorder
import javax.swing.border.LineBorder

class DirectionalBorder(
    private val north: Boolean = false,
    private val west: Boolean = false,
    private val south: Boolean = false,
    private val east: Boolean = false
) : AbstractBorder() {

    var lineBorder = LineBorder(ThemeManager.curr.globalLaF.borderColor, ScaleManager.curr.borderScale.thickness)
    var thickness = ScaleManager.curr.borderScale.thickness

    init {
        ScaleManager.addScaleChangeEvent {
            val color = ThemeManager.curr.globalLaF.borderColor
            this.lineBorder = LineBorder(color, it.borderScale.thickness)
            this.thickness = it.borderScale.thickness
        }

        ThemeManager.addThemeChangeListener {
            val thickness = ScaleManager.curr.borderScale.thickness
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
        return ScaleManager.curr.borderScale.getInsets()
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        val themeInsets = ScaleManager.curr.borderScale.getInsets()
        insets.set(themeInsets.top, themeInsets.left, themeInsets.bottom, themeInsets.right)
        return insets
    }

}