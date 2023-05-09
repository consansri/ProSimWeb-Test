package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory

class ArchRISCII: Architecture {

    constructor():  super("IKR RISC-II", ProgramMemory(32,32), DataMemory(32,4)){

    }

}