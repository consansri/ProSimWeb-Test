package me.c3.ui.manager

import emulator.kit.nativeLog
import me.c3.ui.scale.scalings.StandardScaling
import me.c3.ui.scale.core.Scaling
import me.c3.ui.scale.scalings.LargerScaling
import me.c3.ui.scale.scalings.SmallScaling

/**
 * Manages the scaling options for the UI components, allowing for scaling changes and handling scale change events.
 */
object ScaleManager {

    // List of available scaling options.
    val scalings: List<Scaling> = listOf(
        StandardScaling(),
        SmallScaling(),
        LargerScaling(),
        me.c3.ui.scale.scalings.LargeScaling()
    )

    // Currently active scaling option.
    var curr: Scaling = scalings.first()
        set(value) {
            field = value
            // Trigger scale change events when the current scaling option is updated.
            triggerScaleChange()
        }

    // List of scale change event listeners.
    private val scaleChangeEvents = mutableListOf<(scaling: Scaling) -> Unit>()

    // Initialization block to ensure there is at least one scaling option supplied.
    init {
        assert(scalings.isNotEmpty()) {
            throw Exception("No Scaling supplied!")
        }
    }

    /**
     * Adds a listener for scale change events.
     * @param event The event listener to be added.
     */
    fun addScaleChangeEvent(event: (scaling: Scaling) -> Unit) {
        scaleChangeEvents.add(event)
    }

    /**
     * Removes a listener for scale change events.
     * @param event The event listener to be removed.
     */
    fun removeScaleChangeEvent(event: (scaling: Scaling) -> Unit) {
        scaleChangeEvents.remove(event)
    }

    /**
     * Triggers all registered scale change events.
     */
    private fun triggerScaleChange() {
        // Create a copy of the listeners to avoid modification issues during iteration.
        val listenersCopy = ArrayList(scaleChangeEvents)
        listenersCopy.forEach {
            it(curr)
        }
        // Log the scaling change.
        nativeLog("ScaleManager: Switched Scaling to ${curr.name}!")
    }
}