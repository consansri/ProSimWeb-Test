package extendable.archs.mini

import extendable.components.*

object Mini {

    val config = Config("IKR Minimalprozessor",
        arrayOf(Register(0,"r0",0, "")),
        listOf(Instruction("name", 2)),
        DataMemory(32, 4),
        Transcript(4, 32, 32)
    )

}