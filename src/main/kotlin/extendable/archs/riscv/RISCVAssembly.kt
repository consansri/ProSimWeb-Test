package extendable.archs.riscv

import StyleConst
import extendable.ArchConst
import extendable.Architecture
import extendable.archs.riscv.RISCVGrammar.T1Directive.Type.*
import extendable.archs.riscv.RISCVGrammar.T2LabelDef.Type.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.Transcript
import extendable.components.types.MutVal
import tools.DebugTools

class RISCVAssembly(val binaryMapper: RISCVBinMapper, val allocStartAddress: MutVal.Value) : Assembly() {

    val labelBinAddrMap = mutableMapOf<RISCVGrammar.T1Label, String>()
    val transcriptEntrys = mutableListOf<Transcript.TranscriptEntry>()
    val binarys = mutableListOf<MutVal.Value.Binary>()

    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val transcript = architecture.getTranscript()
        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.values.joinToString { it }}")
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
                entry.addContent(ArchConst.TranscriptHeaders.INSTRUCTION, result.type.id)
                var paramString = result.binaryMap.entries.joinToString(", ") {
                    "${it.key.name.lowercase()}: ${
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
                                if (result.type == RISCVGrammar.T1Instr.Type.JALR || result.type == RISCVGrammar.T1Instr.Type.JAL) {
                                    var labelString = ""
                                    for (labels in labelBinAddrMap) {
                                        if (MutVal.Value.Binary(labels.value) == it.value) {
                                            labelString = labels.key.wholeName
                                        }
                                    }
                                    if (labelString.isNotEmpty()) {
                                        "${it.value.toHex().getHexStr()} -> $labelString"
                                    } else {
                                        it.value.toHex().getHexStr()
                                    }

                                } else {
                                    it.value.toHex().getHexStr()
                                }
                            }

                            else -> {
                                ""
                            }
                        }

                    }"
                }
                entry.addContent(ArchConst.TranscriptHeaders.PARAMS, paramString)
            }

        }
        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> ${transcriptEntrys.joinToString { it.content.values.joinToString { it } }}")
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
            val instructionMapList = mutableMapOf<Long, RISCVGrammar.T2InstrDef>()
            val dataAllocList = mutableListOf<MemAllocEntry>()
            var instrID: Long = 0

            var nextAddress = allocStartAddress
            // Resolving Sections
            for (section in rootNode.sections) {
                when (section) {
                    is RISCVGrammar.T3TextSection -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RISCVGrammar.T2InstrDef -> {
                                    instructionMapList.set(instrID, entry)
                                    instrID += entry.type.memWords
                                }

                                is RISCVGrammar.T2LabelDef -> {
                                    if (entry.type == JUMP) {
                                        val address = (instrID * 4).toString(2)
                                        if (DebugTools.RISCV_showAsmInfo) {
                                            console.log("RISCVAssembly.generateByteCode(): found Label ${entry.t1Label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                        }
                                        labelBinAddrMap.set(entry.t1Label, address)

                                    }
                                }
                            }
                        }
                    }

                    is RISCVGrammar.T3DataSection -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RISCVGrammar.T2LabelDef -> {
                                    if (entry.type == MEMALLOC) {
                                        val param = entry.t1Param?.paramsWithOutSplitSymbols?.first()
                                        if (param is RISCVGrammar.T1Param.Constant) {
                                            val originalValue: MutVal.Value.Hex
                                            val constToken = param.constant
                                            val isAsciiString: Boolean
                                            when (constToken) {
                                                is Compiler.Token.Constant.Ascii -> {
                                                    originalValue = MutVal.Value.Hex(MutVal.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                    isAsciiString = true
                                                }

                                                is Compiler.Token.Constant.Binary -> {
                                                    originalValue = MutVal.Value.Binary(constToken.content).toHex()
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.Dec -> {
                                                    originalValue = MutVal.Value.Dec(constToken.content).toHex()
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.Hex -> {
                                                    originalValue = MutVal.Value.Hex(constToken.content)
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.UDec -> {
                                                    originalValue = MutVal.Value.UDec(constToken.content).toHex()
                                                    isAsciiString = false
                                                }
                                            }

                                            val resizedValues: Array<MutVal.Value.Hex>
                                            val length: MutVal.Value.Hex
                                            when (entry.t1Directive?.type) {
                                                byte -> {
                                                    resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                    length = MutVal.Value.Hex("1")
                                                }

                                                half -> {
                                                    resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit16()))
                                                    length = MutVal.Value.Hex("2")
                                                }

                                                word -> {
                                                    resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit32()))
                                                    length = MutVal.Value.Hex("4")
                                                }

                                                dword -> {
                                                    resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit64()))
                                                    length = MutVal.Value.Hex("8")
                                                }

                                                asciz -> {
                                                    resizedValues = arrayOf(originalValue.getUResized(MutVal.Size.Bit8()))
                                                    length = MutVal.Value.Hex("1")
                                                }

                                                string -> {
                                                    if (isAsciiString) {
                                                        val content = constToken.content.substring(1, constToken.content.length - 1)
                                                        val valueList = mutableListOf<MutVal.Value.Hex>()
                                                        for (char in content) {
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

                                            dataAllocList.add(MemAllocEntry(entry, nextAddress.toHex(), resizedValues.first().size, *resizedValues))
                                            nextAddress += length
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }

            val memory = architecture.getMemory()

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataAllocList) {
                labelBinAddrMap.set(alloc.labelDef.t1Label, alloc.address.toBin().getRawBinaryStr())
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
                        "Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.t1Instr.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.type.id},  ${
                            instr.value.t1ParamColl?.paramsWithOutSplitSymbols?.joinToString(",") {
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

            assemblyMap = AssemblyMap(instrIDMap)
        }

        return assemblyMap ?: AssemblyMap()
    }

    class MemAllocEntry(val labelDef: RISCVGrammar.T2LabelDef, val address: MutVal.Value.Hex, val sizeOfOne: MutVal.Size, vararg val values: MutVal.Value)

}