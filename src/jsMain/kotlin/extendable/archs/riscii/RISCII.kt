package extendable.archs.riscii

import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.MutVal

object RISCII {

    val asmConfig = AsmConfig(RISCIIGrammar(), RISCIIAssembly())


    val config = Config(
        "IKR RISC-II",
        FileHandler("riiasm"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile( "R0", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("0", RISCV.REG_ADDRESS_SIZE), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), "")))
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), "0", MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}