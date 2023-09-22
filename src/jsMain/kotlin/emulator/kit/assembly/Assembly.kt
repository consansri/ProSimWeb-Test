package emulator.kit.assembly

import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import tools.DebugTools

abstract class Assembly {
    abstract fun generateTranscript(architecture: Architecture, syntaxTree: Syntax.SyntaxTree)
    abstract fun generateByteCode(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap
    data class AssemblyMap(val lineAddressMap: Map<String, MapEntry> = mapOf()) {
        init {
            if (DebugTools.ARCH_showAsmInfo) {
                console.log("Assembly.AssemblyMap(): " + lineAddressMap)
            }
        }

        data class MapEntry(val file: FileHandler.File, val lineID: Int)
    }
}