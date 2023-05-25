package extendable.archs.riscii

import extendable.components.*

object RISCII {

    val config = Config("IKR RISC-II",
        arrayOf(Register(0,"r0",0, "")),
        listOf(Instruction("name", 2)),
        DataMemory(32, 4),
        Transcript(4, 32, 32)
    )

}