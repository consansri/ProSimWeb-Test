package extendable.cisc

import extendable.Architecture
import extendable.archs.riscii.RISCII
import extendable.archs.riscv.RISCV
import extendable.components.DataMemory
import extendable.components.Instruction
import extendable.components.Transcript
import extendable.components.Register

class ArchRISCII : Architecture {

    constructor() : super(RISCII.config) {

    }

}