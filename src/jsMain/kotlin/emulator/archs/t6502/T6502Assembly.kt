package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*

class T6502Assembly : Assembly() {
    private val labelBinAddrMap = mutableMapOf<T6502Syntax.ELabel, Hex>()
    private val instrAddrList = mutableListOf<Pair<Hex, T6502Syntax.EInstr>>()

    override fun disassemble(architecture: Architecture) {
        if(instrAddrList.isEmpty()) return

        val firstAddress = instrAddrList.minBy { it.first.getRawHexStr() }.first
        val lastAddress = instrAddrList.maxBy { it.first.getRawHexStr() }.first
        var nextAddress = firstAddress
        var nextOpCode = architecture.getMemory().load(nextAddress).toHex()

        while (true) {
            val instrType = T6502Syntax.InstrType.entries.firstOrNull { it.opCode.values.contains(nextOpCode) } ?: break
            val amode = instrType.opCode.map { it.value to it.key }.toMap()[nextOpCode] ?: break

            val extAddr = (nextAddress + Hex("1", T6502.MEM_ADDR_SIZE)).toHex()
            val nextByte = architecture.getMemory().load(extAddr).toHex()
            val nextWord = architecture.getMemory().load(extAddr, 2).toHex()

            val row = DisassembledRow(nextAddress, instrType, amode, nextByte, nextWord)
            architecture.getTranscript().addRow(Transcript.Type.DISASSEMBLED, row)

            if (nextAddress > lastAddress || nextAddress == Hex("FFFF", T6502.WORD_SIZE)) break

            nextAddress = (if (amode.immSize != null) nextAddress + Hex((amode.immSize.getByteCount() + 1).toString(16), T6502.WORD_SIZE) else nextAddress + Hex("1", T6502.WORD_SIZE)).toHex()
            nextOpCode = architecture.getMemory().load(nextAddress).toHex()
        }
    }

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        val lineAddressMap = mutableMapOf<String, AssemblyMap.MapEntry>()

        syntaxTree.rootNode ?: return AssemblyMap()

        if(syntaxTree.rootNode.containers.isEmpty()) return AssemblyMap()

        labelBinAddrMap.clear()
        instrAddrList.clear()

        var currentAddress = Hex("0600", T6502.WORD_SIZE)
        architecture.getRegContainer().pc.set(currentAddress)

        // BUILD labelBinAddrMap and instrBinAddrMap
        for (container in syntaxTree.rootNode.containers) {
            when (container) {
                is T6502Syntax.SText -> {
                    for (entry in container.elements) {
                        when (entry) {
                            is T6502Syntax.EInstr -> {
                                lineAddressMap[currentAddress.getRawHexStr()] = AssemblyMap.MapEntry(entry.tokens.first().lineLoc.file, entry.tokens.first().lineLoc.lineID)
                                instrAddrList.add(currentAddress to entry)
                                val extWidth = if (entry.opCodeRelevantAMode.immSize != null) Hex((entry.opCodeRelevantAMode.immSize.getByteCount() + 1).toString(16), Bit16()) else Hex("1", Bit16())
                                currentAddress = (currentAddress + extWidth).toHex()
                            }

                            is T6502Syntax.ESetAddr -> {
                                currentAddress = entry.value.toHex()
                            }

                            is T6502Syntax.ELabel -> {
                                labelBinAddrMap[entry] = currentAddress
                            }
                        }
                    }
                }
            }
        }

        for (instrPair in instrAddrList) {
            val instr = instrPair.second
            val opCode = instr.instrType.opCode[instr.opCodeRelevantAMode]

            if (opCode == null) {
                architecture.getConsole().error("Couldn't resolve OpCode of instruction: ${instr.instrType} with ${instr.opCodeRelevantAMode}!")
                continue
            }

            architecture.getConsole().log("store: ${instrPair.first} -> $opCode")

            architecture.getMemory().store(instrPair.first, opCode, mark = StyleAttr.Main.Table.Mark.PROGRAM)

            val extAddr = instrPair.first + Hex("1", Bit16())
            val extValue = if (instr.linkedLabel != null) labelBinAddrMap[instr.linkedLabel] else instr.imm

            architecture.getTranscript().addRow(Transcript.Type.COMPILED, CompiledRow(instrPair.first, labelBinAddrMap.toList().filter { it.second.getRawHexStr() == instrPair.first.getRawHexStr() }.map { it.first }, instr, extValue?.toHex(), instr.linkedLabel))

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

    class CompiledRow(addr: Variable.Value, labels: List<T6502Syntax.ELabel>, instr: T6502Syntax.EInstr, ext: Hex?, extLabel: T6502Syntax.ELabel?) : Transcript.Row(addr) {

        val content = listOf(
            Entry(Orientation.CENTER, addr.toHex().toString()),
            Entry(Orientation.LEFT, labels.joinToString(", ") { it.labelName }),
            Entry(Orientation.LEFT, "${instr.instrType.name} (${instr.opCodeRelevantAMode.name})"),
            Entry(Orientation.LEFT, instr.addressingMode.getString(ext ?: Hex("0", T6502.BYTE_SIZE), ext ?: Hex("0", T6502.WORD_SIZE), extLabel?.labelName))
        )

        override fun getContent(): List<Entry> {
            return content
        }
    }

    class DisassembledRow(addr: Variable.Value, instrType: T6502Syntax.InstrType, amode: T6502Syntax.AModes, nextByte: Hex, nextWord: Hex) : Transcript.Row(addr) {

        val content = listOf(
            Entry(Orientation.CENTER, addr.toHex().toString()),
            Entry(Orientation.LEFT, instrType.name),
            Entry(Orientation.LEFT, amode.getString(nextByte, nextWord))
        )

        override fun getContent(): List<Entry> {
            return content
        }
    }


}