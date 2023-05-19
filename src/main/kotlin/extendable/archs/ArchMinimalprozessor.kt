package extendable.archs

import extendable.Architecture
import extendable.components.DataMemory
import extendable.components.ProgramMemory
import extendable.components.Register

class ArchMinimalprozessor : Architecture {

    constructor(): super("IKR Minimalprozessor", ProgramMemory(4,32,32), DataMemory(32,4), arrayOf(Register(0,"r0",0, ""))){

    }
}