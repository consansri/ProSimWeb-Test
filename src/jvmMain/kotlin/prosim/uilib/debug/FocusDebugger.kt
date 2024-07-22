package prosim.uilib.debug

import emulator.kit.nativeLog
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class FocusDebugger: FocusListener {
    override fun focusGained(e: FocusEvent) {
        nativeLog("Focus gained by: ${e.component}")
        if (e.oppositeComponent != null){
            nativeLog("Opposite component: ${e.oppositeComponent.javaClass.simpleName}")
        }
        nativeLog("Cause: ${e.cause}")
        nativeLog("Stack trace:")
        Thread.currentThread().stackTrace.take(10).forEach {
            nativeLog(it.toString())
        }
        nativeLog("\n")
    }

    override fun focusLost(e: FocusEvent) {
        nativeLog("Focus lost by: ${e.component}")
        if (e.oppositeComponent != null){
            nativeLog("Opposite component: ${e.oppositeComponent.javaClass.simpleName}")
        }
        nativeLog("Cause: ${e.cause}")
        nativeLog("Stack trace:")
        Thread.currentThread().stackTrace.take(10).forEach {
            nativeLog(it.toString())
        }
        nativeLog("\n")
    }
}