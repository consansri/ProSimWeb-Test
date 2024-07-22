package prosim.skialib.core

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme

class SkiaTester : SComponent() {
    override fun setDefaults(theme: Theme, scaling: Scaling, icons: Icons) {

    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val paint = Paint().apply {
            color = Color.RED
        }

        canvas.clear(Color.BLACK)

        canvas.drawRect(Rect(10f, 10f, 100f, 100f), paint)
    }
}