package extendable.archs.mini

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode

object Mini {

    val config = Config(
        "IKR Minimalprozessor",
        arrayOf(
            Register(0, "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name", listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010"), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript(),
        FlagsConditions(
            listOf(),
            listOf()
        )
    )

    fun add(extensionList: List<ExtensionType>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}