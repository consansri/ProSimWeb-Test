package cengine.lang.asm.ast.target.t6502

import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule

enum class T6502InstrType(override val detectionName: String, val opCode: UByte, val aMode: T6502ParamType, val description: String, override val isPseudo: Boolean = false) : InstrTypeInterface {
    // Load, store, interregister transfer
    LDA_ABS("LDA", 0xADU, T6502ParamType.ABS, "load accumulator"),
    LDA_ABS_X("LDA", 0xBDU,T6502ParamType.ABS_X, "load accumulator"),
    LDA_ABS_Y("LDA", 0xB9U, T6502ParamType.ABS_Y, "load accumulator"),
    LDA_IMM("LDA", 0xA9U, T6502ParamType.IMM, "load accumulator"),
    LDA_ZP("LDA", 0xA5U, T6502ParamType.ZP, "load accumulator"),
    LDA_ZP_X_IND("LDA", 0xA1U, T6502ParamType.ZP_X_IND, "load accumulator"),
    LDA_ZP_X("LDA", 0xB5U, T6502ParamType.ZP_X, "load accumulator"),
    LDA_ZPIND_Y("LDA", 0xB1U, T6502ParamType.ZPIND_Y, "load accumulator"),

    LDX_ABS("LDX", 0xAEU, T6502ParamType.ABS, "load X"),
    LDX_ABS_Y("LDX", 0xBEU, T6502ParamType.ABS_Y, "load X"),
    LDX_IMM("LDX", 0xA2U, T6502ParamType.IMM, "load X"),
    LDX_ZP("LDX", 0xA6U, T6502ParamType.ZP, "load X"),
    LDX_ZP_Y("LDX", 0xB6U, T6502ParamType.ZP_Y, "load X"),

    LDY_ABS("LDY", 0xACU, T6502ParamType.ABS, "load Y"),
    LDY_ABS_X("LDY", 0xBCU, T6502ParamType.ABS_X, "load Y"),
    LDY_IMM("LDY", 0xA0U, T6502ParamType.IMM, "load Y"),
    LDY_ZP("LDY", 0xA4U, T6502ParamType.ZP, "load Y"),
    LDY_ZP_X("LDY", 0xB4U, T6502ParamType.ZP_X, "load Y"),

    // STA instructions
    STA_ABS("STA", 0x8DU, T6502ParamType.ABS, "store accumulator"),
    STA_ABS_X("STA", 0x9DU, T6502ParamType.ABS_X, "store accumulator"),
    STA_ABS_Y("STA", 0x99U, T6502ParamType.ABS_Y, "store accumulator"),
    STA_ZP("STA", 0x85U, T6502ParamType.ZP, "store accumulator"),
    STA_ZP_X_IND("STA", 0x81U, T6502ParamType.ZP_X_IND, "store accumulator"),
    STA_ZP_X("STA", 0x95U, T6502ParamType.ZP_X, "store accumulator"),
    STA_ZPIND_Y("STA", 0x91U, T6502ParamType.ZPIND_Y, "store accumulator"),

    STX_ABS("STX", 0x8EU, T6502ParamType.ABS, "store X"),
    STX_ZP("STX", 0x86U, T6502ParamType.ZP, "store X"),
    STX_ZP_Y("STX", 0x96U, T6502ParamType.ZP_Y, "store X"),

    STY_ABS("STY", 0x8CU, T6502ParamType.ABS, "store Y"),
    STY_ZP("STY", 0x84U, T6502ParamType.ZP, "store Y"),
    STY_ZP_X("STY", 0x94U, T6502ParamType.ZP_X, "store Y"),

    TAX_IMPLIED("TAX", 0xAAU, T6502ParamType.IMPLIED, "transfer accumulator to X"),
    TAY_IMPLIED("TAY", 0xA8U, T6502ParamType.IMPLIED, "transfer accumulator to Y"),
    TSX_IMPLIED("TSX", 0xBAU, T6502ParamType.IMPLIED, "transfer stack pointer to X"),
    TXA_IMPLIED("TXA", 0x8AU, T6502ParamType.IMPLIED, "transfer X to accumulator"),
    TXS_IMPLIED("TXS", 0x9AU, T6502ParamType.IMPLIED, "transfer X to stack pointer"),
    TYA_IMPLIED("TYA", 0x98U, T6502ParamType.IMPLIED, "transfer Y to accumulator"),

    // Stack instructions
    PHA("PHA", 0x48U, T6502ParamType.IMPLIED, "push accumulator"),
    PHP("PHP", 0x08U, T6502ParamType.IMPLIED, "push processor status (SR)"),
    PLA("PLA", 0x68U, T6502ParamType.IMPLIED, "pull accumulator"),
    PLP("PLP", 0x28U, T6502ParamType.IMPLIED, "pull processor status (SR)"),

    // Decrements, Increments
    DEC_ABS("DEC", 0xCEU, T6502ParamType.ABS, "decrement"),
    DEC_ABS_X("DEC", 0xDEU, T6502ParamType.ABS_X, "decrement"),
    DEC_ZP("DEC", 0xC6U, T6502ParamType.ZP, "decrement"),
    DEC_ZP_X("DEC", 0xD6U, T6502ParamType.ZP_X, "decrement"),

    DEX_IMPLIED("DEX", 0xCAU, T6502ParamType.IMPLIED, "decrement X"),
    DEY_IMPLIED("DEY", 0x88U, T6502ParamType.IMPLIED, "decrement Y"),

    INC_ABS("INC", 0xEEU, T6502ParamType.ABS, "increment"),
    INC_ABS_X("INC", 0xFEU, T6502ParamType.ABS_X, "increment"),
    INC_ZP("INC", 0xE6U, T6502ParamType.ZP, "increment"),
    INC_ZP_X("INC", 0xF6U, T6502ParamType.ZP_X, "increment"),

    INX_IMPLIED("INX", 0xE8U, T6502ParamType.IMPLIED, "increment X"),
    INY_IMPLIED("INY", 0xC8U, T6502ParamType.IMPLIED, "increment Y"),

    // Arithmetic Operations
    ADC_ABS("ADC", 0x6DU, T6502ParamType.ABS, "add with carry"),
    ADC_ABS_X("ADC", 0x7DU, T6502ParamType.ABS_X, "add with carry"),
    ADC_ABS_Y("ADC", 0x79U, T6502ParamType.ABS_Y, "add with carry"),
    ADC_IMM("ADC", 0x69U, T6502ParamType.IMM, "add with carry"),
    ADC_ZP("ADC", 0x65U, T6502ParamType.ZP, "add with carry"),
    ADC_ZP_X_IND("ADC", 0x61U, T6502ParamType.ZP_X_IND, "add with carry"),
    ADC_ZP_X("ADC", 0x75U, T6502ParamType.ZP_X, "add with carry"),
    ADC_ZPIND_Y("ADC", 0x71U, T6502ParamType.ZPIND_Y, "add with carry"),

    SBC_ABS("SBC", 0xEDU, T6502ParamType.ABS, "subtract with carry"),
    SBC_ABS_X("SBC", 0xFDU, T6502ParamType.ABS_X, "subtract with carry"),
    SBC_ABS_Y("SBC", 0xF9U, T6502ParamType.ABS_Y, "subtract with carry"),
    SBC_IMM("SBC", 0xE9U, T6502ParamType.IMM, "subtract with carry"),
    SBC_ZP("SBC", 0xE5U, T6502ParamType.ZP, "subtract with carry"),
    SBC_ZP_X_IND("SBC", 0xE1U, T6502ParamType.ZP_X_IND, "subtract with carry"),
    SBC_ZP_X("SBC", 0xF5U, T6502ParamType.ZP_X, "subtract with carry"),
    SBC_ZPIND_Y("SBC", 0xF1U, T6502ParamType.ZPIND_Y, "subtract with carry"),

    CMP_ABS("CMP", 0xCDU, T6502ParamType.ABS, "compare"),
    CMP_ABS_X("CMP", 0xDDU, T6502ParamType.ABS_X, "compare"),
    CMP_ABS_Y("CMP", 0xD9U, T6502ParamType.ABS_Y, "compare"),
    CMP_IMM("CMP", 0xC9U, T6502ParamType.IMM, "compare"),
    CMP_ZP("CMP", 0xC5U, T6502ParamType.ZP, "compare"),
    CMP_ZP_X_IND("CMP", 0xC1U, T6502ParamType.ZP_X_IND, "compare"),
    CMP_ZP_X("CMP", 0xD5U, T6502ParamType.ZP_X, "compare"),
    CMP_ZPIND_Y("CMP", 0xD1U, T6502ParamType.ZPIND_Y, "compare"),

    CPX_ABS("CPX", 0xECU, T6502ParamType.ABS, "compare X"),
    CPX_IMM("CPX", 0xE0U, T6502ParamType.IMM, "compare X"),
    CPX_ZP("CPX", 0xE4U, T6502ParamType.ZP, "compare X"),

    CPY_ABS("CPY", 0xCCU, T6502ParamType.ABS, "compare Y"),
    CPY_IMM("CPY", 0xC0U, T6502ParamType.IMM, "compare Y"),
    CPY_ZP("CPY", 0xC4U, T6502ParamType.ZP, "compare Y"),

    // Logic Operations
    AND_ABS("AND", 0x2DU, T6502ParamType.ABS, "and (with accumulator)"),
    AND_ABS_X("AND", 0x3DU, T6502ParamType.ABS_X, "and (with accumulator)"),
    AND_ABS_Y("AND", 0x39U, T6502ParamType.ABS_Y, "and (with accumulator)"),
    AND_IMM("AND", 0x29U, T6502ParamType.IMM, "and (with accumulator)"),
    AND_ZP("AND", 0x25U, T6502ParamType.ZP, "and (with accumulator)"),
    AND_ZP_X_IND("AND", 0x21U, T6502ParamType.ZP_X_IND, "and (with accumulator)"),
    AND_ZP_X("AND", 0x35U, T6502ParamType.ZP_X, "and (with accumulator)"),
    AND_ZPIND_Y("AND", 0x31U, T6502ParamType.ZPIND_Y, "and (with accumulator)"),

    EOR_ABS("EOR", 0x4DU, T6502ParamType.ABS, "exclusive or"),
    EOR_ABS_X("EOR", 0x5DU, T6502ParamType.ABS_X, "exclusive or"),
    EOR_ABS_Y("EOR", 0x59U, T6502ParamType.ABS_Y, "exclusive or"),
    EOR_IMM("EOR", 0x49U, T6502ParamType.IMM, "exclusive or"),
    EOR_ZP("EOR", 0x45U, T6502ParamType.ZP, "exclusive or"),
    EOR_ZP_X_IND("EOR", 0x41U, T6502ParamType.ZP_X_IND, "exclusive or"),
    EOR_ZP_X("EOR", 0x55U, T6502ParamType.ZP_X, "exclusive or"),
    EOR_ZPIND_Y("EOR", 0x51U, T6502ParamType.ZPIND_Y, "exclusive or"),

    ORA_ABS("ORA", 0x0DU, T6502ParamType.ABS, "or (with accumulator)"),
    ORA_ABS_X("ORA", 0x1DU, T6502ParamType.ABS_X, "or (with accumulator)"),
    ORA_ABS_Y("ORA", 0x19U, T6502ParamType.ABS_Y, "or (with accumulator)"),
    ORA_IMM("ORA", 0x09U, T6502ParamType.IMM, "or (with accumulator)"),
    ORA_ZP("ORA", 0x05U, T6502ParamType.ZP, "or (with accumulator)"),
    ORA_ZP_X_IND("ORA", 0x01U, T6502ParamType.ZP_X_IND, "or (with accumulator)"),
    ORA_ZP_X("ORA", 0x15U, T6502ParamType.ZP_X, "or (with accumulator)"),
    ORA_ZPIND_Y("ORA", 0x11U, T6502ParamType.ZPIND_Y, "or (with accumulator)"),

    BIT_ABS("BIT", 0x2CU, T6502ParamType.ABS, "test bits in memory with accumulator"),
    BIT_IMM("BIT", 0x89U, T6502ParamType.IMM, "test bits in memory with accumulator"),
    BIT_ZP("BIT", 0x24U, T6502ParamType.ZP, "test bits in memory with accumulator"),

    // Shifts and Rotates
    ASL_ABS("ASL", 0x0EU, T6502ParamType.ABS, "arithmetic shift left"),
    ASL_ABS_X("ASL", 0x1EU, T6502ParamType.ABS_X, "arithmetic shift left"),
    ASL_ACC("ASL", 0x0AU, T6502ParamType.ACC, "arithmetic shift left"),
    ASL_ZP("ASL", 0x06U, T6502ParamType.ZP, "arithmetic shift left"),
    ASL_ZP_X("ASL", 0x16U, T6502ParamType.ZP_X, "arithmetic shift left"),

    LSR_ABS("LSR", 0x4EU, T6502ParamType.ABS, "logical shift right"),
    LSR_ABS_X("LSR", 0x5EU, T6502ParamType.ABS_X, "logical shift right"),
    LSR_ACC("LSR", 0x4AU, T6502ParamType.ACC, "logical shift right"),
    LSR_ZP("LSR", 0x46U, T6502ParamType.ZP, "logical shift right"),
    LSR_ZP_X("LSR", 0x56U, T6502ParamType.ZP_X, "logical shift right"),

    ROL_ABS("ROL", 0x2EU, T6502ParamType.ABS, "rotate left"),
    ROL_ABS_X("ROL", 0x3EU, T6502ParamType.ABS_X, "rotate left"),
    ROL_ACC("ROL", 0x2AU, T6502ParamType.ACC, "rotate left"),
    ROL_ZP("ROL", 0x26U, T6502ParamType.ZP, "rotate left"),
    ROL_ZP_X("ROL", 0x36U, T6502ParamType.ZP_X, "rotate left"),

    ROR_ABS("ROR", 0x6EU, T6502ParamType.ABS, "rotate right"),
    ROR_ABS_X("ROR", 0x7EU, T6502ParamType.ABS_X, "rotate right"),
    ROR_ACC("ROR", 0x6AU, T6502ParamType.ACC, "rotate right"),
    ROR_ZP("ROR", 0x66U, T6502ParamType.ZP, "rotate right"),
    ROR_ZP_X("ROR", 0x76U, T6502ParamType.ZP_X, "rotate right"),

    // Flags
    CLC("CLC", 0x18U, T6502ParamType.IMPLIED,"clear carry"),
    CLD("CLD", 0xDAU, T6502ParamType.IMPLIED, "clear decimal"),
    CLI("CLI", 0x58U, T6502ParamType.IMPLIED, "clear interrupt disable"),
    CLV("CLV", 0xB8U, T6502ParamType.IMPLIED, "clear overflow"),
    SEC("SEC", 0x38U, T6502ParamType.IMPLIED, "set carry"),
    SED("SED", 0xF8U, T6502ParamType.IMPLIED, "set decimal"),
    SEI("SEI", 0x78U, T6502ParamType.IMPLIED, "set interrupt disable"),

    // Jumps, Calls, Returns
    JMP_ABS("JMP", 0x4CU, T6502ParamType.ABS, "jump"),
    JMP_IND("JMP", 0x6CU, T6502ParamType.IND, "jump"),
    JSR_ABS("JSR", 0x20U, T6502ParamType.ABS, "jump to subroutine"),
    RTS_IMPLIED("RTS", 0x60U, T6502ParamType.IMPLIED, "return from subroutine"),
    RTI_IMPLIED("RTI", 0x40U, T6502ParamType.IMPLIED, "return from interrupt"),

    // Branching
    BCC_REL("BCC", 0x90U, T6502ParamType.REL, "branch if carry clear"),
    BCS_REL("BCS", 0xB0U, T6502ParamType.REL, "branch if carry set"),
    BEQ_REL("BEQ", 0xF0U, T6502ParamType.REL, "branch if equal"),
    BMI_REL("BMI", 0x30U, T6502ParamType.REL, "branch if minus"),
    BNE_REL("BNE", 0xD0U, T6502ParamType.REL, "branch if not equal"),
    BPL_REL("BPL", 0x10U, T6502ParamType.REL, "branch on plus"),
    BVC_REL("BVC", 0x50U, T6502ParamType.REL, "branch on overflow clear"),
    BVS_REL("BVS", 0x70U, T6502ParamType.REL, "branch on overflow set"),

    BRK_IMPLIED("BRK", 0x00U, T6502ParamType.IMPLIED, "break / interrupt"),

    NOP("NOP", 0xeaU, T6502ParamType.IMPLIED, "no operation"),    ;

    override val typeName: String
        get() = name

    override val bytesNeeded: Int = aMode.byteAmount
    override val paramRule: Rule?
        get() = aMode.rule
}