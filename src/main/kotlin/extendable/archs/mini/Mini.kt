package extendable.archs.mini

import extendable.archs.riscv.RISCV
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.ByteValue

object Mini {

    val asmConfig = AsmConfig(MiniGrammar(), MiniAssembly())

    val config = Config(
        "IKR Minimalprozessor",
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(RegisterContainer.RegLabel.PC, "PC", arrayOf(RegisterContainer.Register(ByteValue.Type.Dec("-1", RISCV.REG_ADDRESS_SIZE), listOf("r0"), ByteValue("0", ByteValue.Size.Bit32()), "")))
            )
        ),
        Memory(ByteValue.Size.Bit32(), "0",ByteValue.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript(),
        FlagsConditions(
            listOf(
                FlagsConditions.Flag("Carry", "Carry"),
                FlagsConditions.Flag("Overflow", "Overflow"),
                FlagsConditions.Flag("Zero", "Zero"),
                FlagsConditions.Flag("Negative", "Negative"),
            ),
            listOf(
                FlagsConditions.Condition("MI", "", calc = {
                    return@Condition it.findFlag("Carry")?.getValue() == true
                }),
                FlagsConditions.Condition("PL", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("EQ", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("NE", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("VS", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("VC", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("CS", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("CC", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("LS", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("HI", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("LT", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("GE", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("LE", "", calc = {
                    return@Condition false
                }),
                FlagsConditions.Condition("GT", "", calc = {
                    return@Condition false
                }),
            )
        )
    )

}