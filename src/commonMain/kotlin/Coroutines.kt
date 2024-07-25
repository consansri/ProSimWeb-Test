import emulator.kit.nativeLog
import kotlinx.coroutines.*

object Coroutines {

    val context = CoroutineScope(Job() + Dispatchers.Default)

    fun setTimeout(timeoutMillis: Long, block: suspend () -> Unit): Job {
        return context.launch {
            nativeLog("Executing with delay!")
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