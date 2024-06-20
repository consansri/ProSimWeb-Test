package me.c3.uilib.styled

import me.c3.uilib.UIStates
import me.c3.uilib.resource.Icons
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.theme.core.Theme
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
    }

    private fun updateTheme(theme: Theme) {
        val curr = component
        curr?.let {
            setDefaults(curr, theme, UIStates.scale.get(), UIStates.icon.get())
        }
    }

    private fun updateScale(scaling: Scaling){
        val curr = component
        curr?.let {
            setDefaults(curr, UIStates.theme.get(), scaling, UIStates.icon.get())
        }
    }

    private fun updateIcon(icons: Icons){
        val curr = component
        curr?.let {
            setDefaults(curr, UIStates.theme.get(), UIStates.scale.get(), icons)
        }
    }

    abstract fun setDefaults(c: T, theme: Theme, scaling: Scaling, icons: Icons)

}