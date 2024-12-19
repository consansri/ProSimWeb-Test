package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.target.t6502.T6502ParamType.*
import cengine.util.integer.UInt8.Companion.toUInt8

enum class T6502InstrType(override val detectionName: String, val opCode: UByte, val aMode: T6502ParamType, val description: String, val labelDependent: Boolean = false) : InstrTypeInterface {
    // Load, store, interregister transfer
    LDA_ABS("LDA", 0xADU, ABS, "load accumulator"),
    LDA_ABS_X("LDA", 0xBDU, ABS_X, "load accumulator"),
    LDA_ABS_Y("LDA", 0xB9U, ABS_Y, "load accumulator"),
    LDA_IMM("LDA", 0xA9U, IMM, "load accumulator"),
    LDA_ZP("LDA", 0xA5U, ZP, "load accumulator"),
    LDA_ZP_X_IND("LDA", 0xA1U, ZP_X_IND, "load accumulator"),
    LDA_ZP_X("LDA", 0xB5U, ZP_X, "load accumulator"),
    LDA_ZPIND_Y("LDA", 0xB1U, ZPIND_Y, "load accumulator"),

    LDX_ABS("LDX", 0xAEU, ABS, "load X"),
    LDX_ABS_Y("LDX", 0xBEU, ABS_Y, "load X"),
    LDX_IMM("LDX", 0xA2U, IMM, "load X"),
    LDX_ZP("LDX", 0xA6U, ZP, "load X"),
    LDX_ZP_Y("LDX", 0xB6U, ZP_Y, "load X"),

    LDY_ABS("LDY", 0xACU, ABS, "load Y"),
    LDY_ABS_X("LDY", 0xBCU, ABS_X, "load Y"),
    LDY_IMM("LDY", 0xA0U, IMM, "load Y"),
    LDY_ZP("LDY", 0xA4U, ZP, "load Y"),
    LDY_ZP_X("LDY", 0xB4U, ZP_X, "load Y"),

    // STA instructions
    STA_ABS("STA", 0x8DU, ABS, "store accumulator"),
    STA_ABS_X("STA", 0x9DU, ABS_X, "store accumulator"),
    STA_ABS_Y("STA", 0x99U, ABS_Y, "store accumulator"),
    STA_ZP("STA", 0x85U, ZP, "store accumulator"),
    STA_ZP_X_IND("STA", 0x81U, ZP_X_IND, "store accumulator"),
    STA_ZP_X("STA", 0x95U, ZP_X, "store accumulator"),
    STA_ZPIND_Y("STA", 0x91U, ZPIND_Y, "store accumulator"),

    STX_ABS("STX", 0x8EU, ABS, "store X"),
    STX_ZP("STX", 0x86U, ZP, "store X"),
    STX_ZP_Y("STX", 0x96U, ZP_Y, "store X"),

    STY_ABS("STY", 0x8CU, ABS, "store Y"),
    STY_ZP("STY", 0x84U, ZP, "store Y"),
    STY_ZP_X("STY", 0x94U, ZP_X, "store Y"),

    TAX_IMPLIED("TAX", 0xAAU, IMPLIED, "transfer accumulator to X"),
    TAY_IMPLIED("TAY", 0xA8U, IMPLIED, "transfer accumulator to Y"),
    TSX_IMPLIED("TSX", 0xBAU, IMPLIED, "transfer stack pointer to X"),
    TXA_IMPLIED("TXA", 0x8AU, IMPLIED, "transfer X to accumulator"),
    TXS_IMPLIED("TXS", 0x9AU, IMPLIED, "transfer X to stack pointer"),
    TYA_IMPLIED("TYA", 0x98U, IMPLIED, "transfer Y to accumulator"),

    // Stack instructions
    PHA("PHA", 0x48U, IMPLIED, "push accumulator"),
    PHP("PHP", 0x08U, IMPLIED, "push processor status (SR)"),
    PLA("PLA", 0x68U, IMPLIED, "pull accumulator"),
    PLP("PLP", 0x28U, IMPLIED, "pull processor status (SR)"),

    // Decrements, Increments
    DEC_ABS("DEC", 0xCEU, ABS, "decrement"),
    DEC_ABS_X("DEC", 0xDEU, ABS_X, "decrement"),
    DEC_ZP("DEC", 0xC6U, ZP, "decrement"),
    DEC_ZP_X("DEC", 0xD6U, ZP_X, "decrement"),

    DEX_IMPLIED("DEX", 0xCAU, IMPLIED, "decrement X"),
    DEY_IMPLIED("DEY", 0x88U, IMPLIED, "decrement Y"),

    INC_ABS("INC", 0xEEU, ABS, "increment"),
    INC_ABS_X("INC", 0xFEU, ABS_X, "increment"),
    INC_ZP("INC", 0xE6U, ZP, "increment"),
    INC_ZP_X("INC", 0xF6U, ZP_X, "increment"),

    INX_IMPLIED("INX", 0xE8U, IMPLIED, "increment X"),
    INY_IMPLIED("INY", 0xC8U, IMPLIED, "increment Y"),

    // Arithmetic Operations
    ADC_ABS("ADC", 0x6DU, ABS, "add with carry"),
    ADC_ABS_X("ADC", 0x7DU, ABS_X, "add with carry"),
    ADC_ABS_Y("ADC", 0x79U, ABS_Y, "add with carry"),
    ADC_IMM("ADC", 0x69U, IMM, "add with carry"),
    ADC_ZP("ADC", 0x65U, ZP, "add with carry"),
    ADC_ZP_X_IND("ADC", 0x61U, ZP_X_IND, "add with carry"),
    ADC_ZP_X("ADC", 0x75U, ZP_X, "add with carry"),
    ADC_ZPIND_Y("ADC", 0x71U, ZPIND_Y, "add with carry"),

    SBC_ABS("SBC", 0xEDU, ABS, "subtract with carry"),
    SBC_ABS_X("SBC", 0xFDU, ABS_X, "subtract with carry"),
    SBC_ABS_Y("SBC", 0xF9U, ABS_Y, "subtract with carry"),
    SBC_IMM("SBC", 0xE9U, IMM, "subtract with carry"),
    SBC_ZP("SBC", 0xE5U, ZP, "subtract with carry"),
    SBC_ZP_X_IND("SBC", 0xE1U, ZP_X_IND, "subtract with carry"),
    SBC_ZP_X("SBC", 0xF5U, ZP_X, "subtract with carry"),
    SBC_ZPIND_Y("SBC", 0xF1U, ZPIND_Y, "subtract with carry"),

    CMP_ABS("CMP", 0xCDU, ABS, "compare"),
    CMP_ABS_X("CMP", 0xDDU, ABS_X, "compare"),
    CMP_ABS_Y("CMP", 0xD9U, ABS_Y, "compare"),
    CMP_IMM("CMP", 0xC9U, IMM, "compare"),
    CMP_ZP("CMP", 0xC5U, ZP, "compare"),
    CMP_ZP_X_IND("CMP", 0xC1U, ZP_X_IND, "compare"),
    CMP_ZP_X("CMP", 0xD5U, ZP_X, "compare"),
    CMP_ZPIND_Y("CMP", 0xD1U, ZPIND_Y, "compare"),

    CPX_ABS("CPX", 0xECU, ABS, "compare X"),
    CPX_IMM("CPX", 0xE0U, IMM, "compare X"),
    CPX_ZP("CPX", 0xE4U, ZP, "compare X"),

    CPY_ABS("CPY", 0xCCU, ABS, "compare Y"),
    CPY_IMM("CPY", 0xC0U, IMM, "compare Y"),
    CPY_ZP("CPY", 0xC4U, ZP, "compare Y"),

    // Logic Operations
    AND_ABS("AND", 0x2DU, ABS, "and (with accumulator)"),
    AND_ABS_X("AND", 0x3DU, ABS_X, "and (with accumulator)"),
    AND_ABS_Y("AND", 0x39U, ABS_Y, "and (with accumulator)"),
    AND_IMM("AND", 0x29U, IMM, "and (with accumulator)"),
    AND_ZP("AND", 0x25U, ZP, "and (with accumulator)"),
    AND_ZP_X_IND("AND", 0x21U, ZP_X_IND, "and (with accumulator)"),
    AND_ZP_X("AND", 0x35U, ZP_X, "and (with accumulator)"),
    AND_ZPIND_Y("AND", 0x31U, ZPIND_Y, "and (with accumulator)"),

    EOR_ABS("EOR", 0x4DU, ABS, "exclusive or"),
    EOR_ABS_X("EOR", 0x5DU, ABS_X, "exclusive or"),
    EOR_ABS_Y("EOR", 0x59U, ABS_Y, "exclusive or"),
    EOR_IMM("EOR", 0x49U, IMM, "exclusive or"),
    EOR_ZP("EOR", 0x45U, ZP, "exclusive or"),
    EOR_ZP_X_IND("EOR", 0x41U, ZP_X_IND, "exclusive or"),
    EOR_ZP_X("EOR", 0x55U, ZP_X, "exclusive or"),
    EOR_ZPIND_Y("EOR", 0x51U, ZPIND_Y, "exclusive or"),

    ORA_ABS("ORA", 0x0DU, ABS, "or (with accumulator)"),
    ORA_ABS_X("ORA", 0x1DU, ABS_X, "or (with accumulator)"),
    ORA_ABS_Y("ORA", 0x19U, ABS_Y, "or (with accumulator)"),
    ORA_IMM("ORA", 0x09U, IMM, "or (with accumulator)"),
    ORA_ZP("ORA", 0x05U, ZP, "or (with accumulator)"),
    ORA_ZP_X_IND("ORA", 0x01U, ZP_X_IND, "or (with accumulator)"),
    ORA_ZP_X("ORA", 0x15U, ZP_X, "or (with accumulator)"),
    ORA_ZPIND_Y("ORA", 0x11U, ZPIND_Y, "or (with accumulator)"),

    BIT_ABS("BIT", 0x2CU, ABS, "test bits in memory with accumulator"),
    BIT_IMM("BIT", 0x89U, IMM, "test bits in memory with accumulator"),
    BIT_ZP("BIT", 0x24U, ZP, "test bits in memory with accumulator"),

    // Shifts and Rotates
    ASL_ABS("ASL", 0x0EU, ABS, "arithmetic shift left"),
    ASL_ABS_X("ASL", 0x1EU, ABS_X, "arithmetic shift left"),
    ASL_ACC("ASL", 0x0AU, ACC, "arithmetic shift left"),
    ASL_ZP("ASL", 0x06U, ZP, "arithmetic shift left"),
    ASL_ZP_X("ASL", 0x16U, ZP_X, "arithmetic shift left"),

    LSR_ABS("LSR", 0x4EU, ABS, "logical shift right"),
    LSR_ABS_X("LSR", 0x5EU, ABS_X, "logical shift right"),
    LSR_ACC("LSR", 0x4AU, ACC, "logical shift right"),
    LSR_ZP("LSR", 0x46U, ZP, "logical shift right"),
    LSR_ZP_X("LSR", 0x56U, ZP_X, "logical shift right"),

    ROL_ABS("ROL", 0x2EU, ABS, "rotate left"),
    ROL_ABS_X("ROL", 0x3EU, ABS_X, "rotate left"),
    ROL_ACC("ROL", 0x2AU, ACC, "rotate left"),
    ROL_ZP("ROL", 0x26U, ZP, "rotate left"),
    ROL_ZP_X("ROL", 0x36U, ZP_X, "rotate left"),

    ROR_ABS("ROR", 0x6EU, ABS, "rotate right"),
    ROR_ABS_X("ROR", 0x7EU, ABS_X, "rotate right"),
    ROR_ACC("ROR", 0x6AU, ACC, "rotate right"),
    ROR_ZP("ROR", 0x66U, ZP, "rotate right"),
    ROR_ZP_X("ROR", 0x76U, ZP_X, "rotate right"),

    // Flags
    CLC("CLC", 0x18U, IMPLIED, "clear carry"),
    CLD("CLD", 0xDAU, IMPLIED, "clear decimal"),
    CLI("CLI", 0x58U, IMPLIED, "clear interrupt disable"),
    CLV("CLV", 0xB8U, IMPLIED, "clear overflow"),
    SEC("SEC", 0x38U, IMPLIED, "set carry"),
    SED("SED", 0xF8U, IMPLIED, "set decimal"),
    SEI("SEI", 0x78U, IMPLIED, "set interrupt disable"),

    // Jumps, Calls, Returns
    JMP_ABS("JMP", 0x4CU, ABS, "jump", true),
    JMP_IND("JMP", 0x6CU, IND, "jump"),
    JSR_ABS("JSR", 0x20U, ABS, "jump to subroutine", true),
    RTS_IMPLIED("RTS", 0x60U, IMPLIED, "return from subroutine"),
    RTI_IMPLIED("RTI", 0x40U, IMPLIED, "return from interrupt"),

    // Branching
    BCC_REL("BCC", 0x90U, REL, "branch if carry clear", true),
    BCS_REL("BCS", 0xB0U, REL, "branch if carry set", true),
    BEQ_REL("BEQ", 0xF0U, REL, "branch if equal", true),
    BMI_REL("BMI", 0x30U, REL, "branch if minus", true),
    BNE_REL("BNE", 0xD0U, REL, "branch if not equal", true),
    BPL_REL("BPL", 0x10U, REL, "branch on plus", true),
    BVC_REL("BVC", 0x50U, REL, "branch on overflow clear", true),
    BVS_REL("BVS", 0x70U, REL, "branch on overflow set", true),

    BRK_IMPLIED("BRK", 0x00U, IMPLIED, "break / interrupt"),

    NOP("NOP", 0xeaU, IMPLIED, "no operation"),
    ;

    override val inCodeInfo: String?
        get() = description

    override val typeName: String
        get() = name

    override val addressInstancesNeeded: Int = aMode.byteAmount

    override val paramRule: Rule?
        get() = aMode.rule

    override fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction) {
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (aMode) {
            ZP_X, ZP_Y, ZP, ZP_X_IND, ZPIND_Y -> {
                val eval = exprs[0].evaluate(builder)

                if (!eval.fitsInSignedOrUnsigned(8)) {
                    exprs[0].addError("$eval exceeds 8 bits")
                }

                // Append Opcode
                builder.currentSection.content.put(opCode.toUInt8())

                // Append Operand
                builder.currentSection.content.put(eval.toUInt8())
            }

            ABS_X -> {
                val eval = exprs[0].evaluate(builder)

                if (!eval.fitsInSignedOrUnsigned(16)) {
                    exprs[0].addError("$eval exceeds 16 bits")
                }

                // Append Opcode
                builder.currentSection.content.put(opCode.toUInt8())

                // Append Operand
                builder.currentSection.content.put(eval.toUInt8())
            }
            ABS_Y -> TODO()
            IND -> TODO()
            ACC -> TODO()
            IMM -> TODO()
            REL -> {
                // Evaluate Later
                builder.currentSection.queueLateInit(instr, 2)
            }
            ABS -> TODO()
            IMPLIED -> TODO()
        }
    }

    override fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int) {
        TODO("Not yet implemented")
    }
}