package me.c3.uilib.state

import debug.DebugTools
import emulator.kit.nativeLog
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
                val ref = it.first.get()
                val shouldBeRemoved = ref == null
                if (shouldBeRemoved) removed++
                shouldBeRemoved
            }
        } catch (_: ConcurrentModificationException) {
        }

        onChange(curr)
        ArrayList(listeners).forEach {
            val event = it.second(curr)
        }
        if (DebugTools.JVM_showStateManagerInfo) {
            curr?.let { curr ->
                nativeLog("[Manager] Changed State to ${curr::class.simpleName} (${listeners.size} listeners, removed $removed)")
            }
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