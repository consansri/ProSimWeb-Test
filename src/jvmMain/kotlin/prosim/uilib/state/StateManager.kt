package prosim.uilib.state

import debug.DebugTools
import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * StateManager is an abstract class that manages state and notifies listeners of state changes.
 *
 * @param T The type of the state being managed.
 * @param init The initial state value.
 *
 * @property curr The current state value.
 * @property eventScope CoroutineScope used for asynchronous event dispatching.
 * @property listeners A list of weak references to state change listeners.
 */
abstract class StateManager<T>(init: T) : WSConfigLoader<T> {

    private var curr: T = init
    private val eventScope = CoroutineScope(Dispatchers.Default)

    private val listeners = mutableListOf<WeakReference<StateListener<T>>>()

    /**
     * Sets a new state value, updates the configuration, and triggers change notifications.
     *
     * @param value The new state value to set.
     */
    fun set(value: T) {
        curr = value
        updateConfig(curr)
        triggerChange()
    }

    /**
     * Retrieves the current state value.
     *
     * @return The current state value.
     */
    fun get(): T {
        return curr
    }

    /**
     * Sets a new state value without updating the configuration and triggers change notifications.
     *
     * @param value The new state value to set.
     */
    fun setConfigNotChanged(value: T) {
        curr = value
        triggerChange()
    }

    /**
     * Triggers state change notifications to all registered listeners.
     * Removes any garbage-collected listeners during this process.
     */
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
            // Silently catch and ignore concurrent modification exceptions
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
                    nativeLog("[Manager] Changed State to ${curr::class.simpleName} (${listeners.size} listeners, removed $removed)")
                }
            }
        }
    }

    /**
     * Called when the state changes. Can be overridden by subclasses to perform additional actions.
     *
     * @param value The new state value.
     */
    open fun onChange(value: T) {}

    /**
     * Adds a StateListener to be notified of state changes.
     *
     * @param listener The StateListener to add.
     *
     * Note: Listeners are stored as WeakReferences.
     * If the listener is garbage collected,
     * it will be automatically removed during the next state change.
     */
    fun addEvent(listener: StateListener<T>) {
        listeners.add(WeakReference(listener))
    }

    /**
     * Removes a specific StateListener from the list of listeners.
     *
     * @param listener The StateListener to remove.
     */
    fun removeEvent(listener: StateListener<T>) {
        listeners.removeIf {
            it.get() == listener
        }
    }

    /**
     * Creates a new StateListener with the given lambda function and adds it to the list of listeners.
     *
     * @param lambda The suspend function to be called when the state changes.
     * @return The created StateListener.
     *
     * WARNING: The returned listener must be attached to a living component to prevent
     * automatic garbage collection. If not properly retained, the listener may be
     * garbage collected and will no longer receive state change notifications.
     */
    fun createAndAddListener(lambda: suspend (T) -> Unit): StateListener<T> {
        val listener = object : StateListener<T> {
            val function = lambda

            override suspend fun onStateChange(newVal: T) {
                function(newVal)
            }
        }
        addEvent(listener)
        return listener
    }
}