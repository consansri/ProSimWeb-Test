package emulator.kit.compiler.gas

import emulator.kit.Architecture
import emulator.kit.compiler.Assembly
import emulator.kit.compiler.parser.ParserTree

open class GASAssembler(arch: Architecture, val definedAssembly: DefinedAssembly) : Assembly(arch) {

    override fun disassemble() {

    }

    override fun assemble(tree: ParserTree): AssemblyMap {
        return AssemblyMap()
    }
}