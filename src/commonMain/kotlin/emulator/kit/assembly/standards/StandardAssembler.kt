package emulator.kit.assembly.standards

import emulator.kit.Architecture
import emulator.kit.compiler.Assembly
import emulator.kit.common.Transcript
import emulator.kit.compiler.parser.ParserTree
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

abstract class StandardAssembler(arch: Architecture,private val memAddressWidth: Variable.Size, private val wordWidth: Variable.Size, val instrsAreWordAligned: Boolean) : Assembly(arch) {

    var instrHexStrings = mutableListOf<String>()

    /**
     * Word Amount or Byte Amount
     */

    override fun assemble(tree: ParserTree): AssemblyMap {


        return AssemblyMap()
    }

    override fun disassemble() {
        arch.getTranscript().clear(Transcript.Type.DISASSEMBLED)
        val tsDisassembledRows = mutableListOf<DisassembledRow>()


        arch.getTranscript().addContent(Transcript.Type.DISASSEMBLED, tsDisassembledRows)
    }

    /**
     * Is used to do the flexible formatting of the [Transcript.Row] which is expected by the transcript.
     */
    class DisassembledRow(address: Hex) : Transcript.Row(address) {

        val content = Standards.TsDisassembledHeaders.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[Standards.TsDisassembledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addInstr(resolvedInstr: ResolvedInstr) {
            content[Standards.TsDisassembledHeaders.Instruction] = Entry(Orientation.LEFT, resolvedInstr.name)
            content[Standards.TsDisassembledHeaders.Parameters] = Entry(Orientation.LEFT, resolvedInstr.params)
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

        init {
            content[Standards.TsCompiledHeaders.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        override fun getContent(): List<Entry> {
            return Standards.TsCompiledHeaders.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

    data class ResolvedInstr(val name: String, val params: String, val wordOrByteAmount: Int)

}