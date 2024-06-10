package emulator.archs.ikrrisc2

import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.lexer.Token.Type
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.syntax.Rule

enum class ParamType(rule: Rule, exampleString: String) {
    I_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER),
            Specific(","),
            Specific("#"),
            SpecNode(GASNodeType.INT_EXPR)
        )
    }, "rc := rb,#imm16"),
    R_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER),
            Specific(","),
            InSpecific(Type.REGISTER),
        )
    }, "rc := rb,ra"),
    R_TYPE_1S(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER)
        )
    }, "rc := rb"),
    L_TYPE_OFF(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            Specific("("),
            InSpecific(Type.REGISTER),
            Specific(","),
            SpecNode(GASNodeType.INT_EXPR),
            Specific(")")
        )
    }, "rc := (rb,disp16)"),
    L_TYPE_INDEX(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            Specific("("),
            InSpecific(Type.REGISTER),
            Specific(","),
            InSpecific(Type.REGISTER),
            Specific(")")
        )
    }, "rc := (rb,ra)"),
    S_TYPE_OFF(Rule {
        Seq(
            Specific("("),
            InSpecific(Type.REGISTER),
            Specific(","),
            SpecNode(GASNodeType.INT_EXPR),
            Specific(")"),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER)
        )
    }, "(rb,disp16) := rc"),
    S_TYPE_INDEX(Rule {
        Seq(
            Specific("("),
            InSpecific(Type.REGISTER),
            Specific(","),
            InSpecific(Type.REGISTER),
            Specific(")"),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER)
        )
    }, "(rb,ra) := rc"),
    BRANCH_DISP18(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(","),
            SpecNode(GASNodeType.INT_EXPR)
        )
    }, "rc,disp18"),
    J_DISP26(Rule {
        SpecNode(GASNodeType.INT_EXPR)
    }, "disp26"),
    J_RB(Rule {
        InSpecific(Type.REGISTER)
    }, "rb")
}