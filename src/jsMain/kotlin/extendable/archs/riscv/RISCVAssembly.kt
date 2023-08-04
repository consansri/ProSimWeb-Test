package extendable.archs.riscv

import StyleConst
import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCVGrammar.E_DIRECTIVE.DirType.*
import extendable.archs.riscv.RISCVGrammar.R_INSTR.InstrType.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.Transcript
import extendable.components.types.MutVal
import tools.DebugTools

class RISCVAssembly(val binaryMapper: RISCVBinMapper, val allocStartAddress: MutVal.Value) : Assembly() {

    val labelBinAddrMap = mutableMapOf<RISCVGrammar.E_LABEL, String>()
    val transcriptEntrys = mutableListOf<Transcript.TranscriptEntry>()
    val binarys = mutableListOf<MutVal.Value.Binary>()

    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val transcript = architecture.getTranscript()
        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.entries.joinToString("") { "\n\t${it.key.wholeName}: ${it.value}" }}")
        }

        for (entryID in transcriptEntrys.indices) {
            val entry = transcriptEntrys[entryID]
            val binary = architecture.getMemory().load(entry.memoryAddress, 4).get().toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (MutVal.Value.Binary(labels.value) == entry.memoryAddress.toBin()) {
                    labelString += "${labels.key.wholeName} "
                }
            }
            entry.addContent(ArchConst.TranscriptHeaders.LABELS, labelString)

            val result = binaryMapper.getInstrFromBinary(binary)
            if (result != null) {
                var branchOffset5: String = ""
                var branchOffset7: String = ""
                var jalOffset20: String = ""
                val paramString = result.binaryMap.entries.joinToString(ArchConst.TRANSCRIPT_PARAMSPLIT) {
                    /*"${it.key.name.lowercase()}\t${*/
                        when (it.key) {
                            RISCVBinMapper.MaskLabel.RD -> {
                                architecture.getRegisterContainer().getRegister(it.value)?.names?.first() ?: it.value.toHex().getHexStr()
                            }

                            RISCVBinMapper.MaskLabel.RS1 -> {
                                architecture.getRegisterContainer().getRegister(it.value)?.names?.first() ?: it.value.toHex().getHexStr()
                            }

                            RISCVBinMapper.MaskLabel.RS2 -> {
                                architecture.getRegisterContainer().getRegister(it.value)?.names?.first() ?: it.value.toHex().getHexStr()
                            }

                            RISCVBinMapper.MaskLabel.SHAMT -> {
                                it.value.toDec().getDecStr()
                            }

                            RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.IMM20 -> {
                                when (result.type) {
                                    JAL -> {
                                        jalOffset20 = it.value.getRawBinaryStr()

                                    }

                                    BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                                        when (it.key) {
                                            RISCVBinMapper.MaskLabel.IMM5 -> {
                                                branchOffset5 = it.value.getRawBinaryStr()
                                            }

                                            RISCVBinMapper.MaskLabel.IMM7 -> {
                                                branchOffset7 = it.value.getRawBinaryStr()
                                            }
                                            else -> {}
                                        }
                                    }

                                    else -> {}
                                }
                                it.value.toHex().getHexStr()
                            }

                            else -> {
                                ""
                            }
                        }

                   /* }"*/
                }
                when (result.type) {
                    JAL -> {
                        val shiftedImm = MutVal.Value.Binary(jalOffset20[0].toString() + jalOffset20.substring(12) + jalOffset20[11] + jalOffset20.substring(1, 11), MutVal.Size.Bit20()).getResized(MutVal.Size.Bit32()) shl 1
                        for (label in labelBinAddrMap) {
                            if (MutVal.Value.Binary(label.value) == (entry.memoryAddress.toBin() + shiftedImm).toBin()) {
                                entry.addContent(ArchConst.TranscriptHeaders.INSTRUCTION, result.type.id + " to ${label.key.wholeName}")
                            }
                        }
                    }
                    BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                        val imm12 = MutVal.Value.Binary(branchOffset7[0].toString() + branchOffset5[4] + branchOffset7.substring(1) + branchOffset5.substring(0, 4), MutVal.Size.Bit12())
                        val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                        for (label in labelBinAddrMap) {
                            if (MutVal.Value.Binary(label.value) == (entry.memoryAddress.toBin() + offset).toBin()) {
                                entry.addContent(ArchConst.TranscriptHeaders.INSTRUCTION, result.type.id + " to ${label.key.wholeName}")
                            }
                        }
                    }

                    else -> {
                        entry.addContent(ArchConst.TranscriptHeaders.INSTRUCTION, result.type.id)
                    }
                }
                entry.addContent(ArchConst.TranscriptHeaders.PARAMS, paramString)
            }

        }
        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> \n${transcriptEntrys.joinToString { "\n\t" + it.content.values.joinToString { it } }}")
        }

        transcript.setContent(transcriptEntrys)
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree): AssemblyMap {
        val rootNode = grammarTree.rootNode
        var assemblyMap: AssemblyMap? = null
        rootNode?.let {
            labelBinAddrMap.clear()
            transcriptEntrys.clear()
            binarys.clear()
            val instructionMapList = mutableMapOf<Long, RISCVGrammar.R_INSTR>()
            val dataAllocList = mutableListOf<MemAllocEntry>()
            var instrID: Long = 0
            var pcStartAddress = MutVal.Value.Hex("0", MutVal.Size.Bit32())

            var nextAddress = allocStartAddress
            // Resolving Sections

            val sections = rootNode.containers.filter { it is RISCVGrammar.C_SECTIONS }.flatMap { it.nodes.toList() }

            for (section in sections) {
                when (section) {
                    is RISCVGrammar.S_TEXT -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RISCVGrammar.R_INSTR -> {
                                    instructionMapList.set(instrID, entry)
                                    if (entry.globlStart) {
                                        pcStartAddress = (MutVal.Value.Hex(instrID.toString(16), MutVal.Size.Bit32()) * MutVal.Value.Hex("4")).toHex()
                                    }
                                    instrID += entry.instrType.memWords

                                }

                                is RISCVGrammar.R_JLBL -> {
                                    val address = (instrID * 4).toString(2)
                                    if (DebugTools.RISCV_showAsmInfo) {
                                        console.log("RISCVAssembly.generateByteCode(): found Label ${entry.label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                    }
                                    labelBinAddrMap.set(entry.label, address)
                                }
                            }
                        }
                    }

                    is RISCVGrammar.S_DATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RISCVGrammar.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RISCVGrammar.E_PARAM.Constant) {
                                        val originalValue: MutVal.Value.Hex
                                        val constToken = param.constant
                                        val isString: Boolean
                                        when (constToken) {
                                            is Compiler.Token.Constant.Ascii -> {
                                                originalValue = MutVal.Value.Hex(MutVal.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.String -> {
                                                originalValue = MutVal.Value.Hex(MutVal.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = true
                                            }

                                            is Compiler.Token.Constant.Binary -> {
                                                originalValue = MutVal.Value.Binary(constToken.content).toHex()
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.Dec -> {
                                                originalValue = MutVal.Value.Dec(constToken.content).toHex()
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.Hex -> {
                                                originalValue = MutVal.Value.Hex(constToken.content)
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.UDec -> {
                                                originalValue = MutVal.Value.UDec(constToken.content).toHex()
                                                isString = false
                                            }
                                        }

                                        val resizedValues: Array<MutVal.Value.Hex>
                                        val length: MutVal.Value.Hex
                                        when (entry.directive.type) {
                                            BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                length = MutVal.Value.Hex("1")
                                            }

                                            HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit16()))
                                                length = MutVal.Value.Hex("2")
                                            }

                                            WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit32()))
                                                length = MutVal.Value.Hex("4")
                                            }

                                            DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit64()))
                                                length = MutVal.Value.Hex("8")
                                            }

                                            ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                length = MutVal.Value.Hex("1")
                                            }

                                            STRING -> {
                                                if (isString) {
                                                    val content = constToken.content.substring(1, constToken.content.length - 1)
                                                    val valueList = mutableListOf<MutVal.Value.Hex>()
                                                    for (char in content.reversed()) {
                                                        valueList.add(MutVal.Value.Hex(char.code.toString(16), MutVal.Size.Bit8()))
                                                    }
                                                    resizedValues = valueList.toTypedArray()
                                                    length = MutVal.Value.Hex(content.length.toString(16))
                                                } else {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = MutVal.Value.Hex(originalValue.size.byteCount.toString(16))
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = MutVal.Value.Hex(originalValue.size.byteCount.toString(16))
                                            }
                                        }

                                        if (DebugTools.RISCV_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tresized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} \n\tallocating at ${nextAddress.toHex().getRawHexStr()}")
                                        }

                                        dataAllocList.add(MemAllocEntry(entry.label, nextAddress.toHex(), resizedValues.first().size, *resizedValues))
                                        nextAddress += length
                                        if (nextAddress % MutVal.Value.Binary("10") == MutVal.Value.Binary("1")) {
                                            nextAddress += MutVal.Value.Binary("1")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is RISCVGrammar.S_RODATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RISCVGrammar.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RISCVGrammar.E_PARAM.Constant) {
                                        val originalValue: MutVal.Value.Hex
                                        val constToken = param.constant
                                        val isString: Boolean
                                        when (constToken) {
                                            is Compiler.Token.Constant.Ascii -> {
                                                originalValue = MutVal.Value.Hex(MutVal.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.String -> {
                                                originalValue = MutVal.Value.Hex(MutVal.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = true
                                            }

                                            is Compiler.Token.Constant.Binary -> {
                                                originalValue = MutVal.Value.Binary(constToken.content).toHex()
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.Dec -> {
                                                originalValue = MutVal.Value.Dec(constToken.content).toHex()
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.Hex -> {
                                                originalValue = MutVal.Value.Hex(constToken.content)
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.UDec -> {
                                                originalValue = MutVal.Value.UDec(constToken.content).toHex()
                                                isString = false
                                            }
                                        }

                                        val resizedValues: Array<MutVal.Value.Hex>
                                        val length: MutVal.Value.Hex
                                        when (entry.directive.type) {
                                            BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                length = MutVal.Value.Hex("1")
                                            }

                                            HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit16()))
                                                length = MutVal.Value.Hex("2")
                                            }

                                            WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit32()))
                                                length = MutVal.Value.Hex("4")
                                            }

                                            DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit64()))
                                                length = MutVal.Value.Hex("8")
                                            }

                                            ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                length = MutVal.Value.Hex("1")
                                            }

                                            STRING -> {
                                                if (isString) {
                                                    val content = constToken.content.substring(1, constToken.content.length - 1)
                                                    val valueList = mutableListOf<MutVal.Value.Hex>()
                                                    for (char in content.reversed()) {
                                                        valueList.add(MutVal.Value.Hex(char.code.toString(16), MutVal.Size.Bit8()))
                                                    }
                                                    resizedValues = valueList.toTypedArray()
                                                    length = MutVal.Value.Hex(content.length.toString(16))
                                                } else {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = MutVal.Value.Hex(originalValue.size.byteCount.toString(16))
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = MutVal.Value.Hex(originalValue.size.byteCount.toString(16))
                                            }
                                        }

                                        if (DebugTools.RISCV_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} resized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} allocating at ${nextAddress.toHex().getRawHexStr()}")
                                        }

                                        dataAllocList.add(MemAllocEntry(entry.label, nextAddress.toHex(), resizedValues.first().size, *resizedValues))
                                        nextAddress += length
                                        if (nextAddress % MutVal.Value.Binary("10") == MutVal.Value.Binary("1")) {
                                            nextAddress += MutVal.Value.Binary("1")
                                        }
                                    }


                                }
                            }
                        }
                    }

                    is RISCVGrammar.S_BSS -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RISCVGrammar.R_ULBL -> {
                                    val dirType = entry.directive.type
                                    val originalValue: MutVal.Value.Hex = when (dirType) {
                                        BYTE -> MutVal.Value.Hex("", MutVal.Size.Bit8())
                                        HALF -> MutVal.Value.Hex("", MutVal.Size.Bit16())
                                        WORD -> MutVal.Value.Hex("", MutVal.Size.Bit32())
                                        DWORD -> MutVal.Value.Hex("", MutVal.Size.Bit64())
                                        ASCIZ -> MutVal.Value.Hex("", MutVal.Size.Bit8())
                                        STRING -> MutVal.Value.Hex("", MutVal.Size.Bit32())
                                        BYTE_2 -> MutVal.Value.Hex("", MutVal.Size.Bit16())
                                        BYTE_4 -> MutVal.Value.Hex("", MutVal.Size.Bit32())
                                        BYTE_8 -> MutVal.Value.Hex("", MutVal.Size.Bit64())
                                        else -> {
                                            MutVal.Value.Hex("", MutVal.Size.Bit32())
                                        }
                                    }

                                    if (DebugTools.RISCV_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tallocating at ${nextAddress.toHex().getRawHexStr()}")
                                    }

                                    dataAllocList.add(MemAllocEntry(entry.label, nextAddress.toHex(), originalValue.size, originalValue))
                                    nextAddress += MutVal.Value.Dec(originalValue.size.byteCount.toString())
                                    if (nextAddress % MutVal.Value.Binary("10") == MutVal.Value.Binary("1")) {
                                        nextAddress += MutVal.Value.Binary("1")
                                    }
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            val memory = architecture.getMemory()

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataAllocList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                for (valueID in alloc.values.indices) {
                    val value = alloc.values[valueID]
                    val address = alloc.address + MutVal.Value.Hex(valueID.toString(16)) * MutVal.Value.Hex(alloc.sizeOfOne.byteCount.toString(16))
                    if (DebugTools.RISCV_showAsmInfo) {
                        console.log("Assembly.generateByteCode(): ASM-STORE DATA ${value.toHex().getRawHexStr()} at ${address.toHex().getRawHexStr()}")
                    }
                    memory.save(address, value, StyleConst.CLASS_TABLE_MARK_DATA)
                }
            }

            // Getting binary and store binary in memory
            val instrIDMap = mutableMapOf<String, Int>()
            binaryMapper.setLabelLinks(labelBinAddrMap)
            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value, MutVal.Value.Hex((binarys.size * 4).toString(16), MutVal.Size.Bit32()))
                if (DebugTools.RISCV_showAsmInfo) {
                    console.log(
                        "Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.instrname.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.instrType.id},  \n\t${
                            instr.value.paramcoll?.paramsWithOutSplitSymbols?.joinToString(",") {
                                it.getAllTokens().joinToString("") { it.content }
                            }
                        } to ${binary.joinToString { it.getRawBinaryStr() }}]"
                    )
                }
                for (wordID in binary.indices) {
                    instrIDMap.set(MutVal.Value.Hex(((binarys.size + wordID) * 4).toString(16), MutVal.Size.Bit32()).getRawHexStr(), instr.value.getAllTokens().first().lineLoc.lineID)
                }

                binarys.addAll(binary)
            }

            for (binaryID in binarys.indices) {

                val binary = binarys[binaryID]
                if (DebugTools.RISCV_showAsmInfo) {
                    console.log("Assembly.generateByteCode(): ASM-STORE ${binaryID} saving...")
                }
                val address = MutVal.Value.Hex((binaryID * 4).toString(16), MutVal.Size.Bit32())
                transcriptEntrys.add(Transcript.TranscriptEntry(address))
                memory.save(address, binary, StyleConst.CLASS_TABLE_MARK_PROGRAM)
            }

            architecture.getRegisterContainer().pc.value.set(pcStartAddress)

            assemblyMap = AssemblyMap(instrIDMap)
        }

        return assemblyMap ?: AssemblyMap()
    }

    class MemAllocEntry(val label: RISCVGrammar.E_LABEL, val address: MutVal.Value.Hex, val sizeOfOne: MutVal.Size, vararg val values: MutVal.Value)

}