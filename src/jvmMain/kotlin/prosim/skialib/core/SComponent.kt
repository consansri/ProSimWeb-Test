package prosim.skialib.core

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.lang.ref.WeakReference
import javax.swing.SwingUtilities

abstract class SComponent : SkiaLayer() {

    init {
        installStateListeners()
        renderDelegate = RenderDelegate()
    }

    fun setup(){
        applyDefaults(UIStates.theme.get(), UIStates.scale.get(), UIStates.icon.get())
    }

    abstract fun setDefaults(theme: Theme, scaling: Scaling, icons: Icons)
    abstract fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)

    private fun updateTheme(theme: Theme) {
        applyDefaults(theme, UIStates.scale.get(), UIStates.icon.get())
    }

    private fun updateScale(scaling: Scaling) {
        applyDefaults(UIStates.theme.get(), scaling, UIStates.icon.get())
    }

    private fun updateIcon(icons: Icons) {
        applyDefaults(UIStates.theme.get(), UIStates.scale.get(), icons)
    }

    private fun applyDefaults(theme: Theme, scaling: Scaling, icons: Icons) {
        SwingUtilities.invokeLater {
            setDefaults(theme, scaling, icons)
        }
    }

    override fun dispose() {
        super.dispose()
        removeStateListeners()
    }

    private fun installStateListeners() {
        UIStates.theme.addEvent(WeakReference(this), ::updateTheme)
        UIStates.scale.addEvent(WeakReference(this), ::updateScale)
        UIStates.icon.addEvent(WeakReference(this), ::updateIcon)
    }

    private fun removeStateListeners() {
        UIStates.theme.removeEvent(::updateTheme)
        UIStates.scale.removeEvent(::updateScale)
        UIStates.icon.removeEvent(::updateIcon)
    }

    inner class RenderDelegate : SkikoRenderDelegate {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            this@SComponent.onRender(canvas, width, height, nanoTime)
        }
    }

}