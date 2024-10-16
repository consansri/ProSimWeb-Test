package cengine.lang.asm.ast.target.riscv.rv64


import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.asm.ast.target.riscv.RVBaseRegs
import cengine.lang.asm.ast.target.riscv.RVConst
import cengine.lang.asm.ast.target.riscv.RVConst.bit
import cengine.lang.asm.ast.target.riscv.RVConst.mask12CBraLayout5
import cengine.lang.asm.ast.target.riscv.RVConst.mask12CBraLayout7
import cengine.lang.asm.ast.target.riscv.RVConst.mask12Hi7
import cengine.lang.asm.ast.target.riscv.RVConst.mask12Lo5
import cengine.lang.asm.ast.target.riscv.RVConst.mask20JalLayout
import cengine.lang.asm.ast.target.riscv.RVConst.mask32Hi20
import cengine.lang.asm.ast.target.riscv.RVConst.mask32Lo12
import cengine.lang.asm.ast.target.riscv.RVCsr
import cengine.lang.obj.elf.ELF32_Shdr
import cengine.lang.obj.elf.ELF64_Shdr
import cengine.lang.obj.elf.ELFBuilder
import cengine.util.integer.Bin
import cengine.util.integer.Size
import cengine.util.integer.toUInt
import cengine.util.integer.toValue
import debug.DebugTools
import emulator.kit.nativeLog

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
    Bgtz("BGTZ", true, RV64ParamType.PS_RS1_Jlbl, true),
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

    override val inCodeInfo: String? = if (isPseudo) "${bytesNeeded ?: "?"} bytes" else null

    override val paramRule: Rule? = paramType.rule

    override val typeName: String = name

    override fun resolve(builder: ELFBuilder, instr: ASNode.Instruction) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (paramType) {
            RV64ParamType.RD_I20 -> {
                val expr = exprs[0]
                val opcode = when (this) {
                    LUI -> RVConst.OPC_LUI
                    AUIPC -> RVConst.OPC_AUIPC
                    JAL -> RVConst.OPC_JAL
                    else -> 0b0U
                }
                val rd = regs[0].ordinal.toUInt()
                val imm = expr.evaluate(builder)
                val imm20 = imm.toBin().toUInt() ?: 0U
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit20)) {
                    expr.addError("${expr.evaluated} exceeds ${Size.Bit20}.")
                }

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_Off12 -> {
                // Load
                val rs1 = regs[1].ordinal.toUInt()

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("${expr.evaluated} exceeds ${Size.Bit12}.")
                }

                val imm12 = imm.toBin().getResized(Size.Bit12).toUInt() ?: 0U

                val funct3 = when (this) {
                    LB -> RVConst.FUNCT3_LOAD_B
                    LH -> RVConst.FUNCT3_LOAD_H
                    LW -> RVConst.FUNCT3_LOAD_W
                    LD -> RVConst.FUNCT3_LOAD_D
                    LBU -> RVConst.FUNCT3_LOAD_BU
                    LHU -> RVConst.FUNCT3_LOAD_HU
                    LWU -> RVConst.FUNCT3_LOAD_WU
                    else -> 0U
                }

                val opcode = RVConst.OPC_LOAD
                val rd = regs[0].ordinal.toUInt()
                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RS2_Off12 -> {
                // Store
                val rs1 = regs[1].ordinal.toUInt()

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("${expr.evaluated} exceeds ${Size.Bit12}.")
                }

                val imm12 = imm.toBin().getResized(Size.Bit12).toUInt() ?: 0U

                val funct3 = when (this) {
                    SB -> RVConst.FUNCT3_STORE_B
                    SH -> RVConst.FUNCT3_STORE_H
                    SW -> RVConst.FUNCT3_STORE_W
                    SD -> RVConst.FUNCT3_STORE_D
                    else -> 0U
                }

                val opcode = RVConst.OPC_STORE
                val rs2 = regs[0].ordinal.toUInt()
                val bundle = (imm12.mask12Hi7() shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm12.mask12Lo5() shl 7) or opcode

                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_RS2 -> {
                val opcode = when (this) {
                    ADDW, SUBW, SLLW, SRLW, SRAW, MULW, DIVW, DIVUW, REMW, REMUW -> RVConst.OPC_ARITH_WORD
                    else -> RVConst.OPC_ARITH
                }


                val funct7 = when (this) {
                    SRA, SRAW -> RVConst.FUNCT7_SHIFT_ARITH
                    SUB, SUBW -> RVConst.FUNCT7_OPERATION_SUB
                    MUL, MULH, MULHSU, MULHU, DIV, DIVU, REM, REMU -> RVConst.FUNCT7_M
                    else -> 0U
                }

                val rd = regs[0].ordinal.toUInt()
                val rs1 = regs[1].ordinal.toUInt()
                val rs2 = regs[2].ordinal.toUInt()

                val funct3 = when (this) {
                    MUL, MULW -> RVConst.FUNCT3_M_MUL
                    MULH -> RVConst.FUNCT3_M_MULH
                    MULHSU -> RVConst.FUNCT3_M_MULHSU
                    MULHU -> RVConst.FUNCT3_M_MULHU
                    DIV, DIVW -> RVConst.FUNCT3_M_DIV
                    DIVU, DIVUW -> RVConst.FUNCT3_M_DIVU
                    REM, REMW -> RVConst.FUNCT3_M_REM
                    REMU, REMUW -> RVConst.FUNCT3_M_REMU
                    ADD, ADDW, SUB, SUBW -> RVConst.FUNCT3_OPERATION
                    SLL, SLLW -> RVConst.FUNCT3_SHIFT_LEFT
                    SRL, SRLW, SRA, SRAW -> RVConst.FUNCT3_SHIFT_RIGHT
                    SLT -> RVConst.FUNCT3_SLT
                    SLTU -> RVConst.FUNCT3_SLTU
                    XOR -> RVConst.FUNCT3_XOR
                    OR -> RVConst.FUNCT3_OR
                    AND -> RVConst.FUNCT3_AND
                    else -> 0U
                }

                val bundle = (funct7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_I12 -> {
                val opcode = when (this) {
                    JALR -> RVConst.OPC_JALR
                    ADDIW -> RVConst.OPC_ARITH_IMM_WORD
                    else -> RVConst.OPC_ARITH_IMM
                }

                val funct3 = when (this) {
                    ADDI, ADDIW -> RVConst.FUNCT3_OPERATION
                    SLTI -> RVConst.FUNCT3_SLT
                    SLTIU -> RVConst.FUNCT3_SLTU
                    XORI -> RVConst.FUNCT3_XOR
                    ORI -> RVConst.FUNCT3_OR
                    ANDI -> RVConst.FUNCT3_AND
                    else -> 0U
                }

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("$imm exceeds ${Size.Bit12}.")
                }
                val imm12 = imm.toBin().getResized(Size.Bit12).toUInt() ?: 0U

                val rd = regs[0].ordinal.toUInt()
                val rs1 = regs[1].ordinal.toUInt()

                val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.RD_RS1_SHAMT6 -> {
                val opcode = when (this) {
                    SRAIW, SLLIW, SRLIW -> RVConst.OPC_ARITH_IMM_WORD
                    else -> RVConst.OPC_ARITH_IMM
                }

                val funct7 = when (this) {
                    SRAI, SRAIW -> RVConst.FUNCT7_SHIFT_ARITH
                    else -> 0U
                }
                val funct3 = when (this) {
                    SLLI, SLLIW -> RVConst.FUNCT3_SHIFT_LEFT
                    else -> RVConst.FUNCT3_SHIFT_RIGHT
                }
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit6)) {
                    expr.addError("$imm exceeds ${Size.Bit6}.")
                }
                val shamt = imm.toBin().getResized(Size.Bit6).toUInt() ?: 0U

                val rd = regs[0].ordinal.toUInt()
                val rs1 = regs[1].ordinal.toUInt()
                val bundle = (funct7 shl 25) or (shamt shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.CSR_RD_OFF12_RS1 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }

                val opcode = RVConst.OPC_CSR
                val funct3 = when (this) {
                    CSRRW -> RVConst.FUNCT3_CSR_RW
                    CSRRS -> RVConst.FUNCT3_CSR_RS
                    CSRRC -> RVConst.FUNCT3_CSR_RC
                    else -> 0U
                }

                val csr = csrs[0].address

                val rd = regs[0].ordinal.toUInt()
                val rs1 = regs[1].ordinal.toUInt()

                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.CSR_RD_OFF12_UIMM5 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }

                val opcode = RVConst.OPC_CSR
                val funct3 = when (this) {
                    CSRRWI -> RVConst.FUNCT3_CSR_RWI
                    CSRRSI -> RVConst.FUNCT3_CSR_RSI
                    CSRRCI -> RVConst.FUNCT3_CSR_RCI
                    else -> 0U
                }

                val csr = csrs[0].address

                val rd = regs[0].ordinal.toUInt()
                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit5)) {
                    expr.addError("$imm exceeds ${Size.Bit5}.")
                }

                val zimm = imm.toBin().getResized(Size.Bit5).toUInt() ?: 0U

                val bundle = (csr shl 20) or (zimm shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_RD_LI_I64 -> {

                val expr = exprs[0]
                val imm = expr.evaluate(builder)
                val rd = regs[0].ordinal.toUInt()
                val x0 = RVBaseRegs.ZERO.ordinal.toUInt()

                if (!imm.checkSizeSignedOrUnsigned(Size.Bit64)) {
                    expr.addError("$imm exceeds ${Size.Bit64}.")
                }

                when {
                    imm.checkSizeSigned(Size.Bit12) -> {
                        val imm12 = imm.toBin().getResized(Size.Bit12).toUInt() ?: 0U
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 12 Bit Signed")
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_OPERATION

                        val addiBundle = (imm12 shl 20) or (x0 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(addiBundle)
                    }

                    imm.checkSizeSigned(Size.Bit32) -> {
                        val imm32 = imm.getResized(Size.Bit32).toBin()
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 32 Bit Signed")
                        // resized = upper + lower
                        // upper = resized - lower
                        val lower = imm32.getResized(Size.Bit12).getResized(Size.Bit32)
                        val upper = (imm32 - lower).toBin()

                        val imm20 = Bin(upper.toRawString().substring(0, 20), Size.Bit20).toUInt() ?: 0U
                        val imm12 = Bin(lower.toRawString().substring(20), Size.Bit12).toUInt() ?: 0U

                        // Build LUI Bundle
                        val luiOPC = RVConst.OPC_LUI
                        val luiBundle = (imm20 shl 12) or (rd shl 7) or luiOPC
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDI Bundle
                        val addiBundle = (imm12 shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle)
                    }

                    imm.checkSizeSigned(Size.Bit44) -> {
                        val resized = imm.toBin().getResized(Size.Bit44)
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 44 Bit Signed for ${resized.toHex()}")
                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         */
                        val l1 = resized.getResized(Size.Bit12).getResized(Size.Bit44)
                        val l2 = resized.shr(12).getResized(Size.Bit12).getResized(Size.Bit44) + (l1.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l3 = resized.shr(12 + 12).getResized(Size.Bit20).getResized(Size.Bit44) + (l2.toBin().getBit(0) ?: Bin("0", Size.Bit1))

                        val l1Int = l1.toUInt() ?: 0U
                        val l2Int = l2.toBin().toUInt() ?: 0U
                        val l3Int = l3.toBin().toUInt() ?: 0U


                        // Build LUI Bundle
                        val luiBundle = (l3Int shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDIW Bundle
                        val addiwBundle = (l2Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                        builder.currentSection.content.put(addiwBundle)

                        // Build SLLI Bundle
                        val slliBundle = (0U shl 25) or (0xCU shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundle)

                        // Build ADDI Bundle
                        val addiBundle = (l1Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle)
                    }

                    imm.checkSizeSigned(Size.Bit56) -> {
                        val resized = imm.toBin().getResized(Size.Bit56)
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 56 Bit Signed for ${resized.toHex()}")
                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         *  SLLI 12
                         *  ADDI
                         */
                        val l1 = resized.getResized(Size.Bit12).getResized(Size.Bit56)
                        val l2 = resized.shr(12).getResized(Size.Bit12).getResized(Size.Bit56) + (l1.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l3 = resized.shr(12 + 12).getResized(Size.Bit12).getResized(Size.Bit56) + (l2.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l4 = resized.shr(12 + 12 + 12).getResized(Size.Bit20).getResized(Size.Bit56) + (l3.toBin().getBit(0) ?: Bin("0", Size.Bit1))

                        val l1Int = l1.toUInt() ?: 0U
                        val l2Int = l2.toBin().toUInt() ?: 0U
                        val l3Int = l3.toBin().toUInt() ?: 0U
                        val l4Int = l4.toBin().toUInt() ?: 0U

                        // Build LUI Bundle
                        val luiBundle = (l4Int shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDIW Bundle
                        val addiwBundle = (l3Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                        builder.currentSection.content.put(addiwBundle)

                        // Build SLLI Bundle
                        val slliBundle = (0U shl 25) or (0xCU shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundle)

                        // Build ADDI Bundle
                        val addiBundle1 = (l2Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle1)

                        // Build SLLI Bundle
                        builder.currentSection.content.put(slliBundle)

                        // Build ADDI Bundle
                        val addiBundle2 = (l1Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle2)
                    }

                    imm.checkSizeSigned(Size.Bit64) -> {
                        val resized = imm.toBin().getUResized(Size.Bit64)
                        if (DebugTools.RV64_showLIDecisions) nativeLog("Decided 64 Bit Signed")
                        /**
                         *  val64 = lui + addiw + addi3 + addi2 + addi1
                         *
                         *  LUI
                         *  ADDIW
                         *  SLLI 12
                         *  ADDI
                         *  SLLI 13
                         *  ADDI
                         *  SLLI 12
                         *  ADDI
                         */

                        val l1 = resized.getResized(Size.Bit12).getResized(Size.Bit64)
                        val l2 = resized.shr(12).getResized(Size.Bit12).getResized(Size.Bit64) + (l1.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l3 = resized.shr(12 + 13).getResized(Size.Bit12).getResized(Size.Bit64) + (l2.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l4 = resized.shr(12 + 13 + 12).getResized(Size.Bit12).getResized(Size.Bit64) + (l3.toBin().getBit(0) ?: Bin("0", Size.Bit1))
                        val l5 = resized.shr(12 + 13 + 12 + 12) + (l4.toBin().getBit(0) ?: Bin("0", Size.Bit1))

                        val l1Int = l1.toUInt() ?: 0U
                        val l2Int = l2.toBin().toUInt() ?: 0U
                        val l3Int = l3.toBin().toUInt() ?: 0U
                        val l4Int = l4.toBin().toUInt() ?: 0U
                        val l5Int = l5.toBin().toUInt() ?: 0U

                        // Build LUI Bundle
                        val luiBundle = (l5Int shl 12) or (rd shl 7) or RVConst.OPC_LUI
                        builder.currentSection.content.put(luiBundle)

                        // Build ADDIW Bundle
                        val addiwBundle = (l4Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM_WORD
                        builder.currentSection.content.put(addiwBundle)

                        // Build SLLI Bundle
                        val slliBundleC = (0U shl 25) or (0xCU shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundleC)

                        // Build ADDI Bundle
                        val addiBundle1 = (l3Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle1)

                        // Build SLLI Bundle
                        val slliBundleD = (0U shl 25) or (0xDU shl 20) or (rd shl 15) or (RVConst.FUNCT3_SHIFT_LEFT shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(slliBundleD)

                        // Build ADDI Bundle
                        val addiBundle2 = (l2Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle2)

                        // Build SLLI Bundle
                        builder.currentSection.content.put(slliBundleC)

                        // Build ADDI Bundle
                        val addiBundle3 = (l1Int shl 20) or (rd shl 15) or (RVConst.FUNCT3_OPERATION shl 12) or (rd shl 7) or RVConst.OPC_ARITH_IMM
                        builder.currentSection.content.put(addiBundle3)
                    }
                }
            }

            RV64ParamType.PS_RD_RS1 -> {
                when (this) {
                    Mv -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val rd = regs[0].ordinal.toUInt()
                        val rs1 = regs[1].ordinal.toUInt()
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Not -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_XOR
                        val imm12 = (-1).toUInt().mask32Lo12()
                        val rd = regs[0].ordinal.toUInt()
                        val rs1 = regs[1].ordinal.toUInt()
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Neg -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_OPERATION
                        val funct7 = RVConst.FUNCT7_OPERATION_SUB
                        val rd = regs[0].ordinal.toUInt()
                        val rs2 = regs[1].ordinal.toUInt()

                        val bundle = (funct7 shl 25) or (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Seqz -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val funct3 = RVConst.FUNCT3_SLTU
                        val imm12 = 1U
                        val rd = regs[0].ordinal.toUInt()
                        val rs1 = regs[1].ordinal.toUInt()
                        val bundle = (imm12 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Snez -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLTU
                        val rd = regs[0].ordinal.toUInt()
                        val rs2 = regs[1].ordinal.toUInt()
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sltz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0].ordinal.toUInt()
                        val rs1 = regs[1].ordinal.toUInt()
                        val bundle = (rs1 shl 15) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Sgtz -> {
                        val opcode = RVConst.OPC_ARITH
                        val funct3 = RVConst.FUNCT3_SLT
                        val rd = regs[0].ordinal.toUInt()
                        val rs2 = regs[1].ordinal.toUInt()
                        val bundle = (rs2 shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    else -> {
                        // Should never happen
                    }
                }
            }

            RV64ParamType.PS_RS1 -> {
                val opcode = RVConst.OPC_JALR
                val rs1 = regs[0].ordinal.toUInt()
                val rd = when (this) {
                    JALR1 -> RVBaseRegs.RA.ordinal.toUInt()
                    else -> RVBaseRegs.ZERO.ordinal.toUInt()
                }
                val bundle = (rs1 shl 15) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_CSR_RS1 -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }
                val opcode = RVConst.OPC_CSR
                val funct3 = RVConst.FUNCT3_CSR_RW
                val csr = csrs[0].address
                val rs1 = regs[0].ordinal.toUInt()
                val bundle = (csr shl 20) or (rs1 shl 15) or (funct3 shl 12) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_RD_CSR -> {
                val csrs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVCsr.regs.firstOrNull { it.recognizable.contains(token.value) } }
                val opcode = RVConst.OPC_CSR
                val funct3 = RVConst.FUNCT3_CSR_RS
                val csr = csrs[0].address
                val rd = regs[0].ordinal.toUInt()
                val bundle = (csr shl 20) or (funct3 shl 12) or (rd shl 7) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.NONE -> {
                val opcode = RVConst.OPC_E
                val imm12 = when (this) {
                    EBREAK -> 1U
                    else -> 0U
                }
                val bundle = (imm12 shl 20) or opcode
                builder.currentSection.content.put(bundle)
            }

            RV64ParamType.PS_NONE -> {
                when (this) {
                    Nop -> {
                        val opcode = RVConst.OPC_ARITH_IMM
                        val bundle = opcode
                        builder.currentSection.content.put(bundle)
                    }

                    Ret -> {
                        val opcode = RVConst.OPC_JALR
                        val rs1 = RVBaseRegs.RA.ordinal.toUInt()
                        val bundle = (rs1 shl 15) or opcode
                        builder.currentSection.content.put(bundle)
                    }

                    else -> {
                        // should not happen
                    }
                }
            }

            RV64ParamType.RS1_RS2_LBL -> {} // Will be evaluated later
            RV64ParamType.PS_RS1_Jlbl -> {} // Will be evaluated later
            RV64ParamType.PS_RD_Albl -> {} // Will be evaluated later
            RV64ParamType.PS_lbl -> {} // Will be evaluated later
        }

        if (labelDependent) {
            return builder.currentSection.queueLateInit(instr, bytesNeeded ?: 4)
        }
    }

    override fun lateEvaluation(builder: ELFBuilder, section: ELFBuilder.Section, instr: ASNode.Instruction, index: Int) {
        val regs = instr.tokens.filter { it.type == AsmTokenType.REGISTER }.mapNotNull { token -> RVBaseRegs.entries.firstOrNull { it.recognizable.contains(token.value) } }
        val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()

        when (this) {
            JAL -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }

                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr
                val imm = result.toValue().toBin().shr(1).toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit20)) {
                    expr.addError("$imm exceeds ${Size.Bit20}")
                }

                val imm20 = imm.getResized(Size.Bit20).toBin().toUInt()?.mask20JalLayout() ?: 0U

                val rd = regs[0].ordinal.toUInt()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr
                val imm = result.toValue().toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("$imm exceeds ${Size.Bit12}")
                }

                val imm12 = imm.getResized(Size.Bit12).toBin().toUInt() ?: 0U
                val imm7 = imm12.mask12CBraLayout7()
                val imm5 = imm12.mask12CBraLayout5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    BEQ -> RVConst.FUNCT3_CBRA_BEQ
                    BNE -> RVConst.FUNCT3_CBRA_BNE
                    BLT -> RVConst.FUNCT3_CBRA_BLT
                    BGE -> RVConst.FUNCT3_CBRA_BGE
                    BLTU -> RVConst.FUNCT3_CBRA_BLTU
                    BGEU -> RVConst.FUNCT3_CBRA_BGEU
                    else -> throw Exception("Implementation Error!")
                }
                val rs1 = regs[0].ordinal.toUInt()
                val rs2 = regs[1].ordinal.toUInt()

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            La -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }

                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val result = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U

                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(12) == 1U) {
                    hi20 += 1U
                }

                val rd = regs[0].ordinal.toUInt()

                val auipcOpCode = RVConst.OPC_AUIPC
                val auipcBundle = (hi20 shl 12) or (rd shl 7) or auipcOpCode
                section.content[index] = auipcBundle

                val addiOpCode = RVConst.OPC_ARITH_IMM
                val funct3 = RVConst.FUNCT3_OPERATION
                val addiBundle = (lo12 shl 20) or (rd shl 15) or (funct3 shl 12) or (rd shl 7) or addiOpCode
                section.content[index + 4] = addiBundle
            }

            Beqz, Bnez, Blez, Bgez, Bltz, Bgtz -> {
                // Comparisons with Zero
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }

                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr

                val imm = result.toValue().toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("$imm exceeds ${Size.Bit12}")
                }

                val imm12 = imm.getResized(Size.Bit12).toBin().toUInt() ?: 0U
                val imm7 = imm12.mask12CBraLayout7()
                val imm5 = imm12.mask12CBraLayout5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    Beqz -> RVConst.FUNCT3_CBRA_BEQ
                    Bnez -> RVConst.FUNCT3_CBRA_BNE
                    Blez -> RVConst.FUNCT3_CBRA_BGE
                    Bgez -> RVConst.FUNCT3_CBRA_BGE
                    Bltz -> RVConst.FUNCT3_CBRA_BLT
                    Bgtz -> RVConst.FUNCT3_CBRA_BLT
                    else -> throw Exception("Implementation Error!")
                }

                val rs2 = when (this) {
                    Beqz -> RVBaseRegs.ZERO.ordinal.toUInt()
                    Bnez -> RVBaseRegs.ZERO.ordinal.toUInt()
                    Blez -> regs[0].ordinal.toUInt()
                    Bgez -> RVBaseRegs.ZERO.ordinal.toUInt()
                    Bltz -> RVBaseRegs.ZERO.ordinal.toUInt()
                    Bgtz -> regs[0].ordinal.toUInt()
                    else -> throw Exception("Implementation Error!")
                }

                val rs1 = when (this) {
                    Beqz -> regs[0].ordinal.toUInt()
                    Bnez -> regs[0].ordinal.toUInt()
                    Blez -> RVBaseRegs.ZERO.ordinal.toUInt()
                    Bgez -> regs[0].ordinal.toUInt()
                    Bltz -> regs[0].ordinal.toUInt()
                    Bgtz -> RVBaseRegs.ZERO.ordinal.toUInt()
                    else -> throw Exception("Implementation Error!")
                }

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            Bgt, Ble, Bgtu, Bleu -> {
                // Swapping rs1 and rs2
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_BRANCH, section, index.toUInt())
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr


                val imm = result.toValue().toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit12)) {
                    expr.addError("$imm exceeds ${Size.Bit12}")
                }

                val imm12 = imm.getResized(Size.Bit12).toBin().toUInt() ?: 0U
                val imm7 = imm12.mask12CBraLayout7()
                val imm5 = imm12.mask12CBraLayout5()

                val opcode = RVConst.OPC_CBRA
                val funct3 = when (this) {
                    Bgt -> RVConst.FUNCT3_CBRA_BLT
                    Ble -> RVConst.FUNCT3_CBRA_BGE
                    Bgtu -> RVConst.FUNCT3_CBRA_BLTU
                    Bleu -> RVConst.FUNCT3_CBRA_BGEU
                    else -> throw Exception("Implementation Error!")
                }

                val rs2 = regs[0].ordinal.toUInt()
                val rs1 = regs[1].ordinal.toUInt()

                val bundle = (imm7 shl 25) or (rs2 shl 20) or (rs1 shl 15) or (funct3 shl 12) or (imm5 shl 7) or opcode
                section.content[index] = bundle
            }

            J -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr

                val imm = result.toValue().toBin().shr(1).toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit20)) {
                    expr.addError("$imm exceeds ${Size.Bit20}")
                }

                val imm20 = imm.getResized(Size.Bit20).toBin().toUInt()?.mask20JalLayout() ?: 0U

                val rd = RVBaseRegs.ZERO.ordinal.toUInt()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            JAL1 -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_JAL, section, index.toUInt())
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = target - thisAddr

                val imm = result.toValue().toBin().shr(1).toDec()
                if (!imm.checkSizeSignedOrUnsigned(Size.Bit20)) {
                    expr.addError("$imm exceeds ${Size.Bit20}")
                }

                val imm20 = imm.getResized(Size.Bit20).toBin().toUInt()?.mask20JalLayout() ?: 0U

                val rd = RVBaseRegs.RA.ordinal.toUInt()
                val opcode = RVConst.OPC_JAL

                val bundle = (imm20 shl 12) or (rd shl 7) or opcode
                section.content[index] = bundle
            }

            Call -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = (target - thisAddr).toUInt()

                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(12) == 1U) {
                    hi20 += 1U
                }

                val x6 = RVBaseRegs.T1.ordinal.toUInt()
                val x1 = RVBaseRegs.RA.ordinal.toUInt()

                val auipcOpcode = RVConst.OPC_AUIPC
                val jalrOpcode = RVConst.OPC_JALR

                val auipcBundle = (hi20 shl 12) or (x6 shl 7) or auipcOpcode
                val jalrBundle = (lo12 shl 20) or (x6 shl 15) or (x1 shl 7) or jalrOpcode

                section.content[index] = auipcBundle
                section.content[index + 4] = jalrBundle
            }

            Tail -> {
                val expr = exprs[0]
                val targetAddr = expr.evaluate(builder) { identifier ->
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_HI20, section, index.toUInt())
                    builder.addRelEntry(identifier, RVConst.R_RISCV_PCREL_LO12_I, section, index.toUInt() + 4U)
                }
                if (!targetAddr.checkSizeSignedOrUnsigned(Size.Bit32)) {
                    expr.addError("$targetAddr exceeds ${Size.Bit32}")
                }

                val shdr = section.shdr
                val thisAddr = when (shdr) {
                    is ELF32_Shdr -> shdr.sh_addr.toULong() + index.toUInt()
                    is ELF64_Shdr -> shdr.sh_addr + index.toUInt()
                }
                val target = targetAddr.getResized(Size.Bit32).toBin().toUInt() ?: 0U
                val result = (target - thisAddr).toUInt()

                val lo12 = result.mask32Lo12()
                var hi20 = result.mask32Hi20()

                if (lo12.bit(12) == 1U) {
                    hi20 += 1U
                }

                val x6 = RVBaseRegs.T1.ordinal.toUInt()
                val x0 = RVBaseRegs.ZERO.ordinal.toUInt()

                val auipcOpcode = RVConst.OPC_AUIPC
                val jalrOpcode = RVConst.OPC_JALR

                val auipcBundle = (hi20 shl 12) or (x6 shl 7) or auipcOpcode
                val jalrBundle = (lo12 shl 20) or (x6 shl 15) or (x0 shl 7) or jalrOpcode

                section.content[index] = auipcBundle
                section.content[index + 4] = jalrBundle
            }

            else -> {
                // Nothing needs to be done
            }
        }
    }

}