package prosim.uilib.state

/**
 * StateListener is an interface for objects that want to be notified of state changes.
 *
 * @param T The type of the state being listened to.
 */
interface StateListener<T> {

    /**
     * Called when the state this listener is registered for changes.
     *
     * @param newVal The new value of the state after the change.
     */
    suspend fun onStateChange(newVal: T)
}

