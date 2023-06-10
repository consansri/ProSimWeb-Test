package extendable.archs.mini

import extendable.ArchConst
import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.Address
import extendable.components.types.ExtensionType
import extendable.components.types.OpCode

object Mini {

    val config = Config(
        "IKR Minimalprozessor",
        arrayOf(
            Register(Address(0, 32), "r0", 0, "", 32),
        ),
        listOf(
            Instruction("name", listOf(ArchConst.EXTYPE_REGISTER), OpCode("0101010"), "", "", ::add)
        ),
        Memory(32, 4),
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

    fun add(extensionList: List<ExtensionType>, memory: Memory, registers: Array<Register>, flagsConditions: FlagsConditions?): Boolean {
        return false
    }

}