package prosim.skialib.core

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import prosim.uilib.UIStates
import prosim.skialib.core.border.Insets
import prosim.skialib.core.layouts.Attribute
import prosim.skialib.core.layouts.BorderLayout
import prosim.skialib.core.layouts.Layout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JFrame

open class SMainPanel(val frame: JFrame) : JComponent(), SkikoRenderDelegate, SChild {
    val skikoLayer: SkiaLayer = SkiaLayer()
    override val children: MutableMap<Attribute, SChild> = mutableMapOf()
    override val preferredWidth: Int = 0 // is only relevant if used in layout, but this should never be used in layout
    override val preferredHeight: Int = 0 // is only relevant if used in layout, but this should never be used in layout
    override val sWidth: Int get() = layout.width
    override val sHeight: Int get() = layout.height
    override var insets: Insets = Insets()
    val background: Paint = Paint().apply {
        color = UIStates.theme.get().globalLaF.bgPrimary.rgb
    }
    override var layout: Layout = BorderLayout()

    init {
        isVisible = true

    }

    fun setup(){
        skikoLayer.renderDelegate = this
        skikoLayer.attachTo(frame)
        skikoLayer.needRedraw()
    }

    /**
     * Connects Children Rendering to skia render delegate
     */
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.clear(Color.WHITE)
        render(canvas)
    }

    override fun customRender(canvas: Canvas) {
        canvas.clear(background.color)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(layout.width, layout.height)
    }


}