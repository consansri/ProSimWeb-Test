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
                row.addInstr(architecture, instrResult, labels.firstOrNull { it.address?.toHex()?.getRawHexStr() == currentAddr.toHex().getRawHexStr() }?.nameString ?: "")
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
                                repeat(element.type.memWords - 1){
                                    currentAddress += Hex("4", RV64.MEM_ADDRESS_WIDTH)
                                    tsRows.add(RVCompiledRow(currentAddress.toHex()))
                                }
                                toAddr = currentAddress.toHex()
                                currentAddress += Hex("4", RV64.MEM_ADDRESS_WIDTH)
                            }

                            is ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress.toHex().getRawHexStr() == tsRows.lastOrNull()?.addr?.getRawHexStr()) {
                                    tsRows.last().addLabel(element)
                                }
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

        init {
            content[RV64.TsDisassembledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(architecture: Architecture, instrResult: RV64BinMapper.InstrResult, labelName: String) {
            val instrName = instrResult.type.id
            content[RV64.TsDisassembledHeaders.Instruction] = Entry(Orientation.LEFT, instrName)
            content[RV64.TsDisassembledHeaders.Parameters] = Entry(Orientation.LEFT, instrResult.type.paramType.getTSParamString(architecture, instrResult.binMap.toMutableMap(), labelName))
        }

        fun addLabel(labelName: String) {
            content[RV64.TsDisassembledHeaders.Label] = Entry(Orientation.LEFT, labelName)
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

        init {
            content[RV64.TsCompiledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addLabel(label: ELabel) {
            content[RV64.TsCompiledHeaders.Label] = Entry(Orientation.LEFT, label.nameString)
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