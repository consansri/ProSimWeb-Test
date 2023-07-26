package extendable.archs.cisc

import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.MutVal

object CISC {

    val asmConfig = AsmConfig(CISCGrammar(), CISCAssembly())

    val config = Config(
        "IKR CISC",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "R0", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("0", RISCV.REG_ADDRESS_SIZE), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.MAIN, "MAIN", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.SYSTEM, "SYSTEM", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), ""))),
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.CUSTOM, "CUSTOM", arrayOf(RegisterContainer.Register(MutVal.Value.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), MutVal("0", MutVal.Size.Bit32()), ""))),
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), "0", MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript()
    )

}