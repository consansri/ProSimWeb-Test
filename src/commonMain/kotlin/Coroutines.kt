import kotlinx.coroutines.*

object Coroutines {

    val context = CoroutineScope(Job() + Dispatchers.Default)

    fun setTimeout(timeoutMillis: Long, block: suspend () -> Unit): Job {
        return context.launch {
            delay(timeoutMillis)
            block()
        }
    }

}