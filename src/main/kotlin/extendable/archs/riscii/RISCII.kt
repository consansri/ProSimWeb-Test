package extendable.archs.riscii

import extendable.Architecture
import extendable.archs.cisc.CISC
import extendable.components.*
import extendable.components.assembly.Grammar
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.ByteValue

object RISCII {

    val asmConfig = AsmConfig(RISCIIGrammar())


    val config = Config(
        "IKR RISC-II",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(Address(0, 32), "r0", ByteValue("0", 4), "")))
            )
        ),
        Memory(32, "0", 4, Memory.Endianess.LittleEndian),
        Transcript()
    )

}