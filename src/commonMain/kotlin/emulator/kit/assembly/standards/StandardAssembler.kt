package emulator.kit.assembly.standards

import emulator.kit.Architecture
import emulator.kit.assembler.Assembly
import emulator.kit.assembler.parser.ParserTree
import emulator.kit.types.Variable

abstract class StandardAssembler(arch: Architecture,private val memAddressWidth: Variable.Size, private val wordWidth: Variable.Size, val instrsAreWordAligned: Boolean) : Assembly(arch) {

    var instrHexStrings = mutableListOf<String>()

    /**
     * Word Amount or Byte Amount
     */

    override fun assemble(tree: ParserTree): AssemblyMap {


        return AssemblyMap()
    }

    override fun disassemble() {

    }

}