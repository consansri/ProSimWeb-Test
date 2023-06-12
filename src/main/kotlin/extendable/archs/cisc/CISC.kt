package extendable.archs.cisc

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode

object CISC {

    // OpMnemonic Labels
    const val OPLBL_SPLIT = "_"
    const val OPLBL_OPCODE = "[opcode]"

    val config = Config(
        "IKR CISC",
        arrayOf(
            Register(Address(0, 32), "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name",listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010", listOf(OPLBL_OPCODE), OPLBL_SPLIT), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<ExtensionType>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}