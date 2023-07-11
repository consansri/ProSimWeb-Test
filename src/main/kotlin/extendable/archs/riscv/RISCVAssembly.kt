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
import extendable.components.types.ByteValue
import tools.DebugTools

class RISCVAssembly(val binaryMapper: RISCVBinMapper, val allocStartAddress: ByteValue.Type) : Assembly() {

    val labelBinAddrMap = mutableMapOf<RISCVGrammar.T1Label, String>()
    val transcriptEntrys = mutableListOf<Transcript.TranscriptEntry>()
    val binarys = mutableListOf<ByteValue.Type.Binary>()

    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val transcript = architecture.getTranscript()
        if(DebugTools.RISCV_showAsmInfo){
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.values.joinToString { it }}")
        }

        for (entryID in transcriptEntrys.indices) {
            val entry = transcriptEntrys[entryID]
            val binary = architecture.getMemory().load(entry.memoryAddress, 4).get().toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (ByteValue.Type.Binary(labels.value) == entry.memoryAddress.toBin()) {
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
                                        if (ByteValue.Type.Binary(labels.value) == it.value) {
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
        if(DebugTools.RISCV_showAsmInfo){
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> ${transcriptEntrys.joinToString { it.content.values.joinToString { it } }}")
        }

        transcript.setContent(transcriptEntrys)
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree, startAtLine: Int): ReservationMap {
        val rootNode = grammarTree.rootNode
        var reservationMap: ReservationMap? = null
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
                                        labelBinAddrMap.set(entry.t1Label, (instrID * 4).toString(2))
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
                                            val originalValue: ByteValue.Type.Hex
                                            val constToken = param.constant
                                            val isAsciiString: Boolean
                                            when (constToken) {
                                                is Compiler.Token.Constant.Ascii -> {
                                                    originalValue = ByteValue.Type.Hex(ByteValue.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                    isAsciiString = true
                                                }

                                                is Compiler.Token.Constant.Binary -> {
                                                    originalValue = ByteValue.Type.Binary(constToken.content).toHex()
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.Dec -> {
                                                    originalValue = ByteValue.Type.Dec(constToken.content).toHex()
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.Hex -> {
                                                    originalValue = ByteValue.Type.Hex(constToken.content)
                                                    isAsciiString = false
                                                }

                                                is Compiler.Token.Constant.UDec -> {
                                                    originalValue = ByteValue.Type.UDec(constToken.content).toHex()
                                                    isAsciiString = false
                                                }
                                            }

                                            val resizedValues: Array<ByteValue.Type.Hex>
                                            val length: ByteValue.Type.Hex
                                            when (entry.t1Directive?.type) {
                                                byte -> {
                                                    resizedValues = arrayOf(originalValue.getResized(ByteValue.Size.Bit8()))
                                                    length = ByteValue.Type.Hex("1")
                                                }

                                                half -> {
                                                    resizedValues = arrayOf(originalValue.getResized(ByteValue.Size.Bit16()))
                                                    length = ByteValue.Type.Hex("2")
                                                }

                                                word -> {
                                                    resizedValues = arrayOf(originalValue.getResized(ByteValue.Size.Bit32()))
                                                    length = ByteValue.Type.Hex("4")
                                                }

                                                dword -> {
                                                    resizedValues = arrayOf(originalValue.getResized(ByteValue.Size.Bit64()))
                                                    length = ByteValue.Type.Hex("8")
                                                }

                                                asciz -> {
                                                    resizedValues = arrayOf(originalValue.getResized(ByteValue.Size.Bit8()))
                                                    length = ByteValue.Type.Hex("1")
                                                }

                                                string -> {
                                                    if (isAsciiString) {
                                                        val content = constToken.content.substring(1, constToken.content.length - 1)
                                                        val valueList = mutableListOf<ByteValue.Type.Hex>()
                                                        for (char in content) {
                                                            valueList.add(ByteValue.Type.Hex(char.code.toString(16), ByteValue.Size.Bit8()))
                                                        }
                                                        resizedValues = valueList.toTypedArray()
                                                        length = ByteValue.Type.Hex(content.length.toString(16))
                                                    } else {
                                                        resizedValues = arrayOf(originalValue)
                                                        length = ByteValue.Type.Hex(originalValue.size.byteCount.toString(16))
                                                    }

                                                }

                                                else -> {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = ByteValue.Type.Hex(originalValue.size.byteCount.toString(16))
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
                    val address = alloc.address + ByteValue.Type.Hex(valueID.toString(16)) * ByteValue.Type.Hex(alloc.sizeOfOne.byteCount.toString(16))
                    if (DebugTools.RISCV_showAsmInfo) {
                        console.log("Assembly.generateByteCode(): ASM-STORE DATA ${value.toHex().getRawHexStr()} at ${address.toHex().getRawHexStr()}")
                    }
                    memory.save(address, value, StyleConst.CLASS_TABLE_MARK_DATA)
                }
            }

            // Getting binary and store binary in memory
            binaryMapper.setLabelLinks(labelBinAddrMap)
            var nextInstrAfterStartAtLine: RISCVGrammar.T1Instr? = null
            var startAtLineBinaryID = 0
            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value)
                if (DebugTools.RISCV_showAsmInfo) {
                    console.log(
                        "Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.t1Instr.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.type.id},  ${
                            instr.value.t1ParamColl?.paramsWithOutSplitSymbols?.joinToString(",") {
                                it.getAllTokens().joinToString("") { it.content }
                            }
                        } to ${binary.joinToString { it.getRawBinaryStr() }}]"
                    )
                }
                val t1InstrToCheckLineStart = instr.value.t1Instr
                if (t1InstrToCheckLineStart.insToken.lineLoc.lineID >= startAtLine) {
                    if (nextInstrAfterStartAtLine != null) {
                        if (nextInstrAfterStartAtLine.insToken.lineLoc.lineID > t1InstrToCheckLineStart.insToken.lineLoc.lineID) {
                            nextInstrAfterStartAtLine = instr.value.t1Instr
                        }
                    } else {
                        nextInstrAfterStartAtLine = instr.value.t1Instr
                    }
                }
                binarys.addAll(binary)
                if(nextInstrAfterStartAtLine == t1InstrToCheckLineStart){
                    startAtLineBinaryID = binarys.indexOf(binary.first())
                }
            }

            for (binaryID in binarys.indices) {
                val binary = binarys[binaryID]
                if (DebugTools.RISCV_showAsmInfo) {
                    console.log("Assembly.generateByteCode(): ASM-STORE ${binaryID} saving...")
                }
                val address = ByteValue.Type.Hex((binaryID * 4).toString(16), ByteValue.Size.Bit32())

                transcriptEntrys.add(Transcript.TranscriptEntry(address))
                memory.save(address, binary, StyleConst.CLASS_TABLE_MARK_PROGRAM)
            }

            reservationMap = ReservationMap(ByteValue.Type.Hex((startAtLineBinaryID * 4).toString(16), ByteValue.Size.Bit32()))
        }

        return reservationMap ?: ReservationMap(ByteValue.Type.Hex("0",ByteValue.Size.Bit32()))
    }

    class MemAllocEntry(val labelDef: RISCVGrammar.T2LabelDef, val address: ByteValue.Type.Hex, val sizeOfOne: ByteValue.Size, vararg val values: ByteValue.Type)

}