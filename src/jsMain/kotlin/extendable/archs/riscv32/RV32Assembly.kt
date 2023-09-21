package extendable.archs.riscv32

import StyleAttr
import extendable.Architecture
import extendable.archs.riscv32.RV32Grammar.E_DIRECTIVE.DirType.*
import extendable.archs.riscv32.RV32Grammar.R_INSTR.InstrType.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.Transcript
import extendable.components.types.Variable
import tools.DebugTools

class RV32Assembly(val binaryMapper: RV32BinMapper, val dataSecStart: Variable.Value, val rodataSecStart: Variable.Value, val bssSecStart: Variable.Value) : Assembly() {

    val labelBinAddrMap = mutableMapOf<RV32Grammar.E_LABEL, String>()
    val transcriptEntrys = mutableListOf<RVDisassembledRow>()
    val bins = mutableListOf<Variable.Value.Bin>()

    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val transcript = architecture.getTranscript()
        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.entries.joinToString("") { "\n\t${it.key.wholeName}: ${it.value}" }}")
        }

        for (rowID in transcriptEntrys.indices) {
            val row = transcriptEntrys[rowID]
            val binary = architecture.getMemory().load(row.getAddresses().first(), 4).get().toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (Variable.Value.Bin(labels.value) == row.getAddresses().first().toBin()) {
                    labelString += "${labels.key.wholeName} "
                }
            }
            row.addLabel(labelString)

            val result = binaryMapper.getInstrFromBinary(binary)
            if (result != null) {
                var branchOffset5: String = ""
                var branchOffset7: String = ""
                var jalOffset20: String = ""
                result.binMap.entries.forEach {
                    /*"${it.key.name.lowercase()}\t${*/
                    when (it.key) {
                        RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.IMM20 -> {
                            when (result.type) {
                                JAL -> {
                                    jalOffset20 = it.value.getRawBinaryStr()
                                }

                                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                                    when (it.key) {
                                        RV32BinMapper.MaskLabel.IMM5 -> {
                                            branchOffset5 = it.value.getRawBinaryStr()
                                        }

                                        RV32BinMapper.MaskLabel.IMM7 -> {
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
                var labelstring = ""
                when (result.type) {
                    JAL -> {
                        val shiftedImm = Variable.Value.Bin(jalOffset20[0].toString() + jalOffset20.substring(12) + jalOffset20[11] + jalOffset20.substring(1, 11), Variable.Size.Bit20()).getResized(Variable.Size.Bit32()) shl 1
                        for (label in labelBinAddrMap) {
                            if (Variable.Value.Bin(label.value) == (row.getAddresses().first().toBin() + shiftedImm).toBin()) {
                                labelstring = label.key.wholeName
                            }
                        }
                    }

                    BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                        val imm12 = Variable.Value.Bin(branchOffset7[0].toString() + branchOffset5[4] + branchOffset7.substring(1) + branchOffset5.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(Variable.Size.Bit32()) shl 1
                        for (label in labelBinAddrMap) {
                            if (Variable.Value.Bin(label.value) == (row.getAddresses().first().toBin() + offset).toBin()) {
                                labelstring = label.key.wholeName
                            }
                        }
                    }

                    else -> {}
                }
                row.addInstr(architecture, result, labelstring)
            }
        }

        if (DebugTools.RISCV_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> \n${transcriptEntrys.joinToString { "\n\t" + it.getContent().joinToString("\t") { it.content } }}")
        }

        transcript.addContent(Transcript.Type.COMPILED, emptyList())
        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, transcriptEntrys)
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree): AssemblyMap {
        val rootNode = grammarTree.rootNode
        var assemblyMap: AssemblyMap? = null
        rootNode?.let {
            labelBinAddrMap.clear()
            transcriptEntrys.clear()
            bins.clear()
            val instructionMapList = mutableMapOf<Long, RV32Grammar.R_INSTR>()
            val dataList = mutableListOf<DataEntry>()
            val rodataList = mutableListOf<DataEntry>()
            val bssList = mutableListOf<DataEntry>()
            var instrID: Long = 0
            var pcStartAddress = Variable.Value.Hex("0", Variable.Size.Bit32())

            var nextDataAddress = dataSecStart
            var nextRoDataAddress = rodataSecStart
            var nextBssAddress = bssSecStart
            // Resolving Sections

            val sections = rootNode.containers.filter { it is RV32Grammar.C_SECTIONS }.flatMap { it.nodes.toList() }

            for (section in sections) {
                when (section) {
                    is RV32Grammar.S_TEXT -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RV32Grammar.R_INSTR -> {
                                    instructionMapList.set(instrID, entry)
                                    instrID += entry.instrType.memWords
                                }

                                is RV32Grammar.R_JLBL -> {
                                    val address = (instrID * 4).toString(2)
                                    if (DebugTools.RISCV_showAsmInfo) {
                                        console.log("RISCVAssembly.generateByteCode(): found Label ${entry.label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                    }
                                    if (entry.isGlobalStart) {
                                        pcStartAddress = Variable.Value.Bin(address, Variable.Size.Bit32()).toHex()
                                    }
                                    labelBinAddrMap.set(entry.label, address)
                                }
                            }
                        }
                    }

                    is RV32Grammar.S_DATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Grammar.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RV32Grammar.E_PARAM.Constant) {
                                        val originalValue: Variable.Value.Hex
                                        val constToken = param.constant
                                        val isString: Boolean
                                        when (constToken) {
                                            is Compiler.Token.Constant.Ascii -> {
                                                originalValue = Variable.Value.Hex(Variable.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.String -> {
                                                originalValue = Variable.Value.Hex(Variable.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = true
                                            }

                                            else -> {
                                                originalValue = constToken.getValue().toHex()
                                                isString = false
                                            }
                                        }

                                        val resizedValues: Array<Variable.Value.Hex>
                                        val length: Variable.Value.Hex
                                        when (entry.directive.type) {
                                            BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1")
                                            }

                                            HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit16()))
                                                length = Variable.Value.Hex("2")
                                            }

                                            WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit32()))
                                                length = Variable.Value.Hex("4")
                                            }

                                            DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit64()))
                                                length = Variable.Value.Hex("8")
                                            }

                                            ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1")
                                            }

                                            STRING -> {
                                                if (isString) {
                                                    val content = constToken.content.substring(1, constToken.content.length - 1)
                                                    val valueList = mutableListOf<Variable.Value.Hex>()
                                                    for (char in content) {
                                                        valueList.add(Variable.Value.Hex(char.code.toString(16), Variable.Size.Bit8()))
                                                    }
                                                    resizedValues = valueList.toTypedArray()
                                                    length = Variable.Value.Hex(content.length.toString(16))
                                                } else {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                            }
                                        }

                                        if (DebugTools.RISCV_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tresized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} \n\tallocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                        }

                                        val sizeOfOne = Variable.Value.Hex(resizedValues.first().size.byteCount.toString(16))
                                        val rest = nextDataAddress % sizeOfOne
                                        if (rest != Variable.Value.Bin("0")) {
                                            nextDataAddress += sizeOfOne - rest
                                        }
                                        val dataEntry = DataEntry(entry.label, nextDataAddress.toHex(), resizedValues.first().size, *resizedValues)
                                        dataList.add(dataEntry)
                                        nextDataAddress += length
                                    }
                                }
                            }
                        }
                    }

                    is RV32Grammar.S_RODATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Grammar.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RV32Grammar.E_PARAM.Constant) {
                                        val originalValue: Variable.Value.Hex
                                        val constToken = param.constant
                                        val isString: Boolean
                                        when (constToken) {
                                            is Compiler.Token.Constant.Ascii -> {
                                                originalValue = Variable.Value.Hex(Variable.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = false
                                            }

                                            is Compiler.Token.Constant.String -> {
                                                originalValue = Variable.Value.Hex(Variable.Tools.asciiToHex(constToken.content.substring(1, constToken.content.length - 1)))
                                                isString = true
                                            }

                                            else -> {
                                                originalValue = constToken.getValue().toHex()
                                                isString = false
                                            }
                                        }

                                        val resizedValues: Array<Variable.Value.Hex>
                                        val length: Variable.Value.Hex
                                        when (entry.directive.type) {
                                            BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1")
                                            }

                                            HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit16()))
                                                length = Variable.Value.Hex("2")
                                            }

                                            WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit32()))
                                                length = Variable.Value.Hex("4")
                                            }

                                            DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit64()))
                                                length = Variable.Value.Hex("8")
                                            }

                                            ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1")
                                            }

                                            STRING -> {
                                                if (isString) {
                                                    val content = constToken.content.substring(1, constToken.content.length - 1)
                                                    val valueList = mutableListOf<Variable.Value.Hex>()
                                                    for (char in content) {
                                                        valueList.add(Variable.Value.Hex(char.code.toString(16), Variable.Size.Bit8()))
                                                    }
                                                    resizedValues = valueList.toTypedArray()
                                                    length = Variable.Value.Hex(content.length.toString(16))
                                                } else {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                            }
                                        }

                                        if (DebugTools.RISCV_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} resized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} allocating at ${nextRoDataAddress.toHex().getRawHexStr()}")
                                        }

                                        val sizeOfOne = Variable.Value.Hex(resizedValues.first().size.byteCount.toString(16))
                                        val rest = nextRoDataAddress % sizeOfOne
                                        if (rest != Variable.Value.Bin("0")) {
                                            nextRoDataAddress += sizeOfOne - rest
                                        }
                                        val dataEntry = DataEntry(entry.label, nextRoDataAddress.toHex(), resizedValues.first().size, *resizedValues)
                                        rodataList.add(dataEntry)
                                        nextRoDataAddress += length
                                    }
                                }
                            }
                        }
                    }

                    is RV32Grammar.S_BSS -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Grammar.R_ULBL -> {
                                    val dirType = entry.directive.type
                                    val originalValue: Variable.Value.Hex = when (dirType) {
                                        BYTE -> Variable.Value.Hex("", Variable.Size.Bit8())
                                        HALF -> Variable.Value.Hex("", Variable.Size.Bit16())
                                        WORD -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        DWORD -> Variable.Value.Hex("", Variable.Size.Bit64())
                                        ASCIZ -> Variable.Value.Hex("", Variable.Size.Bit8())
                                        STRING -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        BYTE_2 -> Variable.Value.Hex("", Variable.Size.Bit16())
                                        BYTE_4 -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        BYTE_8 -> Variable.Value.Hex("", Variable.Size.Bit64())
                                        else -> {
                                            Variable.Value.Hex("", Variable.Size.Bit32())
                                        }
                                    }

                                    if (DebugTools.RISCV_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tallocating at ${nextBssAddress.toHex().getRawHexStr()}")
                                    }

                                    val sizeOfOne = Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                    val rest = nextBssAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0")) {
                                        nextBssAddress += sizeOfOne - rest
                                    }
                                    dataList.add(DataEntry(entry.label, nextBssAddress.toHex(), originalValue.size, originalValue))
                                    nextBssAddress += Variable.Value.Hex(originalValue.size.byteCount.toString(16))
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            val memory = architecture.getMemory()

            // adding bss addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in bssList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                memory.saveArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // adding rodata addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in rodataList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                memory.saveArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA, true)
            }

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                memory.saveArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // Getting binary and store binary in memory
            val instrIDMap = mutableMapOf<String, AssemblyMap.MapEntry>()
            binaryMapper.setLabelLinks(labelBinAddrMap)
            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value, Variable.Value.Hex((bins.size * 4).toString(16), Variable.Size.Bit32()))
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
                    instrIDMap.set(Variable.Value.Hex(((bins.size + wordID) * 4).toString(16), Variable.Size.Bit32()).getRawHexStr(), AssemblyMap.MapEntry(instr.value.getAllTokens().first().lineLoc.file, instr.value.getAllTokens().first().lineLoc.lineID))
                }
                bins.addAll(binary)
            }

            var address = Variable.Value.Hex("0", Variable.Size.Bit32())
            for (binaryID in bins.indices) {
                val binary = bins[binaryID]
                if (DebugTools.RISCV_showAsmInfo) {
                    console.log("Assembly.generateByteCode(): ASM-STORE ${binaryID} saving...")
                }
                address = Variable.Value.Hex((binaryID * 4).toString(16), Variable.Size.Bit32())
                transcriptEntrys.add(RVDisassembledRow(address))
                memory.save(address, binary, StyleAttr.Main.Table.Mark.PROGRAM)
            }
            transcriptEntrys.add(RVDisassembledRow((address + Variable.Value.Hex("4")).toHex()))
            architecture.getRegisterContainer().pc.value.set(pcStartAddress)
            assemblyMap = AssemblyMap(instrIDMap)
        }

        return assemblyMap ?: AssemblyMap()
    }

    class DataEntry(val label: RV32Grammar.E_LABEL, val address: Variable.Value.Hex, val sizeOfOne: Variable.Size, vararg val values: Variable.Value)

    class RVDisassembledRow(address: Variable.Value.Hex) : Transcript.Row(address) {

        val content = RV32.TS_DISASSEMBLED_HEADERS.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[RV32.TS_DISASSEMBLED_HEADERS.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(architecture: Architecture, instrResult: RV32BinMapper.InstrResult, labelName: String) {
            val instrName = instrResult.type.id
            content[RV32.TS_DISASSEMBLED_HEADERS.Instruction] = Entry(Orientation.LEFT, instrName)
            content[RV32.TS_DISASSEMBLED_HEADERS.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture.getRegisterContainer(), instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(labelName: String) {
            content[RV32.TS_DISASSEMBLED_HEADERS.Label] = Entry(Orientation.LEFT, labelName)
        }

        override fun getContent(): List<Entry> {
            return RV32.TS_DISASSEMBLED_HEADERS.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }


}