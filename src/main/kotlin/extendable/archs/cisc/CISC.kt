package extendable.archs.cisc

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.OpCode
import extendable.components.types.ByteValue

object CISC {

    // OpMnemonic Labels
    const val OPLBL_SPLIT = "_"
    val OPLBL_OPCODE = OpCode.OpLabel("[opcode]", null, true)

    val config = Config(
        "IKR CISC",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), ""))),
                        RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.SYSTEM, "SYSTEM", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.CUSTOM, "CUSTOM", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), ""))),
            )
        ),
        listOf(
            Instruction("name", listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010", listOf(OPLBL_OPCODE), OPLBL_SPLIT), "", "", ",", ::add),
        ),
        Memory(32, "0", 4),
        Transcript()
    )

    fun add(opCodeBinary: String?, extensionWords: List<Instruction.Ext>?, memory: Memory, registerContainer: RegisterContainer, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}