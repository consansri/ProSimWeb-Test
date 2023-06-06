package extendable.archs.riscii

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode

object RISCII {

    val config = Config(
        "IKR RISC-II",
        arrayOf(
            Register(0, "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name", listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010"), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<ExtensionType>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}