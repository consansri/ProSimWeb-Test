package extendable.components.assembly

import extendable.Architecture
import tools.DebugTools

abstract class Assembly {

    abstract fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree)

    abstract fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree): AssemblyMap

    data class AssemblyMap(val lineAddressMap: Map<String, Int> = mapOf()) {
        init {
            if (DebugTools.ARCH_showAsmInfo) {
                console.log("Assembly.AssemblyMap(): " + lineAddressMap)
            }
        }
    }

}