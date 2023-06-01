package extendable.archs.mini

import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.OpCode

object Mini {

    val config = Config("IKR Minimalprozessor",
        arrayOf(
            Register(0, "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name", 3,"[name] , , ", OpCode("0101010"), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<String>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}