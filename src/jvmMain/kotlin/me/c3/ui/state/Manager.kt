package me.c3.ui.state

import emulator.kit.nativeLog

abstract class Manager<T>(init: T) : WSConfigLoader<T> {

    protected var curr: T = init

    private val listeners = mutableListOf<(T) -> Unit>()

    fun set(value: T) {
        curr = value
        updateConfig(curr)
        triggerChange()
    }

    fun get(): T = curr

    fun setConfigNotChanged(value: T){
        curr = value
        triggerChange()
    }

    private fun triggerChange() {
        onChange()
        ArrayList(listeners).forEach {
            it(curr)
        }
        curr?.let {
            nativeLog("${this::class.simpleName}: Switched ${it::class.simpleName} to ${it::class.simpleName}")
        }
    }

    open fun onChange() {}

    fun addEvent(event: (T) -> Unit) {
        listeners.add(event)
    }

    fun removeEvent(event: (T) -> Unit) {
        listeners.remove(event)
    }
}