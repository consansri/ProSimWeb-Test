package emulator.archs.t6502

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax

class T6502Assembly: Assembly() {
    override fun disassemble(architecture: Architecture) {

    }

    override fun assemble(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {

        return AssemblyMap()
    }
}