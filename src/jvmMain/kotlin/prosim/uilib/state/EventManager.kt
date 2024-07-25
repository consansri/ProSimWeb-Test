package prosim.uilib.state

import debug.DebugTools
import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * EventManager is a generic class that manages event listeners and triggers events.
 *
 * WARNING: Listeners added to this EventManager must be attached to a living component
 * to prevent automatic garbage collection.
 * If a listener is not properly retained,
 * it may be garbage collected and will no longer receive events.
 *
 * @param T The type of data that will be passed when an event is triggered.
 * @property name A string identifier for this EventManager instance.
 */
class EventManager<T>(val name: String) {

    // List of weak references to event listeners
    private val listeners = mutableListOf<WeakReference<EventListener<T>>>()

    // CoroutineScope for asynchronous event triggering
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Adds a new listener to the event manager.
     *
     * @param listener The EventListener to be added.
     *
     * The added listener must be attached to a living component to prevent
     * automatic garbage collection. If not properly retained, the listener may be
     * garbage collected and will no longer receive events.
     */
    fun addListener(listener: EventListener<T>) {
        listeners.add(WeakReference(listener))
    }

    /**
     * Removes a specific listener from the event manager.
     *
     * @param listener The EventListener to be removed.
     * @return Boolean indicating whether the listener was successfully removed.
     */
    fun removeListener(listener: EventListener<T>): Boolean {
        return listeners.removeIf {
            it.get() == listener
        }
    }

    /**
     * Triggers an event, notifying all active listeners.
     *
     * @param value The data to be passed to the listeners.
     */
    fun triggerEvent(value: T) {
        listeners.removeIf {
            it.get() == null
        }

        scope.launch {
            ArrayList(listeners).forEach {
                it.get()?.onTrigger(value)
            }
            if (DebugTools.JVM_showEventManagerInfo) {
                value?.let {
                    nativeLog("Event: $name (${it::class.simpleName.toString()})")
                }
            }
        }
    }

    /**
     * Attach the returned [EventListener] to the component which uses it!
     *
     * Otherwise, it gets garbage collected.
     */
    fun createAndAddListener(lambda: suspend (T) -> Unit): EventListener<*> {
        val listener = object : EventListener<T> {
            val function = lambda

            override suspend fun onTrigger(newVal: T) {
                function(newVal)
            }
        }
        addListener(listener)
        return listener
    }
}