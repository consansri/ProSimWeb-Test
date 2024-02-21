package emulator.kit.assembly.standards

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

abstract class StandardAssembler(private val memAddressWidth: Variable.Size, private val wordWidth: Variable.Size, val instrsAreWordAligned: Boolean) : Assembly() {

    var fromAddr = Hex("0", memAddressWidth)
    var toAddr = Hex("0", memAddressWidth)
    val labels = mutableListOf<StandardSyntax.ELabel>()

    /**
     * Word Amount or Byte Amount
     */
    abstract fun getInstrSpace(arch: Architecture, instr: StandardSyntax.EInstr): Int
    abstract fun getOpBinFromInstr(arch: Architecture, instr: StandardSyntax.EInstr): Array<Bin>
    abstract fun getInstrFromBinary(arch: Architecture, currentAddress: Hex): ResolvedInstr?

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        labels.clear()

        val lineAddressMap = mutableMapOf<String, Compiler.LineLoc>()
        val root = syntaxTree.rootNode ?: return AssemblyMap()
        val sectionContainer = root.containers.firstOrNull { it is StandardSyntax.CSections } ?: return AssemblyMap()

        var currentAddress: Variable.Value = Hex("0", memAddressWidth)

        val tsRows = mutableListOf<CompiledRow>()

        // Calculate Addresses
        for (section in sectionContainer.nodes.filterIsInstance<Syntax.TreeNode.SectionNode>()) {
            for (element in section.collNodes.filterIsInstance<Syntax.TreeNode.ElementNode>()) {
                when (section) {
                    is StandardSyntax.SText -> {
                        when (element) {
                            is StandardSyntax.EInstr -> {
                                lineAddressMap[currentAddress.toHex().getRawHexStr()] = element.tokens.first().lineLoc
                                element.setAddress(currentAddress)
                                val row = CompiledRow(currentAddress.toHex())
                                row.addInstr(element)
                                tsRows.add(row)
                                if (instrsAreWordAligned) {
                                    repeat(getInstrSpace(architecture, element) - 1) {
                                        currentAddress += Hex(wordWidth.getByteCount().toString(16), memAddressWidth)
                                        tsRows.add(CompiledRow(currentAddress.toHex()))
                                    }
                                    toAddr = currentAddress.toHex()
                                    currentAddress += Hex(wordWidth.getByteCount().toString(16), memAddressWidth)
                                } else {
                                    repeat(getInstrSpace(architecture, element) - 1) {
                                        currentAddress += Hex("1", memAddressWidth)
                                        tsRows.add(CompiledRow(currentAddress.toHex()))
                                    }
                                    toAddr = currentAddress.toHex()
                                    currentAddress += Hex("1", memAddressWidth)
                                }
                            }

                            is StandardSyntax.ELabel -> {
                                element.setAddress(currentAddress)
                                labels.add(element)
                            }

                            is StandardSyntax.ESetPC -> {
                                currentAddress = element.constant.getValue(memAddressWidth)
                            }
                        }
                    }

                    is StandardSyntax.SData -> {
                        when (element) {
                            is StandardSyntax.EInitData -> {
                                when (element.dirType.dirMajType) {
                                    StandardSyntax.DirMajType.DE_ALIGNED -> {
                                        val deSize = Hex(element.dirType.deSize?.getByteCount()?.toString(16) ?: "1", memAddressWidth)
                                        val rem = (currentAddress.toBin() % deSize)
                                        if (rem != Bin("0")) {
                                            val padding = deSize - rem
                                            currentAddress += padding
                                        }
                                    }

                                    StandardSyntax.DirMajType.DE_UNALIGNED -> {}
                                    else -> {}
                                }
                                element.setAddress(currentAddress)
                                currentAddress += element.bytesNeeded
                            }

                            is StandardSyntax.ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }

                            is StandardSyntax.ESetPC -> {
                                currentAddress = element.constant.getValue(memAddressWidth)
                            }
                        }
                    }

                    is StandardSyntax.SRoData -> {
                        when (element) {
                            is StandardSyntax.EInitData -> {
                                when (element.dirType.dirMajType) {
                                    StandardSyntax.DirMajType.DE_ALIGNED -> {
                                        val deSize = Hex(element.dirType.deSize?.getByteCount()?.toString(16) ?: "1", memAddressWidth)
                                        val rem = (currentAddress.toBin() % deSize)
                                        if (rem != Bin("0")) {
                                            val padding = deSize - rem
                                            currentAddress += padding
                                        }
                                    }

                                    StandardSyntax.DirMajType.DE_UNALIGNED -> {}
                                    else -> {}
                                }
                                element.setAddress(currentAddress)
                                currentAddress += element.bytesNeeded
                            }

                            is StandardSyntax.ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }

                            is StandardSyntax.ESetPC -> {
                                currentAddress = element.constant.getValue(memAddressWidth)
                            }
                        }
                    }

                    is StandardSyntax.SBss -> {
                        when (element) {
                            is StandardSyntax.EUnInitData -> {
                                when (element.dirType.dirMajType) {
                                    StandardSyntax.DirMajType.DE_ALIGNED -> {
                                        val deSize = Hex(element.dirType.deSize?.getByteCount()?.toString(16) ?: "1", memAddressWidth)
                                        val rem = (currentAddress.toBin() % deSize)
                                        if (rem != Bin("0")) {
                                            val padding = deSize - rem
                                            currentAddress += padding
                                        }
                                    }

                                    StandardSyntax.DirMajType.DE_UNALIGNED -> {}
                                    else -> {}
                                }
                                element.setAddress(currentAddress)
                                val size = element.dirType.deSize
                                if (size != null) {
                                    currentAddress += Hex(size.getByteCount().toString(16), memAddressWidth)
                                } else {
                                    currentAddress += Hex("1", memAddressWidth)
                                    architecture.getConsole().warn("Couldn't resolve uninitialized width for type ${element.dirType.name} -> Allocated 1 byte!")
                                }
                            }

                            is StandardSyntax.ELabel -> {
                                element.setAddress(currentAddress)
                                if (currentAddress == tsRows.lastOrNull()?.addr) {
                                    tsRows.last().addLabel(element)
                                }
                                labels.add(element)
                            }

                            is StandardSyntax.ESetPC -> {
                                currentAddress = element.constant.getValue(memAddressWidth)
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
                    is StandardSyntax.SText -> {
                        when (element) {
                            is StandardSyntax.EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is StandardSyntax.EInstr -> {
                                val address = element.address ?: continue
                                val binary = getOpBinFromInstr(architecture, element)
                                architecture.getMemory().storeArray(address, *binary, mark = StyleAttr.Main.Table.Mark.PROGRAM)
                            }
                        }
                    }

                    is StandardSyntax.SData -> {
                        when (element) {
                            is StandardSyntax.EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is StandardSyntax.EInitData -> {
                                val address = element.address ?: continue
                                val values = element.constants.map { it.getValue(element.dirType.deSize) }
                                architecture.getMemory().storeArray(address, *values.toTypedArray(), mark = StyleAttr.Main.Table.Mark.DATA)
                            }
                        }
                    }

                    is StandardSyntax.SRoData -> {
                        when (element) {
                            is StandardSyntax.EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is StandardSyntax.EInitData -> {
                                val address = element.address ?: continue
                                val values = element.constants.map { it.getValue(element.dirType.deSize) }
                                architecture.getMemory().storeArray(address, *values.toTypedArray(), mark = StyleAttr.Main.Table.Mark.DATA, readonly = true)
                            }
                        }
                    }

                    is StandardSyntax.SBss -> {
                        when (element) {
                            is StandardSyntax.EGlobal -> {
                                val address = element.linkedlabel?.address
                                if (address != null) {
                                    architecture.getRegContainer().pc.set(address)
                                } else architecture.getConsole().error("Label not linked correctly!")
                            }

                            is StandardSyntax.EUnInitData -> {
                                val address = element.address ?: continue
                                architecture.getMemory().store(address, Bin("0", element.dirType.deSize ?: Variable.Size.Bit8()), mark = StyleAttr.Main.Table.Mark.DATA)
                            }
                        }
                    }
                }
            }
        }

        architecture.getTranscript().addContent(Transcript.Type.COMPILED, tsRows)

        return AssemblyMap(lineAddressMap)
    }

    override fun disassemble(architecture: Architecture) {
        val tsRows = mutableListOf<DisassembledRow>()
        var currentAddr: Variable.Value = fromAddr
        while (currentAddr <= toAddr) {
            val instrResult = getInstrFromBinary(architecture, currentAddr.toHex())
            if (instrResult != null) {
                val row = DisassembledRow(currentAddr.toHex())
                row.addInstr(instrResult)
                val label = labels.filter { it.address?.toHex()?.getRawHexStr() == currentAddr.toHex().getRawHexStr() }
                label.forEach { row.addLabel(it) }
                tsRows.add(row)
                if (instrsAreWordAligned) {
                    repeat(instrResult.wordOrByteAmount - 1) {
                        currentAddr += Hex(wordWidth.getByteCount().toString(16), memAddressWidth)
                        tsRows.add(DisassembledRow(currentAddr.toHex()))
                    }
                } else {
                    repeat(instrResult.wordOrByteAmount - 1) {
                        currentAddr += Hex("1", memAddressWidth)
                        tsRows.add(DisassembledRow(currentAddr.toHex()))
                    }
                }
            }

            currentAddr += if (instrsAreWordAligned) Hex(wordWidth.getByteCount().toString(16), memAddressWidth) else Hex("1", memAddressWidth)
        }

        architecture.getTranscript().addContent(Transcript.Type.DISASSEMBLED, tsRows)
    }

    /**
     * Is used to do the flexible formatting of the [Transcript.Row] which is expected by the transcript.
     */
    class DisassembledRow(address: Hex) : Transcript.Row(address) {

        val content = Standards.TsDisassembledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()
        val labels = mutableListOf<StandardSyntax.ELabel>()

        init {
            content[Standards.TsDisassembledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(resolvedInstr: ResolvedInstr) {
            content[Standards.TsDisassembledHeaders.Instruction] = Entry(Orientation.LEFT, resolvedInstr.name)
            content[Standards.TsDisassembledHeaders.Parameters] = Entry(Orientation.LEFT, resolvedInstr.params)
        }

        fun addLabel(label: StandardSyntax.ELabel) {
            labels.add(label)
            content[Standards.TsDisassembledHeaders.Label] = Entry(Orientation.LEFT, labels.joinToString { it.nameString })
        }

        override fun getContent(): List<Entry> {
            return Standards.TsDisassembledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

    /**
     * FOR TRANSCRIPT
     */
    class CompiledRow(val addr: Hex) : Transcript.Row(addr) {
        val content = Standards.TsCompiledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()
        val labels = mutableListOf<StandardSyntax.ELabel>()

        init {
            content[Standards.TsCompiledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addLabel(label: StandardSyntax.ELabel) {
            labels.add(label)
            content[Standards.TsCompiledHeaders.Label] = Entry(Orientation.LEFT, labels.joinToString { it.nameString })
        }

        fun addInstr(instr: StandardSyntax.EInstr) {
            content[Standards.TsCompiledHeaders.Instruction] = Entry(Orientation.LEFT, instr.nameToken.content)
            content[Standards.TsCompiledHeaders.Parameters] = Entry(Orientation.LEFT, instr.params.joinToString("") { it.content })
        }

        override fun getContent(): List<Entry> {
            return Standards.TsCompiledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

    data class ResolvedInstr(val name: String, val params: String, val wordOrByteAmount: Int)

}