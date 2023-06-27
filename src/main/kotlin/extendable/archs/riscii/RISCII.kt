package extendable.archs.riscii

import extendable.Architecture
import extendable.archs.cisc.CISC
import extendable.components.*
import extendable.components.assembly.Grammar
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.OpCode
import extendable.components.types.ByteValue

object RISCII {

    // OpMnemonic Labels
    const val OPLBL_SPLIT = "_"
    val OPLBL_OPCODE = OpCode.OpLabel("[opcode]", null, true)

    val asmConfig = AsmConfig(RISCIIGrammar())


    val config = Config(
        "IKR RISC-II",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), "")))
            )
        ),
        listOf(
            Instruction("name", listOf(Instruction.EXT.REG), OpCode("0101010", listOf(CISC.OPLBL_OPCODE), CISC.OPLBL_SPLIT), "", "", ",", ::add),
        ),
        Memory(32, "0", 4),
        Transcript()
    )

    fun add(architecture: Architecture, mode: Instruction.ExecutionMode): Instruction.ReturnType {
        when(mode){
            is Instruction.ExecutionMode.EXECUTION -> {
                return Instruction.ReturnType.ExecutionSuccess(false)
            }
            is Instruction.ExecutionMode.BYTEGENERATION -> {
                return Instruction.ReturnType.BinaryRep(emptyList())
            }
        }
    }

}