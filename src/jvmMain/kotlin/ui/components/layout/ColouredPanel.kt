package me.c3.ui.components.layout

import me.c3.ui.UIManager
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.components.styled.CPanel
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

open class ColouredPanel(uiManager: UIManager, primary: Boolean) : CPanel(uiManager, primary) {
    private var colors: List<ColorAnker> = listOf(ColorAnker(0.0f, Color(0xc76b29)), ColorAnker(1.0f, Color(0x3d8fd1)))
        set(value) {
            field = value
            repaint()
        }

    init {
        background = Color(0,0,0,0)
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

    inner class ColorAnker(val pos: Float, val color: Color)

}