package me.c3.ui.state

import emulator.kit.nativeLog
import java.lang.ref.WeakReference

class Event<T>(val name: String) {

    private val listeners = mutableListOf<Pair<WeakReference<*>, (T) -> Unit>>()

    fun addListener(ref: WeakReference<*>, onTrigger: (T) -> Unit) {
        listeners.add(ref to onTrigger)
    }

    fun removeListener(onTrigger: (T) -> Unit): Boolean {
        return listeners.removeIf {
            it.second == onTrigger
        }
    }

    fun triggerEvent(value: T) {
        listeners.removeIf {
            it.first.get() == null
        }
        ArrayList(listeners).forEach {
            it.second(value)
        }
        value?.let {
            nativeLog("Event: $name (${it::class.simpleName.toString()})")
        }
    }
}