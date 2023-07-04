package extendable.components.assembly

import extendable.Architecture
import extendable.components.connected.Memory

abstract class Compiler {

    abstract fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree)

    abstract fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree)


}