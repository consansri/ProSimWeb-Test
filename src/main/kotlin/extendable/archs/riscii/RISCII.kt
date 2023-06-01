package extendable.archs.riscii

import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.OpCode

object RISCII {

    val config = Config(
        "IKR RISC-II",
        arrayOf(
            Register(0, "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name",3, "[name] , , ", OpCode("0101010"), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<String>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}