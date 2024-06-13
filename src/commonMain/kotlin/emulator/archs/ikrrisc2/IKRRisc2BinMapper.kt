package emulator.archs.ikrrisc2

import Settings
import emulator.archs.ikrrisc2.IKRRisc2BinMapper.MaskLabel.*
import emulator.kit.Architecture
import emulator.kit.assembler.parser.Parser
import emulator.kit.common.RegContainer
import emulator.kit.nativeWarn
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.Bin
import emulator.kit.types.Variable.Value.Dec

object IKRRisc2BinMapper {

    fun decodeBinary(arch: Architecture, bin: Bin): DecodeResult? {
        for (type in InstrType.entries) {
            val result = type.opCode.checkOpCode(bin)
            if (!result.matches) continue

            val binStr = bin.toRawString()
            val rc = arch.getRegByAddr(Bin(binStr.substring(6, 11), IKRRisc2.REG_SIZE)) ?: throw Error("Couldn't find valid register for ${binStr.substring(7, 12)}.")
            val rb = arch.getRegByAddr(Bin(binStr.substring(11, 16), IKRRisc2.REG_SIZE)) ?: throw Error("Couldn't find valid register for ${binStr.substring(12, 17)}.")
            val ra = arch.getRegByAddr(Bin(binStr.substring(27, 32), IKRRisc2.REG_SIZE)) ?: throw Error("Couldn't find valid register for ${binStr.substring(27, 32)}.")
            val imm16 = Bin(binStr.substring(16, 32), Bit16)
            val disp16 = imm16
            val disp18 = Bin(binStr.substring(14, 32), Bit18)
            val disp26 = Bin(binStr.substring(6, 32), Bit26)

            return DecodeResult(
                type,
                rc,
                rb,
                ra,
                imm16,
                disp16,
                disp18,
                disp26
            )
        }
        return null
    }

    fun generateBinary(instr: IKRRisc2Assembler.IKRRisc2Instr, lblDisplacement: Dec?): Bin {
        val type = instr.type
        val paramType = type.paramType
        val regs = instr.regs

        when (paramType) {
            ParamType.I_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rb = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val imm16 = instr.immediate.toBin().getUResized(Bit16)

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, IMM16 to imm16)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.R2_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rb = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val ra = regs.getOrNull(2)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, RA to ra)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.R1_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rb = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.L_OFF_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rb = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val disp16 = instr.immediate.toBin().getUResized(Bit16)

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, DISP16 to disp16)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.L_INDEX_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rb = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val ra = regs.getOrNull(2)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, RA to ra)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.S_OFF_TYPE -> {
                val rb = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rc = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val disp16 = instr.immediate.toBin().getUResized(Bit16)

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, DISP16 to disp16)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.S_INDEX_TYPE -> {
                val rb = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val ra = regs.getOrNull(1)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val rc = regs.getOrNull(2)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")

                return type.opCode.getOpCode(mapOf(RC to rc, RB to rb, RA to ra)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.B_DISP18_TYPE -> {
                val rc = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")
                val displ = lblDisplacement ?: throw Parser.ParserError(instr.getFirstToken(), "Label displacement is missing for type $type.")
                if (!displ.check(Bit18).valid) {
                    throw Parser.ParserError(instr.getFirstToken(), "Label displacement ($displ) exceeds 18 Bits.")
                }
                val displ18 = displ.getResized(Bit18).toBin()

                return type.opCode.getOpCode(mapOf(RC to rc, DISP18 to displ18)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.B_DISP26_TYPE -> {
                val displ = lblDisplacement ?: throw Parser.ParserError(instr.getFirstToken(), "Label displacement is missing for type $type.")
                if (!displ.check(Bit26).valid) {
                    throw Parser.ParserError(instr.getFirstToken(), "Label displacement ($displ) exceeds 26 Bits.")
                }
                val displ26 = displ.getResized(Bit26).toBin()

                return type.opCode.getOpCode(mapOf(DISP26 to displ26)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }

            ParamType.B_REG_TYPE -> {
                val rb = regs.getOrNull(0)?.address?.toBin() ?: throw Parser.ParserError(instr.getFirstToken(), "Expected register is missing.")

                return type.opCode.getOpCode(mapOf(RB to rb)) ?: throw Parser.ParserError(instr.getFirstToken(), "Couldn't resolve OpCode for type $type.")
            }
        }
    }

    data class DecodeResult(
        val type: InstrType,
        val rc: RegContainer.Register,
        val rb: RegContainer.Register,
        val ra: RegContainer.Register,
        val imm16: Bin,
        val disp16: Bin,
        val disp18: Bin,
        val disp26: Bin
    )

    class OpCode(val opMask: String, vararg val maskLabels: MaskLabel) {

        val opMaskList = opMask.removePrefix(Settings.PRESTRING_BINARY).split(" ")
        fun checkOpCode(bin: Bin): CheckResult {
            if (bin.size != IKRRisc2.WORD_WIDTH) {
                return CheckResult(false)
            }
            // Check OpCode
            val binaryString = bin.getRawBinStr()

            val binMap = mutableMapOf<MaskLabel, Bin>()

            // check static labels
            for (labelID in maskLabels.indices) {
                val label = maskLabels[labelID]
                if (label.static) {
                    val substring = getSubString(binaryString, label)
                    if (substring != opMaskList[labelID]) {
                        return CheckResult(false)
                    }
                }
            }

            // add not static labels
            for (labelID in maskLabels.indices) {
                val label = maskLabels[labelID]
                if (!label.static) {
                    val substring = getSubString(binaryString, label)
                    if (label.maxSize != null) {
                        binMap[label] = Bin(substring, label.maxSize)
                    }
                }
            }

            return CheckResult(true, binMap)
        }

        fun getOpCode(parameterMap: Map<MaskLabel, Bin>): Bin? {
            val opCode = opMaskList.toMutableList()
            var length = 0
            opCode.forEach { length += it.length }
            if (length != IKRRisc2.WORD_WIDTH.bitWidth) {
                nativeWarn("BinMapper.OpCode: OpMask isn't ${IKRRisc2.WORD_WIDTH} Binary! -> returning null")
                return null
            }
            if (opCode.size != maskLabels.size) {
                nativeWarn("BinMapper.OpCode: OpMask [$opMask] and Labels [${maskLabels.joinToString { it.name }}] aren't the same size! -> returning null")
                return null
            }

            for (labelID in maskLabels.indices) {
                val maskLabel = maskLabels[labelID]
                if (!maskLabel.static) {
                    val param = parameterMap[maskLabel]
                    if (param != null) {
                        val size = maskLabel.maxSize
                        if (size != null) {
                            opCode[labelID] = param.getUResized(size).getRawBinStr()
                        } else {
                            nativeWarn("BinMapper.OpCode.getOpCode(): can't insert ByteValue in OpMask without a maxSize! -> returning null")
                            return null
                        }
                    } else {
                        nativeWarn("BinMapper.OpCode.getOpCode(): parameter [${maskLabel.name}] not found! -> inserting zeros")
                        val bitWidth = maskLabel.maxSize?.bitWidth
                        bitWidth?.let {
                            opCode[labelID] = "0".repeat(it)
                        }
                    }

                }
            }

            return Bin(opCode.joinToString("") { it }, Bit32)
        }

        private fun getSubString(binary: String, maskLabel: MaskLabel): String {
            var startIndex = 0
            for (maskID in opMaskList.indices) {
                val maskString = opMaskList[maskID]
                if (maskLabels[maskID] == maskLabel) {
                    return binary.substring(startIndex, startIndex + maskString.length)
                }
                startIndex += maskString.length
            }
            return ""
        }

        private fun getMaskString(maskLabel: MaskLabel): String {
            for (labelID in maskLabels.indices) {
                val label = maskLabels[labelID]
                if (label == maskLabel) {
                    return opMaskList[labelID]
                }
            }
            return ""
        }

        data class CheckResult(val matches: Boolean, val binMap: Map<MaskLabel, Bin> = mapOf())
    }

    enum class MaskLabel(val static: Boolean, val maxSize: Variable.Size? = null) {
        OPCODE(true, Bit6),
        FUNCT6(true, Bit6),
        FUNCT5(true, Bit5),
        RA(false, Bit5),
        RB(false, Bit5),
        RC(false, Bit5),
        IMM16(false, Bit16),
        DISP16(false, Bit16),
        DISP18(false, Bit18),
        DISP26(false, Bit26),
        FUNCT3(true, Bit2),
        NONE5(true, Bit5),
        NONE(true)
    }


}