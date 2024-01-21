package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.types.Variable

class T6502Assembly : Assembly() {
    private val labelBinAddrMap = mutableMapOf<T6502Syntax.ELabel, Variable.Value.Hex>()
    private val instrAddrList = mutableListOf<Pair<Variable.Value.Hex, T6502Syntax.EInstr>>()

    override fun disassemble(architecture: Architecture) {

    }

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        val lineAddressMap = mutableMapOf<String, AssemblyMap.MapEntry>()

        syntaxTree.rootNode ?: return AssemblyMap()

        labelBinAddrMap.clear()
        instrAddrList.clear()

        var currentAddress = Variable.Value.Hex("0600", T6502.WORD_SIZE)
        architecture.getRegContainer().pc.set(currentAddress)

        // BUILD labelBinAddrMap and instrBinAddrMap
        for (container in syntaxTree.rootNode.containers) {
            when (container) {
                is T6502Syntax.SText -> {
                    for (entry in container.elements) {
                        when (entry) {
                            is T6502Syntax.EInstr -> {
                                lineAddressMap.set(currentAddress.getRawHexStr(), AssemblyMap.MapEntry(entry.tokens.first().lineLoc.file, entry.tokens.first().lineLoc.lineID))
                                instrAddrList.add(currentAddress to entry)
                                val extWidth = if (entry.opCodeRelevantAMode.immSize != null) Variable.Value.Hex((entry.opCodeRelevantAMode.immSize.getByteCount() + 1).toString(16), Variable.Size.Bit16()) else Variable.Value.Hex("1", Variable.Size.Bit16())
                                currentAddress = (currentAddress + extWidth).toHex()
                            }

                            is T6502Syntax.ESetAddr -> {
                                currentAddress = entry.value.toHex()
                            }

                            is T6502Syntax.ELabel -> {
                                labelBinAddrMap.set(entry, currentAddress)
                            }
                        }
                    }
                }
            }
        }

        for (instrPair in instrAddrList) {
            val instr = instrPair.second
            val opCode = instr.instrType.opCode.get(instr.opCodeRelevantAMode)

            if (opCode == null) {
                architecture.getConsole().error("Couldn't resolve OpCode of instruction: ${instr.instrType} with ${instr.opCodeRelevantAMode}!")
                continue
            }

            architecture.getConsole().log("store: ${instrPair.first} -> $opCode")

            architecture.getMemory().store(instrPair.first, opCode, mark = StyleAttr.Main.Table.Mark.PROGRAM)

            val extAddr = instrPair.first + Variable.Value.Hex("1", Variable.Size.Bit16())
            val extValue = if (instr.linkedLabel != null) labelBinAddrMap.get(instr.linkedLabel) else instr.imm

            architecture.getTranscript().addRow(Transcript.Type.COMPILED, CompiledRow(instrPair.first, labelBinAddrMap.toList().filter { it.second.getRawHexStr() == instrPair.first.getRawHexStr() }.map { it.first }, instr, extValue?.toHex()))

            if (instr.opCodeRelevantAMode.immSize == null) continue

            if (extValue == null) {
                architecture.getConsole().error("Couldn't resolve Immediate of instruction: ${instr.instrType} with ${instr.opCodeRelevantAMode}!")
                continue
            }

            architecture.getConsole().log("store: $extAddr -> $extValue")
            architecture.getMemory().storeArray(extAddr, extValue, mark = StyleAttr.Main.Table.Mark.PROGRAM)
        }
        return AssemblyMap(lineAddressMap)
    }

    class CompiledRow(addr: Variable.Value, labels: List<T6502Syntax.ELabel>, instr: T6502Syntax.EInstr, ext: Variable.Value.Hex?) : Transcript.Row(addr) {

        val content = listOf(
            Entry(Orientation.CENTER, addr.toHex().toString()),
            Entry(Orientation.LEFT, labels.joinToString(", ") { it.labelName }),
            Entry(Orientation.LEFT, "${instr.instrType.name} ${instr.opCodeRelevantAMode.name}"),
            Entry(Orientation.LEFT, ext?.getHexStr() ?: "")
        )

        override fun getContent(): List<Entry> {
            return content
        }
    }


}