package extendable.archs.riscii

import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.ByteValue

object RISCII {

    val asmConfig = AsmConfig(RISCIIGrammar(), RISCIIAssembly())


    val config = Config(
        "IKR RISC-II",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), "")))
            )
        ),
        Memory(ByteValue.Size.Bit32(), "0", ByteValue.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}