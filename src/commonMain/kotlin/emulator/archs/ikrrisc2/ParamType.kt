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
    R2_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER),
            Specific(","),
            InSpecific(Type.REGISTER),
        )
    }, "rc := rb,ra"),
    R1_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(":"),
            Specific("="),
            InSpecific(Type.REGISTER)
        )
    }, "rc := rb"),
    L_OFF_TYPE(Rule {
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
    L_INDEX_TYPE(Rule {
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
    S_OFF_TYPE(Rule {
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
    S_INDEX_TYPE(Rule {
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
    B_DISP18_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER),
            Specific(","),
            SpecNode(GASNodeType.INT_EXPR)
        )
    }, "rc,disp18"),
    B_DISP26_TYPE(Rule {
        Seq(
            SpecNode(GASNodeType.INT_EXPR)
        )
    }, "disp26"),
    B_REG_TYPE(Rule {
        Seq(
            InSpecific(Type.REGISTER)
        )
    }, "rb");

    fun getContentString(instr: IKRRisc2Assembler.IKRRisc2Instr): String {
        return when(this){
            I_TYPE -> "${instr.regs[0]} := ${instr.regs[1]},#${instr.immediate}"
            R2_TYPE -> "${instr.regs[0]} := ${instr.regs[1]},${instr.regs[2]}"
            R1_TYPE -> "${instr.regs[0]} := ${instr.regs[1]}"
            L_OFF_TYPE -> "${instr.regs[0]} := (${instr.regs[1]},${instr.immediate})"
            L_INDEX_TYPE -> "${instr.regs[0]} := (${instr.regs[1]},${instr.regs[2]})"
            S_OFF_TYPE -> "(${instr.regs[0]},${instr.immediate}) := ${instr.regs[1]}"
            S_INDEX_TYPE -> "(${instr.regs[0]},${instr.regs[1]}) := ${instr.regs[2]}"
            B_DISP18_TYPE -> "${instr.regs[0]},${instr.displacement}"
            B_DISP26_TYPE -> "${instr.displacement}"
            B_REG_TYPE -> "${instr.regs[0]}"
        }
    }
}