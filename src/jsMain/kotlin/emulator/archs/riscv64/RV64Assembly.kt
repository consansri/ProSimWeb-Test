package emulator.archs.riscv64

import emulator.kit.Architecture
import emulator.kit.assembly.Assembly
import emulator.kit.assembly.Syntax

class RV64Assembly:Assembly() {
    override fun generateTranscript(architecture: Architecture, syntaxTree: Syntax.SyntaxTree) {
        TODO("Not yet implemented")
    }

    override fun generateByteCode(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap {
        TODO("Not yet implemented")
    }
}