package cengine.lang.asm.ast.target.riscv.rv32

import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule

enum class RV32InstrType(override val detectionName: String, val isPseudo: Boolean, val paramType: RV32ParamType, override val bytesNeeded: Int? = 4) : InstrTypeInterface {
    LUI("LUI", false, RV32ParamType.RD_I20),
    AUIPC("AUIPC", false, RV32ParamType.RD_I20),
    JAL("JAL", false, RV32ParamType.RD_I20),
    JALR("JALR", false, RV32ParamType.RD_RS1_I12),
    ECALL("ECALL", false, RV32ParamType.NONE),
    EBREAK("EBREAK", false, RV32ParamType.NONE),
    BEQ("BEQ", false, RV32ParamType.RS1_RS2_LBL),
    BNE("BNE", false, RV32ParamType.RS1_RS2_LBL),
    BLT("BLT", false, RV32ParamType.RS1_RS2_LBL),
    BGE("BGE", false, RV32ParamType.RS1_RS2_LBL),
    BLTU("BLTU", false, RV32ParamType.RS1_RS2_LBL),
    BGEU("BGEU", false, RV32ParamType.RS1_RS2_LBL),
    LB("LB", false, RV32ParamType.RD_OFF12),
    LH("LH", false, RV32ParamType.RD_OFF12),
    LW("LW", false, RV32ParamType.RD_OFF12),
    LBU("LBU", false, RV32ParamType.RD_OFF12),
    LHU("LHU", false, RV32ParamType.RD_OFF12),
    SB("SB", false, RV32ParamType.RS2_OFF12),
    SH("SH", false, RV32ParamType.RS2_OFF12),
    SW("SW", false, RV32ParamType.RS2_OFF12),
    ADDI("ADDI", false, RV32ParamType.RD_RS1_I12),
    SLTI("SLTI", false, RV32ParamType.RD_RS1_I12),
    SLTIU("SLTIU", false, RV32ParamType.RD_RS1_I12),
    XORI("XORI", false, RV32ParamType.RD_RS1_I12),
    ORI("ORI", false, RV32ParamType.RD_RS1_I12),
    ANDI("ANDI", false, RV32ParamType.RD_RS1_I12),
    SLLI("SLLI", false, RV32ParamType.RD_RS1_SHAMT5),
    SRLI("SRLI", false, RV32ParamType.RD_RS1_SHAMT5),
    SRAI("SRAI", false, RV32ParamType.RD_RS1_SHAMT5),
    ADD("ADD", false, RV32ParamType.RD_RS1_RS2),
    SUB("SUB", false, RV32ParamType.RD_RS1_RS2),
    SLL("SLL", false, RV32ParamType.RD_RS1_RS2),
    SLT("SLT", false, RV32ParamType.RD_RS1_RS2),
    SLTU("SLTU", false, RV32ParamType.RD_RS1_RS2),
    XOR("XOR", false, RV32ParamType.RD_RS1_RS2),
    SRL("SRL", false, RV32ParamType.RD_RS1_RS2),
    SRA("SRA", false, RV32ParamType.RD_RS1_RS2),
    OR("OR", false, RV32ParamType.RD_RS1_RS2),
    AND("AND", false, RV32ParamType.RD_RS1_RS2),

    // CSR Extension
    CSRRW("CSRRW", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRS("CSRRS", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRC("CSRRC", false, RV32ParamType.CSR_RD_OFF12_RS1),
    CSRRWI("CSRRWI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),
    CSRRSI("CSRRSI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),
    CSRRCI("CSRRCI", false, RV32ParamType.CSR_RD_OFF12_UIMM5),

    CSRW("CSRW", true, RV32ParamType.PS_CSR_RS1),
    CSRR("CSRR", true, RV32ParamType.PS_RD_CSR),

    // M Extension
    MUL("MUL", false, RV32ParamType.RD_RS1_RS2),
    MULH("MULH", false, RV32ParamType.RD_RS1_RS2),
    MULHSU("MULHSU", false, RV32ParamType.RD_RS1_RS2),
    MULHU("MULHU", false, RV32ParamType.RD_RS1_RS2),
    DIV("DIV", false, RV32ParamType.RD_RS1_RS2),
    DIVU("DIVU", false, RV32ParamType.RD_RS1_RS2),
    REM("REM", false, RV32ParamType.RD_RS1_RS2),
    REMU("REMU", false, RV32ParamType.RD_RS1_RS2),

    Nop("NOP", true, RV32ParamType.PS_NONE),
    Mv("MV", true, RV32ParamType.PS_RD_RS1),
    Li("LI", true, RV32ParamType.PS_RD_I32, bytesNeeded = 8),
    La("LA", true, RV32ParamType.PS_RD_ALBL, bytesNeeded = 8),
    Not("NOT", true, RV32ParamType.PS_RD_RS1),
    Neg("NEG", true, RV32ParamType.PS_RD_RS1),
    Seqz("SEQZ", true, RV32ParamType.PS_RD_RS1),
    Snez("SNEZ", true, RV32ParamType.PS_RD_RS1),
    Sltz("SLTZ", true, RV32ParamType.PS_RD_RS1),
    Sgtz("SGTZ", true, RV32ParamType.PS_RD_RS1),
    Beqz("BEQZ", true, RV32ParamType.PS_RS1_JLBL),
    Bnez("BNEZ", true, RV32ParamType.PS_RS1_JLBL),
    Blez("BLEZ", true, RV32ParamType.PS_RS1_JLBL),
    Bgez("BGEZ", true, RV32ParamType.PS_RS1_JLBL),
    Bltz("BLTZ", true, RV32ParamType.PS_RS1_JLBL),
    BGTZ("BGTZ", true, RV32ParamType.PS_RS1_JLBL),
    Bgt("BGT", true, RV32ParamType.RS1_RS2_LBL),
    Ble("BLE", true, RV32ParamType.RS1_RS2_LBL),
    Bgtu("BGTU", true, RV32ParamType.RS1_RS2_LBL),
    Bleu("BLEU", true, RV32ParamType.RS1_RS2_LBL),
    J("J", true, RV32ParamType.PS_JLBL),
    JAL1("JAL", true, RV32ParamType.PS_JLBL),
    Jr("JR", true, RV32ParamType.PS_RS1),
    JALR1("JALR", true, RV32ParamType.PS_RS1),
    Ret("RET", true, RV32ParamType.PS_NONE),
    Call("CALL", true, RV32ParamType.PS_JLBL, bytesNeeded = 8),
    Tail("TAIL", true, RV32ParamType.PS_JLBL, bytesNeeded = 8);

    override val inCodeInfo: String? = if(isPseudo) "${bytesNeeded ?: "?"} bytes" else null

    override val paramRule: Rule?
        get() = paramType.rule

    override val typeName: String = name.lowercase()
}