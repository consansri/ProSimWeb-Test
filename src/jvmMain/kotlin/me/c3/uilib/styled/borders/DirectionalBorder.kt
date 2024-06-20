package me.c3.uilib.styled.borders

import me.c3.uilib.UIStates
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

    val lineBorder = LineBorder(UIStates.theme.get().globalLaF.borderColor, UIStates.scale.get().borderScale.thickness)
    val thickness = UIStates.scale.get().borderScale.thickness

    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        if (north) lineBorder.paintBorder(c, g, x, y, width, thickness)
        if (west) lineBorder.paintBorder(c, g, x, y, thickness, height)
        if (south) lineBorder.paintBorder(c, g, x, y + height - thickness, width, thickness)
        if (east) lineBorder.paintBorder(c, g, x + width - thickness, y, thickness, height)
    }

    override fun getBorderInsets(c: Component): Insets {
        return UIStates.scale.get().borderScale.getInsets()
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        val themeInsets = UIStates.scale.get().borderScale.getInsets()
        insets.set(themeInsets.top, themeInsets.left, themeInsets.bottom, themeInsets.right)
        return insets
    }

}