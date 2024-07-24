package prosim.uilib.state

interface EventListener<T> {

    fun onEventChange(newVal: T)

}