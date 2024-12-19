package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.ast.Component.*
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.lexer.AsmTokenType


/**
 * Addressing Modes
 * IMPORTANT: The order of the enums determines the order in which the modes will be checked!
 */
enum class T6502ParamType(val rule: Rule?, val byteAmount: Int, val exampleString: String, val description: String) {

    ZP_X(Rule { Seq(SpecNode(ASNodeType.INT_EXPR){node ->
        if(node !is cengine.lang.asm.ast.impl.ASNode.NumericExpr) return@SpecNode false
        if(node !is cengine.lang.asm.ast.impl.ASNode.NumericExpr.Operand.Number) return@SpecNode false

        (node.number.type == AsmTokenType.INT_HEX && node.number.asNumber.length == 2)
                || node.number.type == AsmTokenType.INT_BIN && node.number.asNumber.length == 8


        true

    }, Specific(","), Specific("X")) }, 2, exampleString = "$00, X", description = "zeropage, X-indexed"), // Zero Page Indexed with X: zp,x
    ZP_Y(Rule { Seq(SpecNode(ASNodeType.INT_EXPR), Specific(","), Specific("Y")) }, 2, exampleString = "$00, Y", description = "zeropage, Y-indexed"), // Zero Page Indexed with Y: zp,y

    ABS_X(Rule { Seq(SpecNode(ASNodeType.INT_EXPR), Specific(","), Specific("X")) }, 3, exampleString = "$0000, X", description = "absolute, X-indexed"), // Absolute Indexed with X: a,x
    ABS_Y(Rule { Seq(SpecNode(ASNodeType.INT_EXPR), Specific(","), Specific("Y")) }, 3, exampleString = "$0000, Y", description = "absolute, Y-indexed"), // Absolute Indexed with Y: a,y
    ZP_X_IND(Rule { Seq(Specific("("), SpecNode(ASNodeType.INT_EXPR), Specific(","), Specific("X"), Specific(")")) }, 2, exampleString = "($00, X)", description = "X-indexed, indirect"), // Zero Page Indexed Indirect: (zp,x)

    ZPIND_Y(Rule { Seq(Specific("("), SpecNode(ASNodeType.INT_EXPR), Specific(")"), Specific(","), Specific("Y")) }, 2, exampleString = "($00), Y", description = "indirect, Y-indexed"), // Zero Page Indirect Indexed with Y: (zp),y

    IND(Rule { Seq(Specific("("), SpecNode(ASNodeType.INT_EXPR), Specific(")")) }, 3, exampleString = "($0000)", description = "indirect"), // Absolute Indirect: (a)

    ACC(Rule { Seq(Specific("A")) }, 1, exampleString = "A", description = "Accumulator"), // Accumulator: A
    IMM(Rule { Seq(Specific("#"), SpecNode(ASNodeType.INT_EXPR)) }, 2, exampleString = "#$00", description = "immediate"), // Immediate: #
    REL(Rule { Seq(SpecNode(ASNodeType.INT_EXPR)) }, 2, exampleString = "$00", description = "relative"), // Relative: r
    ZP(Rule { Seq(SpecNode(ASNodeType.INT_EXPR)) }, 2, exampleString = "$00", description = "zeropage"), // Zero Page: zp
    ABS(Rule { Seq(SpecNode(ASNodeType.INT_EXPR)) }, 3, exampleString = "$0000", description = "absolute"), // Absolute: a

    IMPLIED(null, 1, exampleString = "", description = "implied"); // Implied: i
}