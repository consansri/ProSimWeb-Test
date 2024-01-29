package emulator.archs.riscv32

import StyleAttr
import emulator.kit.Architecture
import emulator.archs.riscv32.RV32Syntax.E_DIRECTIVE.DirType.*
import emulator.archs.riscv32.RV32Syntax.R_INSTR.InstrType.*
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import debug.DebugTools
import emulator.kit.assembly.Compiler
import emulator.kit.optional.ArchSetting
import kotlin.time.measureTime


/**
 * Contains the **RV32 assembly logic**.
 *
 * @property binaryMapper is needed for the exact mapping of instructions in text and binary format.
 *
 * @property labelBinAddrMap temporary contains all address mapped labels. This addresses will be mapped in [assemble].
 * @property transcriptEntrys temporary contains the [RVDisassembledRow]'s which are generated in [disassemble].
 * @property bins temporary contains all binary representations from the instructions.
 *
 */
class RV32Assembly(private val binaryMapper: RV32BinMapper) : Assembly() {

    private val labelBinAddrMap = mutableMapOf<RV32Syntax.E_LABEL, String>()
    private val transcriptEntrys = mutableListOf<RVDisassembledRow>()
    private val bins = mutableListOf<Variable.Value.Bin>()

    /**
     * Disassembles the content of the memory and builds the [RVDisassembledRow]'s from it which are then added to the disassembled transcript view.
     */
    override fun disassemble(architecture: Architecture) {
        val transcript = architecture.getTranscript()
        if (DebugTools.RV32_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): labelMap -> ${labelBinAddrMap.entries.joinToString("") { "\n\t${it.key.wholeName}: ${it.value}" }}")
        }

        for (rowID in transcriptEntrys.indices) {
            val row = transcriptEntrys[rowID]
            val binary = architecture.getMemory().load(row.getAddresses().first().toHex(), 4).toBin()
            var labelString = ""
            for (labels in labelBinAddrMap) {
                if (Variable.Value.Bin(labels.value, Variable.Size.Bit32()) == row.getAddresses().first().toBin()) {
                    labelString += "${labels.key.wholeName} "
                }
            }
            row.addLabel(labelString)

            val result = binaryMapper.getInstrFromBinary(binary)
            if (result != null) {
                var branchOffset5 = ""
                var branchOffset7 = ""
                var jalOffset20 = ""
                result.binMap.entries.forEach {
                    when (it.key) {
                        RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.IMM20 -> {
                            when (result.type) {
                                JAL -> {
                                    jalOffset20 = it.value.getRawBinStr()
                                }

                                BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                                    when (it.key) {
                                        RV32BinMapper.MaskLabel.IMM5 -> {
                                            branchOffset5 = it.value.getRawBinStr()
                                        }

                                        RV32BinMapper.MaskLabel.IMM7 -> {
                                            branchOffset7 = it.value.getRawBinStr()
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

        if (DebugTools.RV32_showAsmInfo) {
            console.log("RISCVAssembly.generateTranscript(): TranscriptEntries -> \n${transcriptEntrys.joinToString {entry -> "\n\t" + entry.getContent().joinToString("\t") { it.content } }}")
        }

        transcript.addContent(Transcript.Type.COMPILED, emptyList())
        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, transcriptEntrys)
    }

    /**
     * Extracts all relevant information from the [syntaxTree] and stores it at certain points in memory.
     */
    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        val dataSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV32.SETTING.DATA.name }?.value?.get() ?: Variable.Value.Hex("10000", RV32.MEM_ADDRESS_WIDTH)
        val roDataSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV32.SETTING.RODATA.name }?.value?.get() ?: Variable.Value.Hex("20000", RV32.MEM_ADDRESS_WIDTH)
        val bssSecStart = architecture.getAllSettings().filterIsInstance<ArchSetting.ImmSetting>().firstOrNull { it.name == RV32.SETTING.BSS.name }?.value?.get() ?: Variable.Value.Hex("30000", RV32.MEM_ADDRESS_WIDTH)

        val rootNode = syntaxTree.rootNode
        var assemblyMap: AssemblyMap? = null
        rootNode?.let {
            labelBinAddrMap.clear()
            transcriptEntrys.clear()
            bins.clear()
            val instructionMapList = mutableMapOf<Long, RV32Syntax.R_INSTR>()
            val dataList = mutableListOf<Entry>()
            val roDataList = mutableListOf<Entry>()
            val bssList = mutableListOf<Entry>()
            var instrID: Long = 0
            var pcStartAddress = Variable.Value.Hex("0", Variable.Size.Bit32())

            var nextDataAddress = dataSecStart
            var nextRoDataAddress = roDataSecStart
            var nextBssAddress = bssSecStart
            // Resolving Sections

            val sections = rootNode.containers.filterIsInstance<RV32Syntax.C_SECTIONS>().flatMap { it.nodes.toList() }

            for (section in sections) {
                when (section) {
                    is RV32Syntax.S_TEXT -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RV32Syntax.R_INSTR -> {
                                    instructionMapList[instrID] = entry
                                    instrID += entry.instrType.memWords
                                }

                                is RV32Syntax.R_JLBL -> {
                                    val address = (instrID * 4).toString(2)
                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("RISCVAssembly.generateByteCode(): found Label ${entry.label.wholeName} and calculated address $address (0x${address.toInt(2).toString(16)})")
                                    }
                                    if (entry.isGlobalStart) {
                                        pcStartAddress = Variable.Value.Bin(address, Variable.Size.Bit32()).toHex()
                                    }
                                    labelBinAddrMap[entry.label] = address

                                    entry.inlineInstr?.let {
                                        instructionMapList[instrID] = it
                                        instrID += it.instrType.memWords
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    is RV32Syntax.S_DATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Syntax.R_ILBL -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV32Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == STRING) {
                                        values = values.flatMap {value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV32.MEM_ADDRESS_WIDTH)

                                    val rest = nextDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.LabeledDataEntry(entry.label, nextDataAddress.toHex(), *values.toTypedArray())
                                    roDataList.add(dataEntry)
                                    nextDataAddress += length
                                }

                                is RV32Syntax.R_DATAEMITTING -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV32Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == STRING) {
                                        values = values.flatMap {value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV32.MEM_ADDRESS_WIDTH)

                                    val rest = nextDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.DataEntry(nextDataAddress.toHex(), *values.toTypedArray())
                                    roDataList.add(dataEntry)
                                    nextDataAddress += length
                                }
                            }
                        }
                    }

                    is RV32Syntax.S_RODATA -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Syntax.R_ILBL -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV32Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextRoDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV32.MEM_ADDRESS_WIDTH)

                                    val rest = nextRoDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextRoDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.LabeledDataEntry(entry.label, nextRoDataAddress.toHex(), *values.toTypedArray())
                                    roDataList.add(dataEntry)
                                    nextRoDataAddress += length
                                }

                                is RV32Syntax.R_DATAEMITTING -> {
                                    val params = entry.paramcoll.paramsWithOutSplitSymbols

                                    var values = params.mapNotNull {
                                        if (it is RV32Syntax.E_PARAM.Constant) {
                                            val constToken = it.constant
                                            constToken.getValue(entry.directive.type.deSize)
                                        } else {
                                            null
                                        }
                                    }

                                    if (DebugTools.RV32_showAsmInfo) {
                                        console.log("Assembly.generateByteCode(): ASM-ALLOC found ${values.joinToString { it.toString() }} allocating at ${nextRoDataAddress.toHex().getRawHexStr()}")
                                    }

                                    if (entry.directive.type == STRING) {
                                        values = values.flatMap { value -> value.toHex().getRawHexStr().chunked(2).map { Variable.Value.Hex(it, Variable.Size.Bit8()) } }
                                    }

                                    val sizeOfOne = Variable.Value.Hex(entry.directive.type.deSize?.getByteCount()?.toString(16) ?: "1", Variable.Size.Bit8())
                                    val length = sizeOfOne * Variable.Value.Hex(values.size.toString(16), RV32.MEM_ADDRESS_WIDTH)

                                    val rest = nextRoDataAddress % sizeOfOne
                                    if (rest != Variable.Value.Bin("0", Variable.Size.Bit1())) {
                                        nextRoDataAddress += sizeOfOne - rest
                                    }
                                    val dataEntry = Entry.DataEntry(nextRoDataAddress.toHex(), *values.toTypedArray())
                                    roDataList.add(dataEntry)
                                    nextRoDataAddress += length
                                }
                            }
                        }
                    }

                    is RV32Syntax.S_BSS -> {
                        for (entry in section.sectionContent) {
                            when (entry) {
                                is RV32Syntax.R_ULBL -> {
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

                                    if (DebugTools.RV32_showAsmInfo) {
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
                when (alloc) {
                    is Entry.LabeledDataEntry -> {
                        labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinStr()
                    }

                    is Entry.DataEntry -> {}
                }

                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // adding roData addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in roDataList) {
                when (alloc) {
                    is Entry.LabeledDataEntry -> {
                        labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinStr()
                    }

                    is Entry.DataEntry -> {}
                }

                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA, true)
            }

            // adding data alloc addresses and labels to labelLink Map and storing alloc constants to memory
            for (alloc in dataList) {
                when (alloc) {
                    is Entry.LabeledDataEntry -> {
                        labelBinAddrMap[alloc.label] = alloc.address.toBin().getRawBinStr()
                    }

                    is Entry.DataEntry -> {}
                }

                memory.storeArray(address = alloc.address, values = alloc.values, StyleAttr.Main.Table.Mark.DATA)
            }

            // Getting binary and store binary in memory
            val instrIDMap = mutableMapOf<String, Compiler.LineLoc>()
            binaryMapper.setLabelLinks(labelBinAddrMap)
            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value, Variable.Value.Hex((bins.size * 4).toString(16), Variable.Size.Bit32()), architecture)
                if (DebugTools.RV32_showAsmInfo) {
                    console.log(
                        "Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.instrname.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.instrType.id},  \n\t${
                            instr.value.paramcoll?.paramsWithOutSplitSymbols?.joinToString(",") {param -> 
                                param.getAllTokens().joinToString("") { it.content }
                            }
                        } to ${binary.joinToString { it.getRawBinStr() }}]"
                    )
                }
                for (wordID in binary.indices) {
                    instrIDMap[Variable.Value.Hex(((bins.size + wordID) * 4).toString(16), Variable.Size.Bit32()).getRawHexStr()] = instr.value.getAllTokens().first().lineLoc
                }
                bins.addAll(binary)
            }

            var address = Variable.Value.Hex("0", RV32.MEM_ADDRESS_WIDTH)
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
     * Is used by [assemble] to temporarily hold up all important information before it is actually saved to memory.
     */
    sealed class Entry(val address: Variable.Value.Hex, vararg val values: Variable.Value) {
        class LabeledDataEntry(val label: RV32Syntax.E_LABEL, address: Variable.Value.Hex, vararg values: Variable.Value) : Entry(address, *values)
        class DataEntry(address: Variable.Value.Hex, vararg values: Variable.Value) : Entry(address, *values)
    }


    /**
     * Is used to do the flexible formatting of the [Transcript.Row] which is expected by the transcript.
     */
    class RVDisassembledRow(address: Variable.Value.Hex) : Transcript.Row(address) {

        val content = RV32.TsDisassembledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[RV32.TsDisassembledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(architecture: Architecture, instrResult: RV32BinMapper.InstrResult, labelName: String) {
            val instrName = instrResult.type.id
            content[RV32.TsDisassembledHeaders.Instruction] = Entry(Orientation.LEFT, instrName)
            content[RV32.TsDisassembledHeaders.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture, instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(labelName: String) {
            content[RV32.TsDisassembledHeaders.Label] = Entry(Orientation.LEFT, labelName)
        }

        override fun getContent(): List<Entry> {
            return RV32.TsDisassembledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }


}