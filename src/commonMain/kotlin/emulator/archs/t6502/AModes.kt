package emulator.archs.t6502

import emulator.kit.assembler.Rule
import emulator.kit.assembler.Rule.Component.*
import emulator.kit.assembler.gas.nodes.GASNodeType

import emulator.kit.types.Variable

/**
 * Addressing Modes
 * IMPORTANT: The order of the enums determines the order in which the modes will be checked!
 */
enum class AModes(val tokenSequence: Rule?, val byteAmount: Int, val exampleString: String, val description: String) {

    ZP_X(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(","), Specific("X"))}, 2, exampleString = "$00, X", description = "zeropage, X-indexed"), // Zero Page Indexed with X: zp,x
    ZP_Y(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(","), Specific("Y"))}, 2, exampleString = "$00, Y", description = "zeropage, Y-indexed"), // Zero Page Indexed with Y: zp,y

    ABS_X(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(","), Specific("X"))}, 3, exampleString = "$0000, X", description = "absolute, X-indexed"), // Absolute Indexed with X: a,x
    ABS_Y(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(","), Specific("Y"))}, 3, exampleString = "$0000, Y", description = "absolute, Y-indexed"), // Absolute Indexed with Y: a,y
    ZP_X_IND(Rule{ Seq(Specific("("), SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(","), Specific("X"), Specific(")"))}, 2, exampleString = "($00, X)", description = "X-indexed, indirect"), // Zero Page Indexed Indirect: (zp,x)

    ZPIND_Y(Rule{ Seq( Specific("("), SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(")"), Specific(","), Specific("Y"))}, 2, exampleString = "($00), Y", description = "indirect, Y-indexed"), // Zero Page Indirect Indexed with Y: (zp),y

    IND(Rule{ Seq(Specific("("), SpecNode(GASNodeType.EXPRESSION_INTEGER), Specific(")"))}, 3, exampleString = "($0000)", description = "indirect"), // Absolute Indirect: (a)

    ACCUMULATOR(Rule{ Seq( Specific("A"))}, 1, exampleString = "A", description = "Accumulator"), // Accumulator: A
    IMM(Rule{ Seq( Specific("#"), SpecNode(GASNodeType.EXPRESSION_INTEGER))}, 2, exampleString = "#$00", description = "immediate"), // Immediate: #
    REL(Rule{ Seq( SpecNode(GASNodeType.EXPRESSION_INTEGER))}, 2, exampleString = "$00", description = "relative"), // Relative: r
    ZP(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER))}, 2, exampleString = "$00", description = "zeropage"), // Zero Page: zp
    ABS(Rule{ Seq(SpecNode(GASNodeType.EXPRESSION_INTEGER))}, 3, exampleString = "$0000", description = "absolute"), // Absolute: a

    IMPLIED(null, 1, exampleString = "", description = "implied"); // Implied: i

    fun getString(threeByte: Array<Variable.Value.Bin>): String {
        val smallVal = threeByte.get(1).toHex().getRawHexStr()
        val bigVal = threeByte.drop(1).joinToString("") { it.toHex().getRawHexStr() }
        return when (this) {
            ZP_X -> "$${smallVal}, X"
            ZP_Y -> "$${smallVal}, Y"
            ABS_X -> "$${bigVal}, X"
            ABS_Y -> "$${bigVal}, Y"
            ZP_X_IND -> "($${smallVal}, X)"
            ZPIND_Y -> "($${smallVal}), Y"
            IND -> "(${bigVal})"
            ACCUMULATOR -> "A"
            IMM -> "#$${smallVal}"
            REL -> "$${smallVal}"
            ZP -> "$${smallVal}"
            ABS -> "$${bigVal}"
            IMPLIED -> ""
        }
    }
}