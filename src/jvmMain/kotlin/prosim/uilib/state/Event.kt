package prosim.uilib.state

import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class Event<T>(val name: String) {

    private val listeners = mutableListOf<WeakReference<EventListener<T>>>()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun addListener(listener: EventListener<T>) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: EventListener<T>): Boolean {
        return listeners.removeIf {
            it.get() == listener
        }
    }

    fun triggerEvent(value: T) {
        listeners.removeIf {
            it.get() == null
        }

        scope.launch {
            ArrayList(listeners).forEach {
                it.get()?.onTrigger(value)
            }
            value?.let {
                nativeLog("Event: $name (${it::class.simpleName.toString()})")
            }
        }
    }

    /**
     * Attach the [EventListener] to the component which uses it!
     *
     * Cause otherwise it gets automatically garbage collected.
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