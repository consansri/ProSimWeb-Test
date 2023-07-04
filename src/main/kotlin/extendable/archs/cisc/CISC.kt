package extendable.archs.cisc

import extendable.Architecture
import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.assembly.Grammar
import extendable.components.connected.*
import extendable.components.types.ByteValue

object CISC {

    val asmConfig = AsmConfig(CISCGrammar(), CISCCompiler())

    val config = Config(
        "IKR CISC",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), "r0", ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), "r0", ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.SYSTEM, "SYSTEM", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), "r0", ByteValue("0", ByteValue.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.CUSTOM, "CUSTOM", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), "r0", ByteValue("0", ByteValue.Size.Bit32()), ""))),
            )
        ),
        Memory(ByteValue.Size.Bit32(), "0", ByteValue.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}