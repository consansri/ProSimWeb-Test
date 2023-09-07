package extendable.archs.riscv64

import extendable.archs.riscv32.RV32
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.MutVal

object RV64 {

    val asmConfig = AsmConfig(RV64Grammar(), RV64Assembly())


    val config = Config(
        "RV64I",
        Docs(),
        FileHandler("s"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile( "R0", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("0", RV32.REG_ADDRESS_SIZE), listOf("x0"), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), "")))
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), "0", MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}