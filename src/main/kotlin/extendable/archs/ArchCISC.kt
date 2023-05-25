package extendable.cisc

import extendable.Architecture
import extendable.archs.cisc.CISC
import extendable.components.DataMemory
import extendable.components.Instruction
import extendable.components.Transcript
import extendable.components.Register

class ArchCISC : Architecture {

    constructor() : super(CISC.config) {

    }
}