package me.c3.ui

import emulator.kit.Architecture
import emulator.kit.nativeLog

class ArchManager(initialArch: Architecture) {
    private val archChangeListeners = mutableListOf<(Architecture) -> Unit>()

    var curr = initialArch
        set(value) {
            field = value
            triggerArchChange()
        }

    fun addArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.add(event)
    }

    fun removeArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.remove(event)
    }

    private fun triggerArchChange(){
        val listenersCopy = ArrayList(archChangeListeners)
        listenersCopy.forEach{
            it(curr)
        }
        nativeLog("ArchManager: Trigger Arch Change!")
    }
}