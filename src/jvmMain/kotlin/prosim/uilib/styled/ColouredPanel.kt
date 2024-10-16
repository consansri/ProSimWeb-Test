package prosim.uilib.styled

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LinearGradientPaint

open class ColouredPanel() : CPanel() {
    private var colors: List<ColorAnker> = listOf(ColorAnker(0.0f, Color(0xc76b29)), ColorAnker(1.0f, Color(0x3d8fd1)))
        set(value) {
            field = value
            repaint()
        }

    init {
        background = Color(0, 0, 0, 0)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        g?.let {
            val g2d = g.create() as Graphics2D
            val w = width
            val h = height

            // Define the gradient paint
            val gradient = LinearGradientPaint(
                0f, 0f, w.toFloat(), h.toFloat(),
                colors.map { it.pos }.toFloatArray(),
                colors.map { it.color }.toTypedArray()
            )

            // Paint the gradient
            g2d.paint = gradient
            g2d.fillRect(0, 0, w, h)

            g2d.dispose()
        }
    }

    class ColorAnker(val pos: Float, val color: Color)
}