package prosim.uilib.styled.borders

import prosim.uilib.UIStates
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets

class DirectionalBorder(
    private val north: Boolean = false,
    private val west: Boolean = false,
    private val south: Boolean = false,
    private val east: Boolean = false
) : CLineBorder() {
    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        if (north) super.paintBorder(c, g, x, y, width, thickness)
        if (west) super.paintBorder(c, g, x, y, thickness, height)
        if (south) super.paintBorder(c, g, x, y + height - thickness, width, thickness)
        if (east) super.paintBorder(c, g, x + width - thickness, y, thickness, height)
    }

    override fun getBorderInsets(c: Component): Insets {
        return UIStates.scale.get().INSETS_MEDIUM
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        val themeInsets = UIStates.scale.get().INSETS_MEDIUM
        insets.set(themeInsets.top, themeInsets.left, themeInsets.bottom, themeInsets.right)
        return insets
    }
}