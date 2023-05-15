package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory

class ArchRISCV : Architecture{

    constructor() : super("IKR RISC-V", ProgramMemory(4,32,32),DataMemory(32,4)) {

    }




}