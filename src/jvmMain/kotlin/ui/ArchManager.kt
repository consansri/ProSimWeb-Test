package me.c3.ui

import emulator.kit.Architecture
import emulator.kit.nativeLog

class ArchManager(initialArch: Architecture) {
    private val archChangeListeners = mutableListOf<(Architecture) -> Unit>()

    var curr = initialArch
        set(value) {
            field = value
            archChangeListeners.forEach {
                it(value)
            }
            nativeLog("ArchManager: Trigger Arch Change!")
        }

    fun addArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.add(event)
    }

    fun removeArchChangeListener(event: (Architecture) -> Unit) {
        archChangeListeners.remove(event)
    }



}