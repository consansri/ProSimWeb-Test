package cengine.lang.asm.ast.target.ikrrisc2

data object IKRR2Const {

    const val I_OP6_ADDI = 0x00U
    const val I_OP6_ADDLI = 0x01U
    const val I_OP6_ADDHI = 0x02U
    const val I_OP6_AND0I = 0x04U
    const val I_OP6_AND1I = 0x05U
    const val I_OP6_ORI = 0x06U
    const val I_OP6_XORI = 0x07U
    const val I_OP6_CMPUI = 0x08U
    const val I_OP6_CMPSI = 0x09U
    const val I_OP6_LDD = 0x10U
    const val I_OP6_STD = 0x14U

    const val FUNCT6_R2 = 0x3FU

    const val R2_OP6_ADD = 0x00U
    const val R2_OP6_ADDX = 0x20U
    const val R2_OP6_SUB = 0x02U
    const val R2_OP6_SUBX = 0x22U
    const val R2_OP6_AND = 0x04U
    const val R2_OP6_OR = 0x06U
    const val R2_OP6_XOR = 0x07U
    const val R2_OP6_CMPU = 0x08U
    const val R2_OP6_CMPS = 0x09U
    const val R2_OP6_LDR = 0x10U
    const val R2_OP6_STR = 0x14U

    const val FUNCT6_R1 = 0x3FU
    const val CONST6_SHIFT_ROTATE = 0x01U
    const val CONST6_SWAPH = 0x10U

    const val R1_OP6_LSL = 0x28U
    const val R1_OP6_LSR = 0x29U
    const val R1_OP6_ASL = 0x2AU
    const val R1_OP6_ASR = 0x2BU
    const val R1_OP6_ROL = 0x2CU
    const val R1_OP6_ROR = 0x2DU
    const val R1_OP6_EXTB = 0x30U
    const val R1_OP6_EXTH = 0x31U
    const val R1_OP6_SWAPB = 0x32U
    const val R1_OP6_SWAPH = 0x2CU
    const val R1_OP6_NOT = 0x33U
    const val R1_OP6_JMP = 0x3CU
    const val R1_OP6_JSR = 0x3DU

    const val B_OP6_BRA = 0x3CU
    const val B_OP6_BSR = 0x3DU
    const val B_OP6_COND_BRA = 0x3EU
    const val B_FUNCT3_BEQ = 0x0U
    const val B_FUNCT3_BNE = 0x1U
    const val B_FUNCT3_BLT = 0x2U
    const val B_FUNCT3_BGT = 0x3U
    const val B_FUNCT3_BLE = 0x4U
    const val B_FUNCT3_BGE = 0x5U


}