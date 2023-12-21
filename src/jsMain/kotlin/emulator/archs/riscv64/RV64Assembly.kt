package emulator.archs.riscv64

import debug.DebugTools
import emulator.archs.riscv32.RV32
import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.optional.ArchSetting
import emulator.kit.types.Variable
import kotlin.time.measureTime

class RV64Assembly(private val binaryMapper: RV64BinMapper) : Assembly() {
    private val labelBinAddrMap = mutableMapOf<RV64Syntax.E_LABEL, String>()
    private val transcriptEntrys = mutableListOf<RVDisassembledRow>()
    private val bins = mutableListOf<Variable.Value.Bin>()

    /**
     * Disassembles the content of the memory and builds the [RVDisassembledRow]'s from it which are then added to the disassembled transcript view.
     */
    override fun generateTranscript(architecture: Architecture, syntaxTree: Syntax.SyntaxTree) {
        if (DebugTools.RV64_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.entries.joinToString("") { "\n\t${it.key.wholeName}: ${it.value}" }}")
        }

        for (rowID in transcriptEntrys.indices) {
            val row = transcriptEntrys[rowID]
            val binary = architecture.getMemory().load(row.getAddresses().first().toHex(), 4).toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (Variable.Value.Bin(labels.value, RV64.MEM_ADDRESS_WIDTH) == row.getAddresses().first().toBin()) {
                    labelString += "${labels.key.wholeName} "
                }
            }
            row.addLabel(labelString)

            val result = binaryMapper.getInstrFromBinary(binary)
            if (result != null) {
                var branchOffset5 = "0"
                var branchOffset7 = "0"
                var jalOffset20 = "0"

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
                        val shiftedImm = Variable.Value.Bin(jalOffset20[0].toString() + jalOffset20.substring(12) + jalOffset20[11] + jalOffset20.substring(1, 11), Variable.Size.Bit20()).getResized(RV64.MEM_ADDRESS_WIDTH) shl 1
                        for (label in labelBinAddrMap) {
                            if (Variable.Value.Bin(label.value) == (row.getAddresses().first().toBin() + shiftedImm).toBin()) {
                                labelstring = label.key.wholeName
                            }
                        }
                    }

                    RV64Syntax.R_INSTR.InstrType.BEQ, RV64Syntax.R_INSTR.InstrType.BNE, RV64Syntax.R_INSTR.InstrType.BLT, RV64Syntax.R_INSTR.InstrType.BGE, RV64Syntax.R_INSTR.InstrType.BLTU, RV64Syntax.R_INSTR.InstrType.BGEU -> {
                        val imm12 = Variable.Value.Bin(branchOffset7[0].toString() + branchOffset5[4] + branchOffset7.substring(1) + branchOffset5.substring(0, 4), Variable.Size.Bit12())
                        val offset = imm12.toBin().getResized(RV64.MEM_ADDRESS_WIDTH) shl 1
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
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> \n${transcriptEntrys.joinToString { entry -> "\n\t" + entry.getContent().joinToString("\t") { it.content } }}")
        }

        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, transcriptEntrys)
    }

    /**
     * Extracts all relevant information from the [syntaxTree] and stores it at certain points in memory.
     */
    override fun generateByteCode(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        val dataSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV64.SETTING.DATA.name }?.value?.get() ?: Variable.Value.Hex("10000", RV32.MEM_ADDRESS_WIDTH)
        val roDataSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV64.SETTING.RODATA.name }?.value?.get() ?: Variable.Value.Hex("20000", RV32.MEM_ADDRESS_WIDTH)
        val bssSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV64.SETTING.BSS.name }?.value?.get() ?: Variable.Value.Hex("30000", RV32.MEM_ADDRESS_WIDTH)

        val rootNode = syntaxTree.rootNode
        var assemblyMap: AssemblyMap? = null
        rootNode?.let {
            labelBinAddrMap.clear()
            transcriptEntrys.clear()
            bins.clear()
            val instructionMapList = mutableMapOf<Long, RV64Syntax.R_INSTR>()
            val dataList = mutableListOf<Entry>()
            val rodataList = mutableListOf<Entry>()
            val bssList = mutableListOf<Entry>()
            var instrID: Long = 0
            var pcStartAddress = Variable.Value.Hex("0", RV64.MEM_ADDRESS_WIDTH)

            var nextDataAddress = dataSecStart
            var nextRoDataAddress = roDataSecStart
            var nextBssAddress = bssSecStart
            // Resolving Sections

            val sections = rootNode.containers.filterIsInstance<RV64Syntax.C_SECTIONS>().flatMap { it.nodes.toList() }

            for (section in sections) {
                when (section) {
                    is RV64Syntax.S_TEXT -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RV64Syntax.R_INSTR -> {
                                    instructionMapList[instrID] = entry
                                    instrID += entry.instrType.memWords
                                }

                                is RV64Syntax.R_JLBL -> {
                                    val address = (instrID * 4).toString(2)
                                    if (DebugTools.RV64_showAsmInfo) {
                                        console.log("RISCVAssembly.generateByteCode(): found Label ${entry.label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                    }
                                    if (entry.isGlobalStart) {
                                        pcStartAddress = Variable.Value.Bin(address, RV64.MEM_ADDRESS_WIDTH).toHex()
                                    }
                                    labelBinAddrMap[entry.label] = address

                                    entry.inlineInstr?.let {
                                        instructionMapList[instrID] = it
                                        instrID += it.instrType.memWords
                                    }
                                }
                            }
                        }
                    }

                    is RV64Syntax.S_DATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV64Syntax.R_ILBL -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV64Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == RV64Syntax.E_DIRECTIVE.DirType.STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV64.XLEN)

                                    val rest = nextDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.LabeledDataEntry(entry.label, nextDataAddress.toHex(), *values.toTypedArray())
                                    rodataList.add(dataEntry)
                                    nextDataAddress += length
                                }

                                is RV64Syntax.R_DATAEMITTING -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV64Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == RV64Syntax.E_DIRECTIVE.DirType.STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV64.XLEN)

                                    val rest = nextDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.DataEntry(nextDataAddress.toHex(), *values.toTypedArray())
                                    rodataList.add(dataEntry)
                                    nextDataAddress += length
                                }
                            }
                        }
                    }

                    is RV64Syntax.S_RODATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV64Syntax.R_ILBL -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV64Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextRoDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == RV64Syntax.E_DIRECTIVE.DirType.STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV64.XLEN)

                                    val rest = nextRoDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextRoDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.LabeledDataEntry(entry.label, nextRoDataAddress.toHex(), *values.toTypedArray())
                                    rodataList.add(dataEntry)
                                    nextRoDataAddress += length
                                }

                                is RV64Syntax.R_DATAEMITTING -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV64Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == RV64Syntax.E_DIRECTIVE.DirType.STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV64.XLEN)

                                    val rest = nextDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.DataEntry(nextDataAddress.toHex(), *values.toTypedArray())
                                    rodataList.add(dataEntry)
                                    nextDataAddress += length
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
                                    dataList.add(Entry.LabeledDataEntry(entry.label, nextBssAddress.toHex(), originalValue))
                                    nextBssAddress += Variable.Value.Hex((originalValue.size.getByteCount()).toString(16))
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
                if (alloc is Entry.LabeledDataEntry) {
                    labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinaryStr()
                }
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // adding rodata addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in rodataList) {
                if (alloc is Entry.LabeledDataEntry) {
                    labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinaryStr()
                }
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA, true)
            }

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataList) {
                if (alloc is Entry.LabeledDataEntry) {
                    labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinaryStr()
                }
                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // Getting binary and store binary in memory

            val instrIDMap = mutableMapOf<String, AssemblyMap.MapEntry>()
            val binBuildingTime = measureTime {

                binaryMapper.setLabelLinks(labelBinAddrMap)
                for (instr in instructionMapList) {
                    val binary = binaryMapper.getBinaryFromInstrDef(instr.value, Variable.Value.Hex((bins.size * 4).toString(16), RV64.MEM_ADDRESS_WIDTH), architecture)
                    if (DebugTools.RV64_showAsmInfo) {
                        console.log(
                            "Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.instrname.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.instrType.id},  \n\t${
                                instr.value.paramcoll?.paramsWithOutSplitSymbols?.joinToString(",") { param ->
                                    param.getAllTokens().joinToString("") { it.content }
                                }
                            } to ${binary.joinToString { it.getRawBinaryStr() }}]"
                        )
                    }
                    for (wordID in binary.indices) {
                        instrIDMap[Variable.Value.Hex(((bins.size + wordID) * 4).toString(16), RV64.MEM_ADDRESS_WIDTH).getRawHexStr()] = AssemblyMap.MapEntry(instr.value.getAllTokens().first().lineLoc.file, instr.value.getAllTokens().first().lineLoc.lineID)
                    }
                    bins.addAll(binary)
                }
            }
            if (DebugTools.RV64_showAsmInfo) {
                console.log("Assembly.generateByteCode(): ASM-BIN-BUILDING took ${binBuildingTime.inWholeMicroseconds} µs")
            }

            var address = Variable.Value.Hex("0", RV64.MEM_ADDRESS_WIDTH)
            val asmStoreTime = measureTime {
                for (binaryID in bins.indices) {
                    val storeTime = measureTime {
                        val binary = bins[binaryID]
                        transcriptEntrys.add(RVDisassembledRow(address))
                        memory.store(address, binary, StyleAttr.Main.Table.Mark.PROGRAM)
                    }
                    if (DebugTools.RV64_showAsmInfo) {
                        console.log("Assembly.generateByteCode(): ASM-STORE ${binaryID + 1}/${bins.size}\ttook ${storeTime.inWholeMicroseconds} µs\taddress: $address")
                    }
                    address = (address + Variable.Value.Hex("4", RV64.MEM_ADDRESS_WIDTH)).toHex()
                }
            }

            if (DebugTools.RV64_showAsmInfo) {
                console.log("Assembly.generateByteCode(): ASM-STORE took ${asmStoreTime.inWholeMicroseconds} µs")
            }

            transcriptEntrys.add(RVDisassembledRow(address.toHex()))
            architecture.getRegContainer().pc.variable.set(pcStartAddress)
            assemblyMap = AssemblyMap(instrIDMap)
        }
        return assemblyMap ?: AssemblyMap()
    }

    /**
     * Is used by [generateByteCode] to temporarily hold up all important information before it is actually saved to memory.
     */
    sealed class Entry(val address: Variable.Value.Hex, vararg val values: Variable.Value) {
        class LabeledDataEntry(val label: RV64Syntax.E_LABEL, address: Variable.Value.Hex, vararg values: Variable.Value) : Entry(address, *values)
        class DataEntry(address: Variable.Value.Hex, vararg values: Variable.Value) : Entry(address, *values)
    }

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
            content[RV64.TS_DISASSEMBLED_HEADERS.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture, instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(labelName: String) {
            content[RV64.TS_DISASSEMBLED_HEADERS.Label] = Entry(Orientation.LEFT, labelName)
        }

        override fun getContent(): List<Entry> {
            return RV64.TS_DISASSEMBLED_HEADERS.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }
}