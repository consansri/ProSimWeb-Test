package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory

class ArchMinimalprozessor : Architecture {

    constructor(): super("IKR Minimalprozessor", ProgramMemory(32,32), DataMemory(32,4)){

    }
}