package emulator.archs.ikrrisc2

import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.lexer.Token.Type
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.syntax.Rule

enum class ParamType(val rule: Rule, val exampleString: String) {
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
    }, "rb");

    fun getContentString(instr: IKRRisc2Assembler.IKRRisc2Instr): String {
        return when(this){
            I_TYPE -> "${instr.regs[0]} := ${instr.regs[1]},#${instr.immediate}"
            R_TYPE -> "${instr.regs[0]} := ${instr.regs[1]},${instr.regs[2]}"
            R_TYPE_1S -> "${instr.regs[0]} := ${instr.regs[1]}"
            L_TYPE_OFF -> "${instr.regs[0]} := (${instr.regs[1]},${instr.immediate})"
            L_TYPE_INDEX -> "${instr.regs[0]} := (${instr.regs[1]},${instr.regs[2]})"
            S_TYPE_OFF -> "(${instr.regs[0]},${instr.immediate}) := ${instr.regs[1]}"
            S_TYPE_INDEX -> "(${instr.regs[0]},${instr.regs[1]}) := ${instr.regs[2]}"
            BRANCH_DISP18 -> "${instr.regs[0]},${instr.label?.evaluate(false)}"
            J_DISP26 -> "${instr.label?.evaluate(false)}"
            J_RB -> "${instr.regs[0]}"
        }
    }
}