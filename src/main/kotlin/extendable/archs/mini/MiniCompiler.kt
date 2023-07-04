package extendable.archs.mini

import extendable.Architecture
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar

class MiniCompiler: Compiler() {
    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        TODO("Not yet implemented")
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        TODO("Not yet implemented")
    }
}