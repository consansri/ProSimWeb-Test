package emulator.kit.assembler.gas

import emulator.kit.Architecture
import emulator.kit.assembler.Assembly
import emulator.kit.assembler.parser.ParserTree

open class GASAssembler(arch: Architecture, val definedAssembly: DefinedAssembly) : Assembly(arch) {

    override fun disassemble() {

    }

    override fun assemble(tree: ParserTree): AssemblyMap {
        return AssemblyMap()
    }
}