package cengine.lang.asm.ast.target.ikrrisc2

import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.asm.ast.Component
import cengine.lang.asm.ast.Rule

enum class IKRR2ParamType(val rule: Rule, val exampleString: String) {
    I_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(":"),
            Component.Specific("="),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.Specific("#"),
            Component.SpecNode(ASNodeType.INT_EXPR)
        )
    }, "rc := rb,#imm16"),
    R2_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(":"),
            Component.Specific("="),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.InSpecific(AsmTokenType.REGISTER),
        )
    }, "rc := rb,ra"),
    R1_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(":"),
            Component.Specific("="),
            Component.InSpecific(AsmTokenType.REGISTER)
        )
    }, "rc := rb"),
    L_OFF_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(":"),
            Component.Specific("="),
            Component.Specific("("),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.SpecNode(ASNodeType.INT_EXPR),
            Component.Specific(")")
        )
    }, "rc := (rb,disp16)"),
    L_INDEX_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(":"),
            Component.Specific("="),
            Component.Specific("("),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(")")
        )
    }, "rc := (rb,ra)"),
    S_OFF_TYPE(Rule {
        Component.Seq(
            Component.Specific("("),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.SpecNode(ASNodeType.INT_EXPR),
            Component.Specific(")"),
            Component.Specific(":"),
            Component.Specific("="),
            Component.InSpecific(AsmTokenType.REGISTER)
        )
    }, "(rb,disp16) := rc"),
    S_INDEX_TYPE(Rule {
        Component.Seq(
            Component.Specific("("),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(")"),
            Component.Specific(":"),
            Component.Specific("="),
            Component.InSpecific(AsmTokenType.REGISTER)
        )
    }, "(rb,ra) := rc"),
    B_DISP18_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER),
            Component.Specific(","),
            Component.SpecNode(ASNodeType.INT_EXPR)
        )
    }, "rc,disp18"),
    B_DISP26_TYPE(Rule {
        Component.Seq(
            Component.SpecNode(ASNodeType.INT_EXPR)
        )
    }, "disp26"),
    B_REG_TYPE(Rule {
        Component.Seq(
            Component.InSpecific(AsmTokenType.REGISTER)
        )
    }, "rb");
}