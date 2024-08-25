package cengine.lang.asm.ast.target.riscv

data object RVConst{
    /**
     * OPCODE CONSTANTS
     */

    const val OPC_LUI = 0b00110111U
    const val OPC_AUIPC = 0b0010111U
    const val OPC_JAL = 0b1101111U
    const val OPC_JALR = 0b1100111U

    const val OPC_E = 0b1110011U

    const val OPC_CBRA = 0b1100011U
    const val OPC_LOAD = 0b0000011U
    const val OPC_STORE = 0b0100011U
    const val OPC_ARITH = 0b0110011U
    const val OPC_ARITH_WORD = 0b0111011U
    const val OPC_ARITH_IMM = 0b0010011U
    const val OPC_ARITH_IMM_WORD = 0b0011011U
    const val OPC_CSR = 0b1110011U

    /**
     * FUNCT3 CONSTANTS
     */

    const val FUNCT3_CBRA_BEQ = 0b000U
    const val FUNCT3_CBRA_BNE = 0b001U
    const val FUNCT3_CBRA_BLT = 0b100U
    const val FUNCT3_CBRA_BGE = 0b101U
    const val FUNCT3_CBRA_BLTU = 0b110U
    const val FUNCT3_CBRA_BGEU = 0b111U

    const val FUNCT3_LOAD_B = 0b000U
    const val FUNCT3_LOAD_H = 0b001U
    const val FUNCT3_LOAD_W = 0b010U
    const val FUNCT3_LOAD_D = 0b011U
    const val FUNCT3_LOAD_BU = 0b100U
    const val FUNCT3_LOAD_HU = 0b101U
    const val FUNCT3_LOAD_WU = 0b110U

    const val FUNCT3_STORE_B = 0b000U
    const val FUNCT3_STORE_H = 0b001U
    const val FUNCT3_STORE_W = 0b010U
    const val FUNCT3_STORE_D = 0b011U

    const val FUNCT3_OPERATION = 0b000U
    const val FUNCT3_SHIFT_LEFT = 0b001U
    const val FUNCT3_SLT = 0b010U
    const val FUNCT3_SLTU = 0b011U
    const val FUNCT3_XOR = 0b100U
    const val FUNCT3_SHIFT_RIGHT = 0b101U
    const val FUNCT3_OR = 0b110U
    const val FUNCT3_AND = 0b111U

    const val FUNCT3_M_MUL = 0b000U
    const val FUNCT3_M_MULH = 0b001U
    const val FUNCT3_M_MULHSU = 0b010U
    const val FUNCT3_M_MULHU = 0b011U
    const val FUNCT3_M_DIV = 0b100U
    const val FUNCT3_M_DIVU = 0b101U
    const val FUNCT3_M_REM = 0b110U
    const val FUNCT3_M_REMU = 0b111U

    const val FUNCT3_CSR_RW = 0b001U
    const val FUNCT3_CSR_RS = 0b010U
    const val FUNCT3_CSR_RC = 0b011U
    const val FUNCT3_CSR_RWI = 0b101U
    const val FUNCT3_CSR_RSI = 0b110U
    const val FUNCT3_CSR_RCI = 0b111U

    /**
     * FUNCT7 CONSTANTS
     */

    const val FUNCT7_SHIFT_ARITH = 0b0100000U
    const val FUNCT7_OPERATION_SUB = 0b0100000U
    const val FUNCT7_M = 0b0000001U

    /**
     * Relocation Types
     *
     * See: https://github.com/riscv-non-isa/riscv-elf-psabi-doc/blob/master/riscv-elf.adoc
     */

    const val R_RISCV_NONE = 0U
    const val R_RISCV_32 = 1U
    const val R_RISCV_64 = 2U
    const val R_RISCV_BRANCH = 16U
    const val R_RISCV_JAL = 17U
    const val R_RISCV_CALL = 18U
    const val R_RISCV_PCREL_HI20 = 23U
    const val R_RISCV_PCREL_LO12_I = 24U
    const val R_RISCV_PCREL_LO12_S = 25U
    const val R_RISCV_HI20 = 26U
    const val R_RISCV_LO12_I = 27U
    const val R_RISCV_LO12_S = 28U

    enum class RelocType{
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

    fun UInt.mask12Hi7(): UInt = this shr 5

    fun UInt.mask12Lo5(): UInt = this and 0b000000011111U

    fun UInt.mask32Hi20(): UInt = this shr 12

    fun UInt.mask32Lo12(): UInt = this and 0b111111111111U

    /**
     * @param index 1 (lowest) .. 32 (highest possible)
     */
    fun UInt.bit(index: Int): UInt = (this shr (index - 1)) and 1U

    fun UInt.lowest4(): UInt = this and 0b1111U
    fun UInt.lowest6(): UInt = this and 0b111111U
    fun UInt.lowest8(): UInt = this and 0b11111111U

    fun UInt.lowest10(): UInt = this and 0b1111111111U

    fun UInt.mask20JalLayout(): UInt = (bit(20) shl 19) or (lowest10() shl 9) or (bit(11) shl 8) or (this shr 11).lowest8()

    fun UInt.mask12CBraLayout7(): UInt = (bit(12) shl 6) or (this shr 4).lowest6()

    fun UInt.mask12CBraLayout5(): UInt = (lowest4() shl 1) or bit(11)
}




