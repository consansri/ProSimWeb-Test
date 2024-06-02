package me.c3.ui.state

import emulator.kit.nativeLog

class Event<T>(val name: String) {

    private val listeners = mutableListOf<(T) -> Unit>()

    fun addListener(onTrigger: (T) -> Unit) {
        listeners.add(onTrigger)
    }

    fun removeListener(onTrigger: (T) -> Unit): Boolean {
        return listeners.remove(onTrigger)
    }

    fun triggerEvent(value: T) {
        ArrayList(listeners).forEach {
            it(value)
        }
        value?.let {
            nativeLog("Event: $name (${it::class.simpleName.toString()})")
        }
    }
}