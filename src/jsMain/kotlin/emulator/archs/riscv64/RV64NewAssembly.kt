package emulator.archs.riscv64

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.archs.riscv64.RV64NewSyntax.*
import emulator.kit.assembly.Compiler
import emulator.kit.common.Transcript
import emulator.archs.riscv64.RV64NewSyntax.InstrType.*

class RV64NewAssembly(val binMapper: RV64BinMapper) : Assembly() {

    var fromAddr = Hex("0", RV64.MEM_ADDRESS_WIDTH)
    var toAddr = Hex("0", RV64.MEM_ADDRESS_WIDTH)
    val labels = mutableListOf<ELabel>()

    override fun disassemble(architecture: Architecture) {
        val tsRows = mutableListOf<RVDisassembledRow>()
        var currentAddr: Variable.Value = fromAddr
        while (currentAddr <= toAddr) {
            val bin = architecture.getMemory().load(currentAddr.toHex(), 4)
            val instrResult = binMapper.getInstrFromBinary(bin)
            if (instrResult != null) {
                val row = RVDisassembledRow(currentAddr.toHex())
                var labelstring = ""
                when (instrResult.type) {
                    JAL -> {
                        val jalOffset20 = instrResult.binMap.get(RV64BinMapper.MaskLabel.IMM20)?.toBin()?.getRawBinStr()
                        if (jalOffset20 != null) {
                            val shiftedImm = Bin(jalOffset20[0].toString() + jalOffset20.substring(12) + jalOffset20[11] + jalOffset20.substring(1, 11), Bit20()).getResized(RV64.MEM_ADDRESS_WIDTH) shl 1
                            for (label in labels) {
                                if (label.address?.toHex()?.getRawHexStr() == (row.getAddresses().first().toBin() + shiftedImm).toHex().getRawHexStr()) {
                                    labelstring = label.nameString
                                }
                            }
                        }
                    }

                    BEQ, BNE, BLT, BGE, BLTU, BGEU -> {
                        val branchOffset7 = instrResult.binMap[RV64BinMapper.MaskLabel.IMM7]?.toBin()?.getRawBinStr()
                        val branchOffset5 = instrResult.binMap[RV64BinMapper.MaskLabel.IMM5]?.toBin()?.getRawBinStr()
                        if (branchOffset7 != null && branchOffset5 != null) {
                            val imm12 = Bin(branchOffset7[0].toString() + branchOffset5[4] + branchOffset7.substring(1) + branchOffset5.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.MEM_ADDRESS_WIDTH) shl 1
                            for (label in labels) {
                                if (label.address?.toBin() == (row.getAddresses().first().toBin() + offset).toBin()) {
                                    labelstring = label.nameString
                                }
                            }
                        }
                    }

                    else -> {}
                }

                row.addInstr(architecture, instrResult, labelstring)
                val label = labels.filter { it.address?.toHex()?.getRawHexStr() == currentAddr.toHex().getRawHexStr() }
                label.forEach { row.addLabel(it) }
                tsRows.add(row)
            }

            currentAddr += Hex("4", RV64.MEM_ADDRESS_WIDTH)
        }

        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, tsRows)
    }

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        labels.clear()

        val lineAddressMap = mutableMapOf<String, Compiler.LineLoc>()
        val root = syntaxTree.rootNode ?: return AssemblyMap()
        val sectionContainer = root.containers.firstOrNull { it is CSections } ?: return AssemblyMap()

        var currentAddress: Variable.Value = Hex("0", RV64.MEM_ADDRESS_WIDTH)

        val tsRows = mutableListOf<RVCompiledRow>()

        // Calculate Addresses
        for (section in sectionContainer.nodes.filterIsInstance<Syntax.TreeNode.SectionNode>()) {
            for (element in section.collNodes.filterIsInstance<Syntax.TreeNode.ElementNode>()) {
                when (section) {
                    is SText -> {
                        when (element) {
                            is EInstr -> {
                                lineAddressMap[currentAddress.toHex().getRawHexStr()] = element.tokens.first().lineLoc
                                element.setAddress(currentAddress)
                                val row = RVCompiledRow(currentAddress.toHex())
                                row.addInstr(element)
                                tsRows.add(row)
                                repeat(element.type.memWords - 1) {
                                    currentAddress += Hex("4", RV64.MEM_ADDRESS_WIDTH)
                                    tsRows.add(RVCompiledRow(currentAddress.toHex()))
                                }
                                toAddr = currentAddress.toHex()
                                currentAddress += Hex("4", RV64.MEM_ADDRESS_WIDTH)
                            }

                            is ELabel -> {
                                element.setAddress(currentAddress)
                                labels.add(element)
                            }
                        }
                    }

                    is SData -> {
                        when (element) {
                            is EInitData -> {
                                element.setAddress(currentAddress)
                                currentAddress += element.bytesNeeded
                            }

                            is ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }
                        }
                    }

                    is SRoData -> {
                        when (element) {
                            is EInitData -> {
                                element.setAddress(currentAddress)
                                currentAddress += element.bytesNeeded
                            }

                            is ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }
                        }
                    }

                    is SBss -> {
                        when (element) {
                            is EUnInitData -> {
                                element.setAddress(currentAddress)
                                val size = element.dirType.deSize
                                if (size != null) {
                                    currentAddress += Hex(size.getByteCount().toString(16), RV64.MEM_ADDRESS_WIDTH)
                                } else architecture.getConsole().error("Couldn't resolve uninitialized width!")
                            }

                            is ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }
                        }
                    }
                }
            }
        }

        // Add Labels to TS
        for (label in labels) {
            val labelAddr = label.address
            tsRows.firstOrNull { it.addr.getRawHexStr() == labelAddr?.toHex()?.getRawHexStr() }?.addLabel(label)
        }

        // Store Content
        for (section in sectionContainer.nodes.filterIsInstance<Syntax.TreeNode.SectionNode>()) {
            for (element in section.collNodes.filterIsInstance<Syntax.TreeNode.ElementNode>()) {
                when (section) {
                    is SText -> {
                        when (element) {
                            is EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is EInstr -> {
                                val address = element.address ?: continue
                                val binary = binMapper.getBinaryFromNewInstrDef(element, architecture)
                                architecture.getMemory().storeArray(address, *binary, mark = StyleAttr.Main.Table.Mark.PROGRAM)
                            }
                        }
                    }

                    is SData -> {
                        when (element) {
                            is EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is EInitData -> {
                                val address = element.address ?: continue
                                val values = element.constants.map { it.getValue(element.dirType.deSize) }
                                architecture.getMemory().storeArray(address, *values.toTypedArray(), mark = StyleAttr.Main.Table.Mark.DATA)
                            }
                        }
                    }

                    is SRoData -> {
                        when (element) {
                            is EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is EInitData -> {
                                val address = element.address ?: continue
                                val values = element.constants.map { it.getValue(element.dirType.deSize) }
                                architecture.getMemory().storeArray(address, *values.toTypedArray(), mark = StyleAttr.Main.Table.Mark.DATA, readonly = true)
                            }
                        }
                    }

                    is SBss -> {
                        when (element) {
                            is EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is EUnInitData -> {
                                val address = element.address ?: continue
                                architecture.getMemory().store(address, Bin("0", element.dirType.deSize ?: Bit8()), mark = StyleAttr.Main.Table.Mark.DATA)
                            }
                        }
                    }
                }
            }
        }

        architecture.getTranscript().addContent(Transcript.Type.COMPILED, tsRows)

        return AssemblyMap(lineAddressMap)
    }

    /**
     * Is used to do the flexible formatting of the [Transcript.Row] which is expected by the transcript.
     */
    class RVDisassembledRow(address: Hex) : Transcript.Row(address) {

        val content = RV64.TsDisassembledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()
        val labels = mutableListOf<ELabel>()

        init {
            content[RV64.TsDisassembledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(architecture: Architecture, instrResult: RV64BinMapper.InstrResult, labelName: String) {
            val instrName = instrResult.type.id
            content[RV64.TsDisassembledHeaders.Instruction] = Entry(Orientation.LEFT, instrName)
            content[RV64.TsDisassembledHeaders.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture, instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(label: ELabel) {
            labels.add(label)
            content[RV64.TsDisassembledHeaders.Label] = Entry(Orientation.LEFT, labels.joinToString { it.nameString })
        }

        override fun getContent(): List<Entry> {
            return RV64.TsDisassembledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

    /**
     * FOR TRANSCRIPT
     */
    class RVCompiledRow(val addr: Hex) : Transcript.Row(addr) {
        val content = RV64.TsCompiledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()
        val labels = mutableListOf<ELabel>()

        init {
            content[RV64.TsCompiledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addLabel(label: ELabel) {
            labels.add(label)
            content[RV64.TsCompiledHeaders.Label] = Entry(Orientation.LEFT, labels.joinToString { it.nameString })
        }

        fun addInstr(instr: EInstr) {
            content[RV64.TsCompiledHeaders.Instruction] = Entry(Orientation.LEFT, "${instr.type.id}${if (instr.type.pseudo && instr.type.relative == null) "\t(pseudo)" else ""}")
            content[RV64.TsCompiledHeaders.Parameters] = Entry(Orientation.LEFT, instr.params.joinToString("") { it.content })
        }

        override fun getContent(): List<Entry> {
            return RV64.TsCompiledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

}