package extendable.archs.riscv64

import extendable.archs.riscv32.RV32
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.Variable

object RV64 {

    val asmConfig = AsmConfig(RV64Grammar(), RV64Assembly())

    val config = Config(
        "RV64I",
        Docs(),
        FileHandler("s"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile( "R0", arrayOf(RegisterContainer.Register(Variable.Value.Dec("0", RV32.REG_ADDRESS_SIZE), listOf("x0"), listOf("r0"), Variable("0", Variable.Size.Bit32()), "")))
            ),
            pcSize = Variable.Size.Bit32()
        ),
        Memory(Variable.Size.Bit32(), "0", Variable.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}