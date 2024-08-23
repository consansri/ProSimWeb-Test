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
     * MASKS
     */

    fun UInt.MASK_12_HI7(): UInt = this shr 5

    fun UInt.MASK_12_LO5(): UInt = this and 0b000000011111U

    fun UInt.MASK_32_HI20(): UInt = this shr 12

    fun UInt.MASK_32_LO12(): UInt = this and 0b111111111111U

}




