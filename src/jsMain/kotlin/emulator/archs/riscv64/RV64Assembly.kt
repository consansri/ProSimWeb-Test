package emulator.archs.riscv64

import debug.DebugTools
import emulator.archs.riscv64.RV64
import emulator.archs.riscv64.RV64BinMapper
import emulator.archs.riscv64.RV64Syntax
import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import kotlin.time.measureTime

class RV64Assembly(val binaryMapper: RV64BinMapper, val dataSecStart: Variable.Value, val rodataSecStart: Variable.Value, val bssSecStart: Variable.Value) : Assembly() {
    val labelBinAddrMap = mutableMapOf<RV64Syntax.E_LABEL, String>()
    val transcriptEntrys = mutableListOf<RVDisassembledRow>()
    val bins = mutableListOf<Variable.Value.Bin>()

    /**
     * Disassembles the content of the memory and builds the [RVDisassembledRow]'s from it which are then added to the disassembled transcript view.
     */
    override fun generateTranscript(architecture: Architecture, syntaxTree: Syntax.SyntaxTree) {
        val transcript = architecture.getTranscript()
        if (DebugTools.RV64_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.entries.joinToString("") { "\n\t${it.key.wholeName}: ${it.value}" }}")
        }

        for (rowID in transcriptEntrys.indices) {
            val row = transcriptEntrys[rowID]
            val binary = architecture.getMemory().load(row.getAddresses().first(), 4).toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (Variable.Value.Bin(labels.value, Variable.Size.Bit32()) == row.getAddresses().first().toBin()) {
                    labelString += "${labels.key.wholeName} "
                }
            }
            row.addLabel(labelString)

            val result = binaryMapper.getInstrFromBinary(binary)
            if (result != null) {
                var branchOffset5: String = "0"
                var branchOffset7: String = "0"
                var jalOffset20: String = "0"

                result.binMap.entries.forEach {
                    when (it.key) {
                        RV64BinMapper.MaskLabel.IMM5, RV64BinMapper.MaskLabel.IMM7, RV64BinMapper.MaskLabel.IMM12, RV64BinMapper.MaskLabel.IMM20 -> {
                            when (result.type) {
                                RV64Syntax.R_INSTR.InstrType.JAL -> {
                                    jalOffset20 = it.value.getRawBinaryStr()
                                }

                                RV64Syntax.R_INSTR.InstrType.BEQ, RV64Syntax.R_INSTR.InstrType.BNE, RV64Syntax.R_INSTR.InstrType.BLT, RV64Syntax.R_INSTR.InstrType.BGE, RV64Syntax.R_INSTR.InstrType.BLTU, RV64Syntax.R_INSTR.InstrType.BGEU -> {
                                    when (it.key) {
                                        RV64BinMapper.MaskLabel.IMM5 -> {
                                            branchOffset5 = it.value.getRawBinaryStr()
                                        }

                                        RV64BinMapper.MaskLabel.IMM7 -> {
                                            branchOffset7 = it.value.getRawBinaryStr()
                                        }

                                        else -> {}
                                    }
                                }

                                else -> {}
                            }
                            it.value.toHex().getHexStr()
                        }

                        else -> {}
                    }
                }
                var labelstring = ""
                when (result.type) {
                    RV64Syntax.R_INSTR.InstrType.JAL -> {
                        val shiftedImm = Variable.Value.Bin(jalOffset20[0].toString() + jalOffset20.substring(12) + jalOffset20[11] + jalOffset20.substring(1, 11), Variable.Size.Bit20()).getResized(Variable.Size.Bit32()) shl 1
                        for (label in labelBinAddrMap) {
                            if (Variable.Value.Bin(label.value) == (row.getAddresses().first().toBin() + shiftedImm).toBin()) {
                                labelstring = label.key.wholeName
                            }
                        }
                    }

                    RV64Syntax.R_INSTR.InstrType.BEQ, RV64Syntax.R_INSTR.InstrType.BNE, RV64Syntax.R_INSTR.InstrType.BLT, RV64Syntax.R_INSTR.InstrType.BGE, RV64Syntax.R_INSTR.InstrType.BLTU, RV64Syntax.R_INSTR.InstrType.BGEU -> {
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

        if (DebugTools.RV64_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> \n${transcriptEntrys.joinToString { "\n\t" + it.getContent().joinToString("\t") { it.content } }}")
        }

        transcript.addContent(Transcript.Type.COMPILED, emptyList())
        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, transcriptEntrys)
    }

    /**
     * Extracts all relevant information from the [syntaxTree] and stores it at certain points in memory.
     */
    override fun generateByteCode(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        val rootNode = syntaxTree.rootNode
        var assemblyMap: AssemblyMap? = null
        rootNode?.let {
            labelBinAddrMap.clear()
            transcriptEntrys.clear()
            bins.clear()
            val instructionMapList = mutableMapOf<Long, RV64Syntax.R_INSTR>()
            val dataList = mutableListOf<DataEntry>()
            val rodataList = mutableListOf<DataEntry>()
            val bssList = mutableListOf<DataEntry>()
            var instrID: Long = 0
            var pcStartAddress = Variable.Value.Hex("0", Variable.Size.Bit32())

            var nextDataAddress = dataSecStart
            var nextRoDataAddress = rodataSecStart
            var nextBssAddress = bssSecStart
            // Resolving Sections

            val sections = rootNode.containers.filter { it is RV64Syntax.C_SECTIONS }.flatMap { it.nodes.toList() }

            for (section in sections) {
                when (section) {
                    is RV64Syntax.S_TEXT -> {
                        for (entry in section.collNodes) {
                            console.log("assembling section: ${section.name}, row: ${entry.name}")
                            when (entry) {
                                is RV64Syntax.R_INSTR -> {
                                    instructionMapList.set(instrID, entry)
                                    instrID += entry.instrType.memWords
                                }

                                is RV64Syntax.R_JLBL -> {
                                    val address = (instrID * 4).toString(2)
                                    if (DebugTools.RV64_showAsmInfo) {
                                        console.log("RISCVAssembly.generateByteCode(): found Label ${entry.label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                    }
                                    if (entry.isGlobalStart) {
                                        pcStartAddress = Variable.Value.Bin(address, Variable.Size.Bit32()).toHex()
                                    }
                                    labelBinAddrMap.set(entry.label, address)
                                }
                                else -> {
                                    console.log("not found")
                                }
                            }
                        }
                    }

                    is RV64Syntax.S_DATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV64Syntax.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RV64Syntax.E_PARAM.Constant) {
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
                                            RV64Syntax.E_DIRECTIVE.DirType.BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit16()))
                                                length = Variable.Value.Hex("2", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit32()))
                                                length = Variable.Value.Hex("4", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit64()))
                                                length = Variable.Value.Hex("8", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.STRING -> {
                                                if (isString) {
                                                    val content = constToken.content.substring(1, constToken.content.length - 1)
                                                    val valueList = mutableListOf<Variable.Value.Hex>()
                                                    for (char in content) {
                                                        valueList.add(Variable.Value.Hex(char.code.toString(16), Variable.Size.Bit8()))
                                                    }
                                                    resizedValues = valueList.toTypedArray()
                                                    length = Variable.Value.Hex(content.length.toString(16), Variable.Size.Bit32())
                                                } else {
                                                    resizedValues = arrayOf(originalValue)
                                                    length = Variable.Value.Hex((originalValue.size.getByteCount()).toString(16), Variable.Size.Bit32())
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
                                            }
                                        }

                                        if (DebugTools.RV64_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tresized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} \n\tallocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                        }

                                        val sizeOfOne = Variable.Value.Hex((resizedValues.first().size.getByteCount()).toString(16), Variable.Size.Bit8())
                                        val rest = nextDataAddress % sizeOfOne
                                        if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
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

                    is RV64Syntax.S_RODATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV64Syntax.R_ILBL -> {
                                    val param = entry.paramcoll.paramsWithOutSplitSymbols.first()
                                    if (param is RV64Syntax.E_PARAM.Constant) {
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
                                            RV64Syntax.E_DIRECTIVE.DirType.BYTE -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.HALF -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit16()))
                                                length = Variable.Value.Hex("2", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.WORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit32()))
                                                length = Variable.Value.Hex("4", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.DWORD -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit64()))
                                                length = Variable.Value.Hex("8", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.ASCIZ -> {
                                                resizedValues = arrayOf(originalValue.getUResized(Variable.Size.Bit8()))
                                                length = Variable.Value.Hex("1", Variable.Size.Bit8())
                                            }

                                            RV64Syntax.E_DIRECTIVE.DirType.STRING -> {
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
                                                    length = Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
                                                }
                                            }

                                            else -> {
                                                resizedValues = arrayOf(originalValue)
                                                length = Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
                                            }
                                        }

                                        if (DebugTools.RV64_showAsmInfo) {
                                            console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} resized to ${resizedValues.joinToString { it.toHex().getRawHexStr() }} allocating at ${nextRoDataAddress.toHex().getRawHexStr()}")
                                        }

                                        val sizeOfOne = Variable.Value.Hex((resizedValues.first().size.getByteCount()).toString(16))
                                        val rest = nextRoDataAddress % sizeOfOne
                                        if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
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

                    is RV64Syntax.S_BSS -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV64Syntax.R_ULBL -> {
                                    val dirType = entry.directive.type
                                    val originalValue: Variable.Value.Hex = when (dirType) {
                                        RV64Syntax.E_DIRECTIVE.DirType.BYTE -> Variable.Value.Hex("", Variable.Size.Bit8())
                                        RV64Syntax.E_DIRECTIVE.DirType.HALF -> Variable.Value.Hex("", Variable.Size.Bit16())
                                        RV64Syntax.E_DIRECTIVE.DirType.WORD -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        RV64Syntax.E_DIRECTIVE.DirType.DWORD -> Variable.Value.Hex("", Variable.Size.Bit64())
                                        RV64Syntax.E_DIRECTIVE.DirType.ASCIZ -> Variable.Value.Hex("", Variable.Size.Bit8())
                                        RV64Syntax.E_DIRECTIVE.DirType.STRING -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        RV64Syntax.E_DIRECTIVE.DirType.BYTE_2 -> Variable.Value.Hex("", Variable.Size.Bit16())
                                        RV64Syntax.E_DIRECTIVE.DirType.BYTE_4 -> Variable.Value.Hex("", Variable.Size.Bit32())
                                        RV64Syntax.E_DIRECTIVE.DirType.BYTE_8 -> Variable.Value.Hex("", Variable.Size.Bit64())
                                        else -> {
                                            Variable.Value.Hex("", Variable.Size.Bit32())
                                        }
                                    }

                                    if (DebugTools.RV64_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${originalValue.toHex().getRawHexStr()} \n\tallocating at ${nextBssAddress.toHex().getRawHexStr()}")
                                    }

                                    val sizeOfOne = Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
                                    val rest = nextBssAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextBssAddress += sizeOfOne - rest
                                    }
                                    dataList.add(DataEntry(entry.label, nextBssAddress.toHex(), originalValue.size, originalValue))
                                    nextBssAddress += emulator.kit.types.Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
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
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // adding rodata addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in rodataList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA, true)
            }

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataList) {
                labelBinAddrMap.set(alloc.label, alloc.address.toBin().getRawBinaryStr())
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // Getting binary and store binary in memory
            val instrIDMap = mutableMapOf<String, AssemblyMap.MapEntry>()
            binaryMapper.setLabelLinks(labelBinAddrMap)
            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value, Variable.Value.Hex((bins.size * 4).toString(16), Variable.Size.Bit32()), architecture)
                if (DebugTools.RV64_showAsmInfo) {
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
                val calcTime = measureTime {
                    address = Variable.Value.Hex((binaryID * 4).toString(16), Variable.Size.Bit32())
                    transcriptEntrys.add(RVDisassembledRow(address))
                }

                val memoryTime = measureTime {
                    memory.store(address, binary, StyleAttr.Main.Table.Mark.PROGRAM)
                }

                if (DebugTools.RV64_showAsmInfo) {
                    console.log("Assembly.generateByteCode(): ASM-STORE ${binaryID}/${bins.size - 1} saving... (calc: ${calcTime.inWholeMicroseconds} µs, memory: ${memoryTime.inWholeMicroseconds} µs)")
                }
            }
            transcriptEntrys.add(RVDisassembledRow((address + Variable.Value.Hex("4", Variable.Size.Bit8())).toHex()))
            architecture.getRegContainer().pc.variable.set(pcStartAddress)
            assemblyMap = AssemblyMap(instrIDMap)
        }
        return assemblyMap ?: AssemblyMap()
    }

    /**
     * Is used by [generateByteCode] to temporarily hold up all important information before it is actually saved to memory.
     */
    class DataEntry(val label: RV64Syntax.E_LABEL, val address: Variable.Value.Hex, val sizeOfOne: Variable.Size, vararg val values: Variable.Value)

    /**
     * Is used to do the flexible formatting of the [Transcript.Row] which is expected by the transcript.
     */
    class RVDisassembledRow(address: Variable.Value.Hex) : Transcript.Row(address) {

        val content = RV64.TS_DISASSEMBLED_HEADERS.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[RV64.TS_DISASSEMBLED_HEADERS.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(architecture: Architecture, instrResult: RV64BinMapper.InstrResult, labelName: String) {
            val instrName = instrResult.type.id
            content[RV64.TS_DISASSEMBLED_HEADERS.Instruction] = Entry(Orientation.LEFT, instrName)
            content[RV64.TS_DISASSEMBLED_HEADERS.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture.getRegContainer(), instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(labelName: String) {
            content[RV64.TS_DISASSEMBLED_HEADERS.Label] = Entry(Orientation.LEFT, labelName)
        }

        override fun getContent(): List<Entry> {
            return RV64.TS_DISASSEMBLED_HEADERS.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }
}