package extendable.archs.cisc

import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.ByteValue

object CISC {

    val asmConfig = AsmConfig(CISCGrammar(), CISCAssembly())

    val config = Config(
        "IKR CISC",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "R0", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("0", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.SYSTEM, "SYSTEM", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.CUSTOM, "CUSTOM", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), ""))),
            ),
            pcSize = ByteValue.Size.Bit32()
        ),
        Memory(ByteValue.Size.Bit32(), "0", ByteValue.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}