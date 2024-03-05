import kotlinx.coroutines.*

object Coroutines {

    val context = CoroutineScope(Job() + Dispatchers.Default)

    fun setTimeout(timeoutMillis: Long, block: suspend () -> Unit): Job {
        return context.launch {
            delay(timeoutMillis)
            block()
        }
    }

    fun loop(delayMillis: Long, block: suspend () -> Unit): Job {
        return context.launch {
            while (true) {
                delay(delayMillis)
                block()
            }
        }
    }

}