package me.c3.ui

import emulator.kit.Architecture
import emulator.kit.nativeLog

/**
 * Manages the architecture settings and notifies listeners of changes.
 * @param initialArch The initial architecture to be set.
 */
class ArchManager(initialArch: Architecture) {

    // Listeners for architecture change events.
    private val archChangeListeners = mutableListOf<(Architecture) -> Unit>()

    // Listeners for feature change events within the architecture.
    private val featureChangeListeners = mutableListOf<(Architecture) -> Unit>()

    // Current architecture, triggers change events when set.
    var curr = initialArch
        set(value) {
            field = value
            triggerArchChange()
        }

    /**
     * Adds a listener for architecture change events.
     * @param event The listener to be added.
     */
    fun addArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.add(event)
    }

    /**
     * Adds a listener for feature change events.
     * @param event The listener to be added.
     */
    fun addFeatureChangeListener(event: (Architecture) -> Unit) {
        featureChangeListeners.add(event)
    }

    /**
     * Removes a listener for architecture change events.
     * @param event The listener to be removed.
     */
    fun removeArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.remove(event)
    }

    /**
     * Removes a listener for feature change events.
     * @param event The listener to be removed.
     */
    fun removeFeatureChangeListener(event: (Architecture) -> Unit) {
        featureChangeListeners.remove(event)
    }

    /**
     * Triggers feature change events for all registered listeners.
     */
    fun triggerFeatureChanged() {
        val listenersCopy = ArrayList(featureChangeListeners)
        listenersCopy.forEach {
            it(curr)
        }
        nativeLog("ArchManager: Trigger Feature Change! (${curr.description.name})")
    }

    /**
     * Triggers architecture change events for all registered listeners,
     * and then triggers feature change events.
     */
    private fun triggerArchChange() {
        val listenersCopy = ArrayList(archChangeListeners)
        listenersCopy.forEach {
            it(curr)
        }
        triggerFeatureChanged()
        nativeLog("ArchManager: Trigger Arch Change! (${curr.description.name})")
    }
}
