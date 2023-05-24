package extendable.cisc

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.Instruction
import extendable.components.Transcript
import extendable.components.Register

class ArchRISCII : Architecture {

    constructor() : super("IKR RISC-II",
        arrayOf(Register(0,"r0",0, "")),
        listOf(Instruction("name", 2)),
        DataMemory(32, 4),
        Transcript(4, 32, 32)
        ) {

    }

}