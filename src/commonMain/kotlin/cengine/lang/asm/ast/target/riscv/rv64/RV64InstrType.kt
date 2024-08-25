package cengine.lang.asm.ast.target.riscv.rv64


import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.RelocatableELFBuilder

enum class RV64InstrType(override val detectionName: String, val isPseudo: Boolean, val paramType: RV64ParamType, val labelDependent: Boolean = false, override val bytesNeeded: Int? = 4) : InstrTypeInterface {
    LUI("LUI", false, RV64ParamType.RD_I20),
    AUIPC("AUIPC", false, RV64ParamType.RD_I20),
    JAL("JAL", false, RV64ParamType.RD_I20, true),
    JALR("JALR", false, RV64ParamType.RD_RS1_I12),
    ECALL("ECALL", false, RV64ParamType.NONE),
    EBREAK("EBREAK", false, RV64ParamType.NONE),
    BEQ("BEQ", false, RV64ParamType.RS1_RS2_LBL, true),
    BNE("BNE", false, RV64ParamType.RS1_RS2_LBL, true),
    BLT("BLT", false, RV64ParamType.RS1_RS2_LBL, true),
    BGE("BGE", false, RV64ParamType.RS1_RS2_LBL, true),
    BLTU("BLTU", false, RV64ParamType.RS1_RS2_LBL, true),
    BGEU("BGEU", false, RV64ParamType.RS1_RS2_LBL, true),
    LB("LB", false, RV64ParamType.RD_Off12),
    LH("LH", false, RV64ParamType.RD_Off12),
    LW("LW", false, RV64ParamType.RD_Off12),
    LD("LD", false, RV64ParamType.RD_Off12),
    LBU("LBU", false, RV64ParamType.RD_Off12),
    LHU("LHU", false, RV64ParamType.RD_Off12),
    LWU("LWU", false, RV64ParamType.RD_Off12),
    SB("SB", false, RV64ParamType.RS2_Off12),
    SH("SH", false, RV64ParamType.RS2_Off12),
    SW("SW", false, RV64ParamType.RS2_Off12),
    SD("SD", false, RV64ParamType.RS2_Off12),
    ADDI("ADDI", false, RV64ParamType.RD_RS1_I12),
    ADDIW("ADDIW", false, RV64ParamType.RD_RS1_I12),
    SLTI("SLTI", false, RV64ParamType.RD_RS1_I12),
    SLTIU("SLTIU", false, RV64ParamType.RD_RS1_I12),
    XORI("XORI", false, RV64ParamType.RD_RS1_I12),
    ORI("ORI", false, RV64ParamType.RD_RS1_I12),
    ANDI("ANDI", false, RV64ParamType.RD_RS1_I12),
    SLLI("SLLI", false, RV64ParamType.RD_RS1_SHAMT6),
    SLLIW("SLLIW", false, RV64ParamType.RD_RS1_SHAMT6),
    SRLI("SRLI", false, RV64ParamType.RD_RS1_SHAMT6),
    SRLIW("SRLIW", false, RV64ParamType.RD_RS1_SHAMT6),
    SRAI("SRAI", false, RV64ParamType.RD_RS1_SHAMT6),
    SRAIW("SRAIW", false, RV64ParamType.RD_RS1_SHAMT6),
    ADD("ADD", false, RV64ParamType.RD_RS1_RS2),
    ADDW("ADDW", false, RV64ParamType.RD_RS1_RS2),
    SUB("SUB", false, RV64ParamType.RD_RS1_RS2),
    SUBW("SUBW", false, RV64ParamType.RD_RS1_RS2),
    SLL("SLL", false, RV64ParamType.RD_RS1_RS2),
    SLLW("SLLW", false, RV64ParamType.RD_RS1_RS2),
    SLT("SLT", false, RV64ParamType.RD_RS1_RS2),
    SLTU("SLTU", false, RV64ParamType.RD_RS1_RS2),
    XOR("XOR", false, RV64ParamType.RD_RS1_RS2),
    SRL("SRL", false, RV64ParamType.RD_RS1_RS2),
    SRLW("SRLW", false, RV64ParamType.RD_RS1_RS2),
    SRA("SRA", false, RV64ParamType.RD_RS1_RS2),
    SRAW("SRAW", false, RV64ParamType.RD_RS1_RS2),
    OR("OR", false, RV64ParamType.RD_RS1_RS2),
    AND("AND", false, RV64ParamType.RD_RS1_RS2),

    // CSR Extension
    CSRRW("CSRRW", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRS("CSRRS", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRC("CSRRC", false, RV64ParamType.CSR_RD_OFF12_RS1),
    CSRRWI("CSRRWI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),
    CSRRSI("CSRRSI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),
    CSRRCI("CSRRCI", false, RV64ParamType.CSR_RD_OFF12_UIMM5),

    CSRW("CSRW", true, RV64ParamType.PS_CSR_RS1),
    CSRR("CSRR", true, RV64ParamType.PS_RD_CSR),

    // M Extension
    MUL("MUL", false, RV64ParamType.RD_RS1_RS2),
    MULH("MULH", false, RV64ParamType.RD_RS1_RS2),
    MULHSU("MULHSU", false, RV64ParamType.RD_RS1_RS2),
    MULHU("MULHU", false, RV64ParamType.RD_RS1_RS2),
    DIV("DIV", false, RV64ParamType.RD_RS1_RS2),
    DIVU("DIVU", false, RV64ParamType.RD_RS1_RS2),
    REM("REM", false, RV64ParamType.RD_RS1_RS2),
    REMU("REMU", false, RV64ParamType.RD_RS1_RS2),

    // RV64 M Extension
    MULW("MULW", false, RV64ParamType.RD_RS1_RS2),
    DIVW("DIVW", false, RV64ParamType.RD_RS1_RS2),
    DIVUW("DIVUW", false, RV64ParamType.RD_RS1_RS2),
    REMW("REMW", false, RV64ParamType.RD_RS1_RS2),
    REMUW("REMUW", false, RV64ParamType.RD_RS1_RS2),

    // Pseudo
    FENCEI("FENCE.I", true, RV64ParamType.PS_NONE),
    Nop("NOP", true, RV64ParamType.PS_NONE),
    Mv("MV", true, RV64ParamType.PS_RD_RS1),
    Li64("LI", true, RV64ParamType.PS_RD_LI_I64, bytesNeeded = null),
    La("LA", true, RV64ParamType.PS_RD_Albl, true, bytesNeeded = 8),
    Not("NOT", true, RV64ParamType.PS_RD_RS1),
    Neg("NEG", true, RV64ParamType.PS_RD_RS1),
    Seqz("SEQZ", true, RV64ParamType.PS_RD_RS1),
    Snez("SNEZ", true, RV64ParamType.PS_RD_RS1),
    Sltz("SLTZ", true, RV64ParamType.PS_RD_RS1),
    Sgtz("SGTZ", true, RV64ParamType.PS_RD_RS1),
    Beqz("BEQZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bnez("BNEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Blez("BLEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bgez("BGEZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bltz("BLTZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    BGTZ("BGTZ", true, RV64ParamType.PS_RS1_Jlbl, true),
    Bgt("BGT", true, RV64ParamType.RS1_RS2_LBL, true),
    Ble("BLE", true, RV64ParamType.RS1_RS2_LBL, true),
    Bgtu("BGTU", true, RV64ParamType.RS1_RS2_LBL, true),
    Bleu("BLEU", true, RV64ParamType.RS1_RS2_LBL, true),
    J("J", true, RV64ParamType.PS_lbl, true),
    JAL1("JAL", true, RV64ParamType.PS_lbl, true),
    Jr("JR", true, RV64ParamType.PS_RS1),
    JALR1("JALR", true, RV64ParamType.PS_RS1),
    Ret("RET", true, RV64ParamType.PS_NONE),
    Call("CALL", true, RV64ParamType.PS_lbl, true, bytesNeeded = 8),
    Tail("TAIL", true, RV64ParamType.PS_lbl, true, bytesNeeded = 8);

    override val inCodeInfo: String? = if(isPseudo) "${bytesNeeded ?: "?"} bytes" else null

    override val paramRule: Rule? = paramType.rule

    override val typeName: String = name

    override fun resolve(builder: RelocatableELFBuilder, instr: ASNode.Instruction) {
        TODO("Not yet implemented")
    }

    override fun lateEvaluation(builder: RelocatableELFBuilder, section: RelocatableELFBuilder.Section, instr: ASNode.Instruction, index: Int) {
        TODO("Not yet implemented")
    }

}