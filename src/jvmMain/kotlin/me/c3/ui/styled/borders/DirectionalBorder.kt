package me.c3.ui.styled.borders

import me.c3.ui.States
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

    var lineBorder = LineBorder(States.theme.get().globalLaF.borderColor, States.scale.get().borderScale.thickness)
    var thickness = States.scale.get().borderScale.thickness

    init {
        States.scale.addEvent { scale ->
            val color = States.theme.get().globalLaF.borderColor
            this.lineBorder = LineBorder(color, scale.borderScale.thickness)
            this.thickness = scale.borderScale.thickness
        }

        States.theme.addEvent { scale ->
            val thickness = States.scale.get().borderScale.thickness
            this.lineBorder = LineBorder(scale.globalLaF.borderColor, thickness)
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
        return States.scale.get().borderScale.getInsets()
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        val themeInsets = States.scale.get().borderScale.getInsets()
        insets.set(themeInsets.top, themeInsets.left, themeInsets.bottom, themeInsets.right)
        return insets
    }

}