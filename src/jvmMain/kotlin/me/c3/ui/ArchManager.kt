package me.c3.ui

import emulator.kit.Architecture
import emulator.kit.nativeLog

class ArchManager(initialArch: Architecture) {
    private val archChangeListeners = mutableListOf<(Architecture) -> Unit>()
    private val featureChangeListeners = mutableListOf<(Architecture) -> Unit>()

    var curr = initialArch
        set(value) {
            field = value
            triggerArchChange()
        }

    fun addArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.add(event)
    }

    fun addFeatureChangeListener(event: (Architecture) -> Unit){
        featureChangeListeners.add(event)
    }

    fun removeArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.remove(event)
    }

    fun removeFeatureChangeListener(event: (Architecture) -> Unit){
        featureChangeListeners.remove(event)
    }

    fun triggerFeatureChanged() {
        val listenersCopy = ArrayList(featureChangeListeners)
        listenersCopy.forEach {
            it(curr)
        }
        nativeLog("ArchManager: Trigger Feature Change! (${curr.getDescription().name})")
    }

    private fun triggerArchChange() {
        val listenersCopy = ArrayList(archChangeListeners)
        listenersCopy.forEach {
            it(curr)
        }
        triggerFeatureChanged()
        nativeLog("ArchManager: Trigger Arch Change! (${curr.getDescription().name})")
    }
}