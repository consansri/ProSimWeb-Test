package me.c3.ui.state

import emulator.kit.nativeLog
import java.awt.Component
import java.lang.ref.WeakReference

abstract class Manager<T>(init: T) : WSConfigLoader<T> {

    protected var curr: T = init

    private val listeners = mutableListOf<Pair<WeakReference<*>, (T) -> Unit>>()

    fun set(value: T) {
        curr = value
        updateConfig(curr)
        triggerChange()
    }

    fun get(): T = curr

    fun setConfigNotChanged(value: T) {
        curr = value
        triggerChange()
    }

    private fun triggerChange() {
        var removed = 0
        try {
            listeners.removeIf {
                removed++
                nativeLog("Removed: ${it.first::class.simpleName}")
                val component = it.first.get()
                (component == null) || (component is Component && component.parent == null)
            }
        } catch (_: ConcurrentModificationException) {
        }

        onChange(curr)
        ArrayList(listeners).forEach {
            it.second(curr)
        }
        curr?.let {
            nativeLog("${this::class.simpleName}: Switched ${it::class.simpleName} to ${it::class.simpleName} (${listeners.size} listeners, removed $removed)")
        }
    }

    open fun onChange(value: T) {}

    fun addEvent(ref: WeakReference<*>, event: (T) -> Unit) {
        listeners.add(ref to event)
    }

    fun removeEvent(event: (T) -> Unit) {
        listeners.removeIf {
            it.second == event
        }
    }
}