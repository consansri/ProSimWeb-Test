package prosim.uilib.state

interface EventListener<T> {

    suspend fun onTrigger(newVal: T)

}