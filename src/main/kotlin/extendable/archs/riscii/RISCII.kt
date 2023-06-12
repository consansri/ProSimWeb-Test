package extendable.archs.riscii

import extendable.ArchConst
import extendable.archs.cisc.CISC
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode

object RISCII {

    // OpMnemonic Labels
    const val OPLBL_SPLIT = "_"
    const val OPLBL_OPCODE = "[opcode]"

    val config = Config(
        "IKR RISC-II",
        arrayOf(
            Register(Address(0, 32), "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name", listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010", listOf(CISC.OPLBL_OPCODE), CISC.OPLBL_SPLIT), "", "", ::add),
        ),
        Memory(32, 4),
        Transcript()
    )

    fun add(extensionList: List<ExtensionType>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}