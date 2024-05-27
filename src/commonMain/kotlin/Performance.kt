import kotlin.time.Duration

object Performance {

    var MAX_INSTR_EXE_AMOUNT = 1000L

    fun updateExePerformance(instrCount: Long, ellapsed: Duration) {
        MAX_INSTR_EXE_AMOUNT = 1000000000 / (ellapsed.inWholeNanoseconds / instrCount)
    }

}