package prosim.uilib.state

interface StateListener<T> {
    suspend fun onStateChange(newVal: T)
}

