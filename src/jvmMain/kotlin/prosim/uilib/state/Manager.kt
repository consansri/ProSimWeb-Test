package prosim.uilib.state

import debug.DebugTools
import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

abstract class Manager<T>(init: T) : WSConfigLoader<T> {

    protected var curr: T = init
    private val eventScope = CoroutineScope(Dispatchers.Default)

    private val listeners = mutableListOf<WeakReference<StateListener<T>>>()

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
                val ref = it.get()
                val shouldBeRemoved = ref == null
                if (shouldBeRemoved) removed++
                shouldBeRemoved
            }
        } catch (_: ConcurrentModificationException) {
        }

        onChange(curr)

        eventScope.launch {
            ArrayList(listeners).forEach {
                val event = it.get()
                if (event != null) {
                    event.onStateChange(curr)
                }
            }
            if (DebugTools.JVM_showStateManagerInfo) {
                curr?.let { curr ->
                    nativeLog("[Manager] Changed State to ${curr::class.simpleName} (${listeners.size} listeners, removed $removed)" +
                            "[Listener Details] ${
                                listeners.joinToString {
                                    val event = it.get()
                                    event?.toString() ?: "null"
                                }
                            }"
                    )
                }
            }
        }
    }

    open fun onChange(value: T) {}

    /**
     * Adds an EventListener which executes the event on state change.
     *
     * If the WeakReference of the event function is null, it will automatically get removed by the Manager.
     */
    fun addEvent(listener: StateListener<T>) {
        listeners.add(WeakReference(listener))
    }

    fun removeEvent(listener: StateListener<T>) {
        listeners.removeIf {
            it.get() == listener
        }
    }
}