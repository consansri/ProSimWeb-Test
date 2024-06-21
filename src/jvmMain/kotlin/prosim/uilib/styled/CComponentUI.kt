package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

abstract class CComponentUI<T : JComponent> : ComponentUI() {

    var component: T? = null

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        this.component = c as? T

        UIStates.theme.addEvent(WeakReference(c), ::updateTheme)
        UIStates.scale.addEvent(WeakReference(c), ::updateScale)
        UIStates.icon.addEvent(WeakReference(c), ::updateIcon)

        this.component?.let {
            applyDefaults()
            onInstall(it)
        }
    }

    private fun updateTheme(theme: Theme) {
        applyDefaults()
    }

    private fun updateScale(scaling: Scaling) {
        applyDefaults()
    }

    private fun updateIcon(icons: Icons) {
        applyDefaults()
    }

    fun applyDefaults() {
        component?.let {
            setDefaults(it, UIStates.theme.get(), UIStates.scale.get(), UIStates.icon.get())
            it.revalidate()
            it.repaint()
        }
    }

    abstract fun onInstall(c: T)

    abstract fun setDefaults(c: T, theme: Theme, scaling: Scaling, icons: Icons)

}