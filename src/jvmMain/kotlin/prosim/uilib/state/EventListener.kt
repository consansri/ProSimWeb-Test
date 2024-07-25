package prosim.uilib.state

/**
 * EventListener is an interface for objects that want to listen to specific events.
 *
 * @param T The type of data that will be passed when the event is triggered.
 */
interface EventListener<T> {

    /**
     * Called when the event this listener is registered for is triggered.
     *
     * @param newVal The value associated with the triggered event.
     */
    suspend fun onTrigger(newVal: T)

}