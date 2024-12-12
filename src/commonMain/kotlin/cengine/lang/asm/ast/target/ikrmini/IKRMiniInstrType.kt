package cengine.lang.asm.ast.target.ikrmini

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNode
import cengine.util.newint.UInt16

enum class IKRMiniInstrType(override val detectionName: String, private val opCode: UInt16, private val paramType: IKRMiniParamType, val description: String) : InstrTypeInterface {
    // Data Transport
    LOAD_IMM("LOAD", UInt16(0x010CU), IKRMiniParamType.IMM, "load AC"),
    LOAD_DIR("LOAD", UInt16(0x020CU), IKRMiniParamType.DIR, "load AC"),
    LOAD_IND("LOAD", UInt16(0x030CU), IKRMiniParamType.IND, "load AC"),
    LOAD_IND_OFF("LOAD", UInt16(0x040CU), IKRMiniParamType.IND_OFF, "load AC"),

    LOADI("LOADI", UInt16(0x200CU), IKRMiniParamType.IMPL, "load indirect"),
    STORE_DIR("STORE", UInt16(0x3200U), IKRMiniParamType.DIR, "store AC at address"),
    STORE_IND("STORE", UInt16(0x3300U), IKRMiniParamType.IND, "store AC at address"),
    STORE_IND_OFF("STORE", UInt16(0x3400U), IKRMiniParamType.IND_OFF, "store AC at address"),

    // Data Manipulation
    AND_IMM("AND", UInt16(0x018AU), IKRMiniParamType.IMM, "and (logic)"),
    AND_DIR("AND", UInt16(0x028AU), IKRMiniParamType.DIR, "and (logic)"),
    AND_IND("AND", UInt16(0x038AU), IKRMiniParamType.IND, "and (logic)"),
    AND_IND_OFF("AND", UInt16(0x048AU), IKRMiniParamType.IND_OFF, "and (logic)"),
    OR_IMM("OR", UInt16(0x0188U), IKRMiniParamType.IMM, "or (logic)"),
    OR_DIR("OR", UInt16(0x0288U), IKRMiniParamType.DIR, "or (logic)"),
    OR_IND("OR", UInt16(0x0388U), IKRMiniParamType.IND, "or (logic)"),
    OR_IND_OFF("OR", UInt16(0x0488U), IKRMiniParamType.IND_OFF, "or (logic)"),
    XOR_IMM("XOR", UInt16(0x0189U), IKRMiniParamType.IMM, "xor (logic)"),
    XOR_DIR("XOR", UInt16(0x0289U), IKRMiniParamType.DIR, "xor (logic)"),
    XOR_IND("XOR", UInt16(0x0389U), IKRMiniParamType.IND, "xor (logic)"),
    XOR_IND_OFF("XOR", UInt16(0x0489U), IKRMiniParamType.IND_OFF, "xor (logic)"),
    ADD_IMM("ADD", UInt16(0x018DU), IKRMiniParamType.IMM, "add"),
    ADD_DIR("ADD", UInt16(0x028DU), IKRMiniParamType.DIR, "add"),
    ADD_IND("ADD", UInt16(0x038DU), IKRMiniParamType.IND, "add"),
    ADD_IND_OFF("ADD", UInt16(0x048DU), IKRMiniParamType.IND_OFF, "add"),
    ADDC_IMM("ADDC", UInt16(0x01ADU), IKRMiniParamType.IMM, "add with carry"),
    ADDC_DIR("ADDC", UInt16(0x02ADU), IKRMiniParamType.DIR, "add with carry"),
    ADDC_IND("ADDC", UInt16(0x03ADU), IKRMiniParamType.IND, "add with carry"),
    ADDC_IND_OFF("ADDC", UInt16(0x04ADU), IKRMiniParamType.IND_OFF, "add with carry"),
    SUB_IMM("SUB", UInt16(0x018EU), IKRMiniParamType.IMM, "sub"),
    SUB_DIR("SUB", UInt16(0x028EU), IKRMiniParamType.DIR, "sub"),
    SUB_IND("SUB", UInt16(0x038EU), IKRMiniParamType.IND, "sub"),
    SUB_IND_OFF("SUB", UInt16(0x048EU), IKRMiniParamType.IND_OFF, "sub"),
    SUBC_IMM("SUBC", UInt16(0x01AEU), IKRMiniParamType.IMM, "sub with carry"),
    SUBC_DIR("SUBC", UInt16(0x02AEU), IKRMiniParamType.DIR, "sub with carry"),
    SUBC_IND("SUBC", UInt16(0x03AEU), IKRMiniParamType.IND, "sub with carry"),
    SUBC_IND_OFF("SUBC", UInt16(0x04AEU), IKRMiniParamType.IND_OFF, "sub with carry"),

    LSL("LSL", UInt16(0x00A0U), IKRMiniParamType.IMPL, "logic shift left"),
    LSL_DIR("LSL", UInt16(0x0220U), IKRMiniParamType.DIR, "logic shift left"),
    LSL_IND("LSL", UInt16(0x0320U), IKRMiniParamType.IND, "logic shift left"),
    LSL_IND_OFF("LSL", UInt16(0x0420U), IKRMiniParamType.IND_OFF, "logic shift left"),
    LSR("LSR", UInt16(0x00A1U), IKRMiniParamType.IMPL, "logic shift right"),
    LSR_DIR("LSR", UInt16(0x0221U), IKRMiniParamType.DIR, "logic shift right"),
    LSR_IND("LSR", UInt16(0x0321U), IKRMiniParamType.IND, "logic shift right"),
    LSR_IND_OFF("LSR", UInt16(0x0421U), IKRMiniParamType.IND_OFF, "logic shift right"),
    ROL("ROL", UInt16(0x00A2U), IKRMiniParamType.IMPL, "rotate left"),
    ROL_DIR("ROL", UInt16(0x0222U), IKRMiniParamType.DIR, "rotate left"),
    ROL_IND("ROL", UInt16(0x0322U), IKRMiniParamType.IND, "rotate left"),
    ROL_IND_OFF("ROL", UInt16(0x0422U), IKRMiniParamType.IND_OFF, "rotate left"),
    ROR("ROR", UInt16(0x00A3U), IKRMiniParamType.IMPL, "rotate right"),
    ROR_DIR("ROR", UInt16(0x0223U), IKRMiniParamType.DIR, "rotate right"),
    ROR_IND("ROR", UInt16(0x0323U), IKRMiniParamType.IND, "rotate right"),
    ROR_IND_OFF("ROR", UInt16(0x0423U), IKRMiniParamType.IND_OFF, "rotate right"),
    ASL("ASL", UInt16(0x00A4U), IKRMiniParamType.IMPL, "arithmetic shift left"),
    ASL_DIR("ASL", UInt16(0x0224U), IKRMiniParamType.DIR, "arithmetic shift left"),
    ASL_IND("ASL", UInt16(0x0324U), IKRMiniParamType.IND, "arithmetic shift left"),
    ASL_IND_OFF("ASL", UInt16(0x0424U), IKRMiniParamType.IND_OFF, "arithmetic shift left"),
    ASR("ASR", UInt16(0x00A5U), IKRMiniParamType.IMPL, "arithmetic shift right"),
    ASR_DIR("ASR", UInt16(0x0225U), IKRMiniParamType.DIR, "arithmetic shift right"),
    ASR_IND("ASR", UInt16(0x0325U), IKRMiniParamType.IND, "arithmetic shift right"),
    ASR_IND_OFF("ASR", UInt16(0x0425U), IKRMiniParamType.IND_OFF, "arithmetic shift right"),

    RCL("RCL", UInt16(0x00A6U), IKRMiniParamType.IMPL, "rotate left with carry"),
    RCL_IMM("RCL", UInt16(0x0126U), IKRMiniParamType.IMM, "rotate left with carry"),
    RCL_DIR("RCL", UInt16(0x0226U), IKRMiniParamType.DIR, "rotate left with carry"),
    RCL_IND("RCL", UInt16(0x0326U), IKRMiniParamType.IND, "rotate left with carry"),
    RCL_IND_OFF("RCL", UInt16(0x0426U), IKRMiniParamType.IND_OFF, "rotate left with carry"),
    RCR("RCR", UInt16(0x00A7U), IKRMiniParamType.IMPL, "rotate right with carry"),
    RCR_IMM("RCR", UInt16(0x0127U), IKRMiniParamType.IMM, "rotate right with carry"),
    RCR_DIR("RCR", UInt16(0x0227U), IKRMiniParamType.DIR, "rotate right with carry"),
    RCR_IND("RCR", UInt16(0x0327U), IKRMiniParamType.IND, "rotate right with carry"),
    RCR_IND_OFF("RCR", UInt16(0x0427U), IKRMiniParamType.IND_OFF, "rotate right with carry"),
    NOT("NOT", UInt16(0x008BU), IKRMiniParamType.IMPL, "invert (logic not)"),
    NOT_DIR("NOT", UInt16(0x020BU), IKRMiniParamType.DIR, "invert (logic not)"),
    NOT_IND("NOT", UInt16(0x030BU), IKRMiniParamType.IND, "invert (logic not)"),
    NOT_IND_OFF("NOT", UInt16(0x040BU), IKRMiniParamType.IND_OFF, "invert (logic not)"),

    NEG_DIR("NEG", UInt16(0x024EU), IKRMiniParamType.DIR, "negotiate"),
    NEG_IND("NEG", UInt16(0x034EU), IKRMiniParamType.IND, "negotiate"),
    NEG_IND_OFF("NEG", UInt16(0x044EU), IKRMiniParamType.IND_OFF, "negotiate"),

    CLR("CLR", UInt16(0x004CU), IKRMiniParamType.IMPL, "clear"),

    INC("INC", UInt16(0x009CU), IKRMiniParamType.IMPL, "increment (+1)"),
    INC_DIR("INC", UInt16(0x021CU), IKRMiniParamType.DIR, "increment (+1)"),
    INC_IND("INC", UInt16(0x031CU), IKRMiniParamType.IND, "increment (+1)"),
    INC_IND_OFF("INC", UInt16(0x041CU), IKRMiniParamType.IND_OFF, "increment (+1)"),
    DEC("DEC", UInt16(0x009FU), IKRMiniParamType.IMPL, "decrement (-1)"),
    DEC_DIR("DEC", UInt16(0x021FU), IKRMiniParamType.DIR, "decrement (-1)"),
    DEC_IND("DEC", UInt16(0x031FU), IKRMiniParamType.IND, "decrement (-1)"),
    DEC_IND_OFF("DEC", UInt16(0x041FU), IKRMiniParamType.IND_OFF, "decrement (-1)"),

    // Unconditional Branches
    BSR("BSR", UInt16(0x510CU), IKRMiniParamType.DEST, "branch and save return address in AC"),
    JMP("JMP", UInt16(0x4000U), IKRMiniParamType.IMPL, "jump to address in AC"),
    BRA("BRA", UInt16(0x6101U), IKRMiniParamType.DEST, "branch"),

    // Conditional Branches
    BHI("BHI", UInt16(0x6102U), IKRMiniParamType.DEST, "branch if higher"),
    BLS("BLS", UInt16(0x6103U), IKRMiniParamType.DEST, "branch if lower or same"),
    BCC("BCC", UInt16(0x6104U), IKRMiniParamType.DEST, "branch if carry clear"),
    BCS("BCS", UInt16(0x6105U), IKRMiniParamType.DEST, "branch if carry set"),
    BNE("BNE", UInt16(0x6106U), IKRMiniParamType.DEST, "branch if not equal"),
    BEQ("BEQ", UInt16(0x6107U), IKRMiniParamType.DEST, "branch if equal"),
    BVC("BVC", UInt16(0x6108U), IKRMiniParamType.DEST, "branch if overflow clear"),
    BVS("BVS", UInt16(0x6109U), IKRMiniParamType.DEST, "branch if overflow set"),
    BPL("BPL", UInt16(0x610AU), IKRMiniParamType.DEST, "branch if positive"),
    BMI("BMI", UInt16(0x610BU), IKRMiniParamType.DEST, "branch if negative"),
    BGE("BGE", UInt16(0x610CU), IKRMiniParamType.DEST, "branch if greater or equal"),
    BLT("BLT", UInt16(0x610DU), IKRMiniParamType.DEST, "branch if less than"),
    BGT("BGT", UInt16(0x610EU), IKRMiniParamType.DEST, "branch if greater than"),
    BLE("BLE", UInt16(0x610FU), IKRMiniParamType.DEST, "branch if less or equal");

    override val typeName: String = name
    override val addressInstancesNeeded: Int? = paramType.wordAmount * 2
    override val inCodeInfo: String? get() = description
    override val paramRule: Rule? get() = paramType.rule

    override fun resolve(builder: AsmCodeGenerator<*>, instr: ASNode.Instruction) {
        when (this) {
            LOAD_IMM, AND_IMM, OR_IMM, XOR_IMM, ADD_IMM, ADDC_IMM, SUB_IMM, SUBC_IMM, RCL_IMM, RCR_IMM,
                -> {
                val expr = instr.nodes.filterIsInstance<ASNode.NumericExpr>().firstOrNull()
                if (expr == null) {
                    instr.addError("Numeric Expression is missing!")
                    return
                }

                builder.currentSection.content.put(opCode.toUShort())

                val evaluated = expr.evaluate(builder)

                if (!evaluated.fitsInSignedOrUnsigned(16)) {
                    expr.addError("$evaluated exceeds 16 bits")
                }

                val imm = try {
                    evaluated.toShort().toUShort()
                } catch (e: Exception) {
                    expr.addInfo("Unsigned Interpretation!")
                    evaluated.toUShort()
                }

                builder.currentSection.content.put(imm)
            }

            LOAD_DIR, STORE_DIR, AND_DIR, OR_DIR, XOR_DIR, ADD_DIR, ADDC_DIR, SUB_DIR, SUBC_DIR,
            LSL_DIR, LSR_DIR, ROL_DIR, ROR_DIR, ASL_DIR, ASR_DIR, RCL_DIR, RCR_DIR, NOT_DIR, NEG_DIR, INC_DIR, DEC_DIR,
                -> {
                val expr = instr.nodes.filterIsInstance<ASNode.NumericExpr>().firstOrNull()
                if (expr == null) {
                    instr.addError("Numeric Expression is missing!")
                    return
                }

                builder.currentSection.content.put(opCode.toUShort())

                val evaluated = expr.evaluate(builder)

                if (!evaluated.fitsInSignedOrUnsigned(16)) {
                    expr.addError("$evaluated exceeds 16 bits")
                }

                val imm = try {
                    evaluated.toShort().toUShort()
                } catch (e: Exception) {
                    expr.addInfo("Unsigned Interpretation!")
                    evaluated.toUShort()
                }

                builder.currentSection.content.put(imm)
            }

            LOAD_IND, STORE_IND, AND_IND, OR_IND, XOR_IND, ADD_IND, ADDC_IND, SUB_IND, SUBC_IND, LSL_IND, LSR_IND, ROL_IND,
            ROR_IND, ASL_IND, ASR_IND, RCL_IND, RCR_IND, NOT_IND, NEG_IND, INC_IND, DEC_IND,
                -> {
                val expr = instr.nodes.filterIsInstance<ASNode.NumericExpr>().firstOrNull()
                if (expr == null) {
                    instr.addError("Numeric Expression is missing!")
                    return
                }

                builder.currentSection.content.put(opCode.toUShort())

                val evaluated = expr.evaluate(builder)

                if (!evaluated.fitsInSignedOrUnsigned(16)) {
                    expr.addError("$evaluated exceeds 16 bits")
                }

                val imm = try {
                    evaluated.toShort().toUShort()
                } catch (e: Exception) {
                    expr.addInfo("Unsigned Interpretation!")
                    evaluated.toUShort()
                }

                builder.currentSection.content.put(imm)
            }

            LOAD_IND_OFF, STORE_IND_OFF, AND_IND_OFF, OR_IND_OFF, XOR_IND_OFF, ADD_IND_OFF, ADDC_IND_OFF, SUB_IND_OFF, SUBC_IND_OFF,
            LSL_IND_OFF, LSR_IND_OFF, ROL_IND_OFF, ROR_IND_OFF, ASL_IND_OFF, ASR_IND_OFF, RCL_IND_OFF, RCR_IND_OFF, NOT_IND_OFF, NEG_IND_OFF,
            INC_IND_OFF, DEC_IND_OFF,
                -> {
                val exprs = instr.nodes.filterIsInstance<ASNode.NumericExpr>()
                if (exprs.size != 2) {
                    instr.addError("Expected 2 Numeric Expressions but received $exprs!")
                    return
                }

                builder.currentSection.content.put(opCode.toUShort())

                val addressExpr = exprs[1]
                val addressEval = addressExpr.evaluate(builder)

                if (!addressEval.fitsInSignedOrUnsigned(16)) {
                    addressExpr.addError("$addressEval exceeds 16 bits!")
                }

                val address = try {
                    addressEval.toShort().toUShort()
                } catch (e: Exception) {
                    addressExpr.addInfo("Unsigned Interpretation!")
                    addressEval.toUShort()
                }

                builder.currentSection.content.put(address)

                val offsetExpr = exprs[0]
                val offsetEval = offsetExpr.evaluate(builder)

                if (!offsetEval.fitsInSignedOrUnsigned(16)) {
                    offsetExpr.addError("$offsetEval exceeds 16 bits!")
                }

                val offset = try {
                    offsetEval.toShort().toUShort()
                } catch (e: Exception) {
                    offsetExpr.addInfo("Unsigned Interpretation!")
                    offsetEval.toUShort()
                }

                builder.currentSection.content.put(offset)

            }

            LOADI, LSL, LSR, ROL, ROR, ASL, ASR, RCL, RCR, NOT, CLR, INC, DEC, JMP,
                -> builder.currentSection.content.put(opCode.toUShort())

            BSR, BRA, BHI, BLS, BCC, BCS, BNE, BEQ, BVC, BVS, BPL, BMI, BGE, BLT, BGT, BLE -> {
                builder.currentSection.queueLateInit(instr, 2)
            }
        }
    }

    override fun lateEvaluation(builder: AsmCodeGenerator<*>, section: AsmCodeGenerator.Section, instr: ASNode.Instruction, index: Int) {
        when (this) {
            BRA, BSR, BHI, BLS, BCC, BCS, BNE, BEQ, BVC, BVS, BPL, BMI, BGE, BLT, BGT, BLE -> {
                val targetExpr = instr.nodes.filterIsInstance<ASNode.NumericExpr>().firstOrNull()
                if (targetExpr == null) {
                    instr.addError("Expected Numeric Expression!")
                    return
                }

                val targetEval = targetExpr.evaluate(builder).toUInt16()

                val pcEval = (section.address.toUInt16() + index)
                val relative = (targetEval - pcEval).toShort()
                section.content[index] = opCode.toUShort()
                section.content[index + 1] = relative
            }

            else -> {
                // Nothing to do here
            }
        }
    }


}