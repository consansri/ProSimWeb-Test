package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory

class ArchCISC : Architecture {

    constructor():  super("IKR CISC", ProgramMemory(32,32), DataMemory(32,4)) {

    }
}