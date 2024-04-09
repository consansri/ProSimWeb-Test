package me.c3.ui.spacing

import emulator.kit.nativeLog
import me.c3.ui.scale.scalings.StandardScaling
import me.c3.ui.spacing.core.Scaling

class ScaleManager {

    val scalings: List<Scaling> = listOf(StandardScaling())

    var currentScaling: Scaling = scalings.first()
        set(value) {
            field = value
            triggerScaleChange()
        }

    init {
        assert(scalings.isNotEmpty()) {
            throw Exception("No Scaling supplied!")
        }
    }

    private val scaleChangeEvents = mutableListOf<(scaling: Scaling) -> Unit>()

    fun addScaleChangeEvent(event: (scaling: Scaling) -> Unit) {
        scaleChangeEvents.add(event)
    }

    fun removeScaleChangeEvent(event: (scaling: Scaling) -> Unit) {
        scaleChangeEvents.remove(event)
    }

    private fun triggerScaleChange(){
        val listenersCopy = ArrayList(scaleChangeEvents)
        listenersCopy.forEach{
            it(currentScaling)
        }
        nativeLog("ScaleManager: Switched Scaling to ${currentScaling.name}!")
    }

}