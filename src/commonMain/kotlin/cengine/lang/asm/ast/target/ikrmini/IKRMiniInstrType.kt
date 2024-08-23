package cengine.lang.asm.ast.target.ikrmini

import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.elf.ELFBuilder

enum class IKRMiniInstrType(override val detectionName: String, val opCode: UShort, val paramType: IKRMiniParamType, val description: String, val labelDependent: Boolean = false) : InstrTypeInterface {
    // Data Transport
    LOAD_IMM("LOAD", 0x010CU, IKRMiniParamType.IMM, "load AC"),
    LOAD_DIR("LOAD", 0x020CU, IKRMiniParamType.DIR, "load AC"),
    LOAD_IND("LOAD", 0x030CU, IKRMiniParamType.IND, "load AC"),
    LOAD_IND_OFF("LOAD", 0x040CU, IKRMiniParamType.IND_OFF, "load AC"),

    LOADI("LOADI", 0x200CU, IKRMiniParamType.IMPL, "load indirect"),
    STORE_DIR("STORE", 0x3200U, IKRMiniParamType.DIR, "store AC at address"),
    STORE_IND("STORE", 0x3300U, IKRMiniParamType.IND, "store AC at address"),
    STORE_IND_OFF("STORE", 0x3400U, IKRMiniParamType.IND_OFF, "store AC at address"),

    // Data Manipulation
    AND_IMM("AND", 0x018AU, IKRMiniParamType.IMM, "and (logic)"),
    AND_DIR("AND", 0x028AU, IKRMiniParamType.DIR, "and (logic)"),
    AND_IND("AND", 0x038AU, IKRMiniParamType.IND, "and (logic)"),
    AND_IND_OFF("AND", 0x048AU, IKRMiniParamType.IND_OFF, "and (logic)"),
    OR_IMM("OR", 0x0188U, IKRMiniParamType.IMM, "or (logic)"),
    OR_DIR("OR", 0x0288U, IKRMiniParamType.DIR, "or (logic)"),
    OR_IND("OR", 0x0388U, IKRMiniParamType.IND, "or (logic)"),
    OR_IND_OFF("OR", 0x0488U, IKRMiniParamType.IND_OFF, "or (logic)"),
    XOR_IMM("XOR", 0x0189U, IKRMiniParamType.IMM, "xor (logic)"),
    XOR_DIR("XOR", 0x0289U, IKRMiniParamType.DIR, "xor (logic)"),
    XOR_IND("XOR", 0x0389U, IKRMiniParamType.IND, "xor (logic)"),
    XOR_IND_OFF("XOR", 0x0489U, IKRMiniParamType.IND_OFF, "xor (logic)"),
    ADD_IMM("ADD", 0x018DU, IKRMiniParamType.IMM, "add"),
    ADD_DIR("ADD", 0x028DU, IKRMiniParamType.DIR, "add"),
    ADD_IND("ADD", 0x038DU, IKRMiniParamType.IND, "add"),
    ADD_IND_OFF("ADD", 0x048DU, IKRMiniParamType.IND_OFF, "add"),
    ADDC_IMM("ADDC", 0x01ADU, IKRMiniParamType.IMM, "add with carry"),
    ADDC_DIR("ADDC", 0x02ADU, IKRMiniParamType.DIR, "add with carry"),
    ADDC_IND("ADDC", 0x03ADU, IKRMiniParamType.IND, "add with carry"),
    ADDC_IND_OFF("ADDC", 0x04ADU, IKRMiniParamType.IND_OFF, "add with carry"),
    SUB_IMM("SUB", 0x018EU, IKRMiniParamType.IMM, "sub"),
    SUB_DIR("SUB", 0x028EU, IKRMiniParamType.DIR, "sub"),
    SUB_IND("SUB", 0x038EU, IKRMiniParamType.IND, "sub"),
    SUB_IND_OFF("SUB", 0x048EU, IKRMiniParamType.IND_OFF, "sub"),
    SUBC_IMM("SUBC", 0x01AEU, IKRMiniParamType.IMM, "sub with carry"),
    SUBC_DIR("SUBC", 0x02AEU, IKRMiniParamType.DIR, "sub with carry"),
    SUBC_IND("SUBC", 0x03AEU, IKRMiniParamType.IND, "sub with carry"),
    SUBC_IND_OFF("SUBC", 0x04AEU, IKRMiniParamType.IND_OFF, "sub with carry"),

    LSL_IMPL("LSL", 0x00A0U, IKRMiniParamType.IMPL, "logic shift left"),
    LSL_DIR("LSL", 0x0220U, IKRMiniParamType.DIR, "logic shift left"),
    LSL_IND("LSL", 0x0320U, IKRMiniParamType.IND, "logic shift left"),
    LSL_IND_OFF("LSL", 0x0420U, IKRMiniParamType.IND_OFF, "logic shift left"),
    LSR_IMPL("LSR", 0x00A1U, IKRMiniParamType.IMPL, "logic shift right"),
    LSR_DIR("LSR", 0x0221U, IKRMiniParamType.DIR, "logic shift right"),
    LSR_IND("LSR", 0x0321U, IKRMiniParamType.IND, "logic shift right"),
    LSR_IND_OFF("LSR", 0x0421U, IKRMiniParamType.IND_OFF, "logic shift right"),
    ROL_IMPL("ROL", 0x00A2U, IKRMiniParamType.IMPL, "rotate left"),
    ROL_DIR("ROL", 0x0222U, IKRMiniParamType.DIR, "rotate left"),
    ROL_IND("ROL", 0x0322U, IKRMiniParamType.IND, "rotate left"),
    ROL_IND_OFF("ROL", 0x0422U, IKRMiniParamType.IND_OFF, "rotate left"),
    ROR_IMPL("ROR", 0x00A3U, IKRMiniParamType.IMPL, "rotate right"),
    ROR_DIR("ROR", 0x0223U, IKRMiniParamType.DIR, "rotate right"),
    ROR_IND("ROR", 0x0323U, IKRMiniParamType.IND, "rotate right"),
    ROR_IND_OFF("ROR", 0x0423U, IKRMiniParamType.IND_OFF, "rotate right"),
    ASL_IMPL("ASL", 0x00A4U, IKRMiniParamType.IMPL, "arithmetic shift left"),
    ASL_DIR("ASL", 0x0224U, IKRMiniParamType.DIR, "arithmetic shift left"),
    ASL_IND("ASL", 0x0324U, IKRMiniParamType.IND, "arithmetic shift left"),
    ASL_IND_OFF("ASL", 0x0424U, IKRMiniParamType.IND_OFF, "arithmetic shift left"),
    ASR_IMPL("ASR", 0x00A5U, IKRMiniParamType.IMPL, "arithmetic shift right"),
    ASR_DIR("ASR", 0x0225U, IKRMiniParamType.DIR, "arithmetic shift right"),
    ASR_IND("ASR", 0x0325U, IKRMiniParamType.IND, "arithmetic shift right"),
    ASR_IND_OFF("ASR", 0x0425U, IKRMiniParamType.IND_OFF, "arithmetic shift right"),

    RCL_IMPL("RCL", 0x00A6U, IKRMiniParamType.IMPL, "rotate left with carry"),
    RCL_IMM("RCL", 0x0126U, IKRMiniParamType.IMM, "rotate left with carry"),
    RCL_DIR("RCL", 0x0226U, IKRMiniParamType.DIR, "rotate left with carry"),
    RCL_IND("RCL", 0x0326U, IKRMiniParamType.IND, "rotate left with carry"),
    RCL_IND_OFF("RCL", 0x0426U, IKRMiniParamType.IND_OFF, "rotate left with carry"),
    RCR_IMPL("RCR", 0x00A7U, IKRMiniParamType.IMPL, "rotate right with carry"),
    RCR_IMM("RCR", 0x0127U, IKRMiniParamType.IMM, "rotate right with carry"),
    RCR_DIR("RCR", 0x0227U, IKRMiniParamType.DIR, "rotate right with carry"),
    RCR_IND("RCR", 0x0327U, IKRMiniParamType.IND, "rotate right with carry"),
    RCR_IND_OFF("RCR", 0x0427U, IKRMiniParamType.IND_OFF, "rotate right with carry"),
    NOT_IMPL("NOT", 0x008BU, IKRMiniParamType.IMPL, "invert (logic not)"),
    NOT_DIR("NOT", 0x020BU, IKRMiniParamType.DIR, "invert (logic not)"),
    NOT_IND("NOT", 0x030BU, IKRMiniParamType.IND, "invert (logic not)"),
    NOT_IND_OFF("NOT", 0x040BU, IKRMiniParamType.IND_OFF, "invert (logic not)"),

    NEG_DIR("NEG", 0x024EU, IKRMiniParamType.DIR, "negotiate"),
    NEG_IND("NEG", 0x034EU, IKRMiniParamType.IND, "negotiate"),
    NEG_IND_OFF("NEG", 0x044EU, IKRMiniParamType.IND_OFF, "negotiate"),

    CLR("CLR", 0x004CU, IKRMiniParamType.IMPL, "clear"),

    INC_IMPL("INC", 0x009CU, IKRMiniParamType.IMPL, "increment (+1)"),
    INC_DIR("INC", 0x021CU, IKRMiniParamType.DIR, "increment (+1)"),
    INC_IND("INC", 0x031CU, IKRMiniParamType.IND, "increment (+1)"),
    INC_IND_OFF("INC", 0x041CU, IKRMiniParamType.IND_OFF, "increment (+1)"),
    DEC_IMPL("DEC", 0x009FU, IKRMiniParamType.IMPL, "decrement (-1)"),
    DEC_DIR("DEC", 0x021FU, IKRMiniParamType.DIR, "decrement (-1)"),
    DEC_IND("DEC", 0x031FU, IKRMiniParamType.IND, "decrement (-1)"),
    DEC_IND_OFF("DEC", 0x041FU, IKRMiniParamType.IND_OFF, "decrement (-1)"),

    // Unconditional Branches
    BSR("BSR", 0x510CU, IKRMiniParamType.DEST, "branch and save return address in AC", true),
    JMP("JMP", 0x4000U, IKRMiniParamType.IMPL, "jump to address in AC"),
    BRA("BRA", 0x6101U, IKRMiniParamType.DEST, "branch", true),

    // Conditional Branches
    BHI("BHI", 0x6102U, IKRMiniParamType.DEST, "branch if higher", true),
    BLS("BLS", 0x6103U, IKRMiniParamType.DEST, "branch if lower or same", true),
    BCC("BCC", 0x6104U, IKRMiniParamType.DEST, "branch if carry clear", true),
    BCS("BCS", 0x6105U, IKRMiniParamType.DEST, "branch if carry set", true),
    BNE("BNE", 0x6106U, IKRMiniParamType.DEST, "branch if not equal", true),
    BEQ("BEQ", 0x6107U, IKRMiniParamType.DEST, "branch if equal", true),
    BVC("BVC", 0x6108U, IKRMiniParamType.DEST, "branch if overflow clear", true),
    BVS("BVS", 0x6109U, IKRMiniParamType.DEST, "branch if overflow set", true),
    BPL("BPL", 0x610AU, IKRMiniParamType.DEST, "branch if positive", true),
    BMI("BMI", 0x610BU, IKRMiniParamType.DEST, "branch if negative", true),
    BGE("BGE", 0x610CU, IKRMiniParamType.DEST, "branch if greater or equal", true),
    BLT("BLT", 0x610DU, IKRMiniParamType.DEST, "branch if less than", true),
    BGT("BGT", 0x610EU, IKRMiniParamType.DEST, "branch if greater than", true),
    BLE("BLE", 0x610FU, IKRMiniParamType.DEST, "branch if less or equal", true);

    override val typeName: String = name
    override val bytesNeeded: Int? = paramType.wordAmount * 2
    override val inCodeInfo: String? get() = description
    override val paramRule: Rule? get() = paramType.rule

    override fun resolve(builder: ELFBuilder, instr: ASNode.Instruction) {
        TODO("Not yet implemented")
    }

    override fun lateEvaluation(builder: ELFBuilder, section: ELFBuilder.Section, instr: ASNode.Instruction, index: Int) {
        TODO("Not yet implemented")
    }


}