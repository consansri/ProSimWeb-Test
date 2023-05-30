package extendable.archs.cisc

import extendable.components.*

object CISC {

    val config = Config(
        "IKR CISC", arrayOf(Register(0, "r0", 0, "")),
        listOf(Instruction("name", 2)),
        DataMemory(32, 4),
        Transcript()
    )

}