package me.c3.ui.components.borders

import me.c3.ui.UIManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.AbstractBorder
import javax.swing.border.LineBorder

class DirectionalBorder(
    uiManager: UIManager,
    private val north: Boolean = false,
    private val west: Boolean = false,
    private val south: Boolean = false,
    private val east: Boolean = false
) : AbstractBorder() {

    var lineBorder = LineBorder(uiManager.themeManager.currentTheme.globalStyle.borderColor, uiManager.scaleManager.currentScaling.borderScale.thickness)
        set(value) {
            field = value
        }

    var thickness = uiManager.scaleManager.currentScaling.borderScale.thickness
        set(value) {
            field = value
        }

    init {
        uiManager.scaleManager.addScaleChangeEvent {
            val color = uiManager.themeManager.currentTheme.globalStyle.borderColor
            this.lineBorder = LineBorder(color, it.borderScale.thickness)
            this.thickness = it.borderScale.thickness
        }

        uiManager.themeManager.addThemeChangeListener {
            val thickness = uiManager.scaleManager.currentScaling.borderScale.thickness
            this.lineBorder = LineBorder(it.globalStyle.borderColor, thickness)
            this.thickness = thickness
        }
    }

    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        if (north) {
            lineBorder.paintBorder(c, g, x, y, width, thickness)
        }
        if (west) {
            lineBorder.paintBorder(c, g, x, y, thickness, height)
        }
        if (south) {
            lineBorder.paintBorder(c, g, x, y + height - thickness, width, thickness)
        }
        if (east) {
            lineBorder.paintBorder(c, g, x + width - thickness, y, thickness, height)
        }
    }

    override fun getBorderInsets(c: Component): Insets {
        val insets = Insets(0, 0, 0, 0)
        if (north) insets.top = thickness
        if (west) insets.left = thickness
        if (south) insets.bottom = thickness
        if (east) insets.right = thickness
        return insets
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        insets.set(0, 0, 0, 0)
        if (north) insets.top = thickness
        if (west) insets.left = thickness
        if (south) insets.bottom = thickness
        if (east) insets.right = thickness
        return insets
    }

}