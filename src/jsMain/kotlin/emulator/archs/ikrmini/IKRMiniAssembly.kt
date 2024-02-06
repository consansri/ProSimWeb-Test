package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax

class IKRMiniAssembly : Assembly() {
    override fun disassemble(architecture: Architecture) {

    }

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        return AssemblyMap()
    }
}