package cengine.lang.asm.ast.target.riscv

import cengine.util.integer.UInt32

data object RVConst {
    /**
     * OPCODE CONSTANTS
     */

    val OPC_LUI = UInt32(0b00110111U)
    val OPC_AUIPC = UInt32(0b0010111U)
    val OPC_JAL = UInt32(0b1101111U)
    val OPC_JALR = UInt32(0b1100111U)

    val OPC_OS = UInt32(0b1110011U)

    val OPC_CBRA = UInt32(0b1100011U)
    val OPC_LOAD = UInt32(0b0000011U)
    val OPC_STORE = UInt32(0b0100011U)
    val OPC_ARITH = UInt32(0b0110011U)
    val OPC_ARITH_WORD = UInt32(0b0111011U)
    val OPC_ARITH_IMM = UInt32(0b0010011U)
    val OPC_ARITH_IMM_WORD = UInt32(0b0011011U)

    val OPC_FENCE = UInt32(0b0001111U)

    /**
     * FUNCT3 CONSTANTS
     */

    val FUNCT3_CBRA_BEQ = UInt32(0b000U)
    val FUNCT3_CBRA_BNE = UInt32(0b001U)
    val FUNCT3_CBRA_BLT = UInt32(0b100U)
    val FUNCT3_CBRA_BGE = UInt32(0b101U)
    val FUNCT3_CBRA_BLTU = UInt32(0b110U)
    val FUNCT3_CBRA_BGEU = UInt32(0b111U)

    val FUNCT3_LOAD_B = UInt32(0b000U)
    val FUNCT3_LOAD_H = UInt32(0b001U)
    val FUNCT3_LOAD_W = UInt32(0b010U)
    val FUNCT3_LOAD_D = UInt32(0b011U)
    val FUNCT3_LOAD_BU = UInt32(0b100U)
    val FUNCT3_LOAD_HU = UInt32(0b101U)
    val FUNCT3_LOAD_WU = UInt32(0b110U)

    val FUNCT3_STORE_B = UInt32(0b000U)
    val FUNCT3_STORE_H = UInt32(0b001U)
    val FUNCT3_STORE_W = UInt32(0b010U)
    val FUNCT3_STORE_D = UInt32(0b011U)

    val FUNCT3_OPERATION = UInt32(0b000U)
    val FUNCT3_SHIFT_LEFT = UInt32(0b001U)
    val FUNCT3_SLT = UInt32(0b010U)
    val FUNCT3_SLTU = UInt32(0b011U)
    val FUNCT3_XOR = UInt32(0b100U)
    val FUNCT3_SHIFT_RIGHT = UInt32(0b101U)
    val FUNCT3_OR = UInt32(0b110U)
    val FUNCT3_AND = UInt32(0b111U)

    val FUNCT3_M_MUL = UInt32(0b000U)
    val FUNCT3_M_MULH = UInt32(0b001U)
    val FUNCT3_M_MULHSU = UInt32(0b010U)
    val FUNCT3_M_MULHU = UInt32(0b011U)
    val FUNCT3_M_DIV = UInt32(0b100U)
    val FUNCT3_M_DIVU = UInt32(0b101U)
    val FUNCT3_M_REM = UInt32(0b110U)
    val FUNCT3_M_REMU = UInt32(0b111U)

    val FUNCT3_E = UInt32(0b000U)
    val FUNCT3_CSR_RW = UInt32(0b001U)
    val FUNCT3_CSR_RS = UInt32(0b010U)
    val FUNCT3_CSR_RC = UInt32(0b011U)
    val FUNCT3_CSR_RWI = UInt32(0b101U)
    val FUNCT3_CSR_RSI = UInt32(0b110U)
    val FUNCT3_CSR_RCI = UInt32(0b111U)

    val FUNCT3_FENCE_I = UInt32(0b001U)

    /**
     * FUNCT7 CONSTANTS
     */

    val FUNCT7_SHIFT_ARITH_OR_SUB = UInt32(0b0100000U)
    val FUNCT7_M = UInt32(0b0000001U)

    /**
     * Relocation Types
     *
     * See: https://github.com/riscv-non-isa/riscv-elf-psabi-doc/blob/master/riscv-elf.adoc
     */

    val R_RISCV_NONE = UInt32(0U)
    val R_RISCV_32 = UInt32(1U)
    val R_RISCV_64 = UInt32(2U)
    val R_RISCV_BRANCH = UInt32(16U)
    val R_RISCV_JAL = UInt32(17U)
    val R_RISCV_CALL = UInt32(18U)
    val R_RISCV_PCREL_HI20 = UInt32(23U)
    val R_RISCV_PCREL_LO12_I = UInt32(24U)
    val R_RISCV_PCREL_LO12_S = UInt32(25U)
    val R_RISCV_HI20 = UInt32(26U)
    val R_RISCV_LO12_I = UInt32(27U)
    val R_RISCV_LO12_S = UInt32(28U)

    enum class RelocType {
        R_RISCV_NONE,
        R_RISCV_32,
        R_RISCV_64,
        R_RISCV_RELATIVE,
        R_RISCV_COPY,
        JUMP_SLOT,
        TLS_DTPMOD32,
        TLS_DTPMOD64,
        TLS_DTPREL32,
        TLS_DTPREL64,
        TLS_TPREL32,
        TLS_TPREL64,
        TLS_DESC,
        BRANCH,
        JAL,
        CALL,
        CALL_PLT,
        GOT_HI20,
        TLS_GOT_HI20,
        TLS_GD_HI20,
        PCREL_LO12_I,
        PCREL_LO12_S,
        HI20,
        LO12_I,
        LO12_S,
        TPREL_HI20,
        TPREL_LO12_I,
        TPREL_LO12_S,
        TPREL_ADD,
        ADD8,
        ADD16,
        ADD32,
        ADD64,
        SUB8,
        SUB16,
        SUB32,
        SUB64,
        GOT32_PCREL,
        _Reserved0,
        ALIGN,
        RVC_BRANCH,
        RVC_JUMP,
    }


    /**
     * MASKS
     */

    fun UInt32.mask12Hi7(): UInt32 = this shr 5

    fun UInt32.mask12Lo5(): UInt32 = this.lowest(5)

    fun UInt32.mask32Hi20(): UInt32 = this shr 12

    fun UInt32.mask32Lo12(): UInt32 = this.lowest(12)


    /**
     * Expects the relative target offset.
     *
     * @return the jType starting from index 0 (needs to be shifted for 12 bit to the left when used in opcode)
     */
    fun UInt32.mask20jType(): UInt32 {
        val bit20 = (this shr 19) and 1
        val bits10to1 = (this shr 1).lowest(10)
        val bit11 = (this shr 11) and 1
        val bits19to12 = (this shr 12).lowest(8)
    
        return (bit20 shl 19) or
                (bits19to12) or
                (bit11 shl 8) or
                (bits10to1 shl 9)
    }

    fun UInt32.mask12bType7(): UInt32 = (bit(12) shl 6) or (this shr 5).lowest(6)

    fun UInt32.mask12bType5(): UInt32 = (shr(1).lowest(4) shl 1) or bit(10)


}




