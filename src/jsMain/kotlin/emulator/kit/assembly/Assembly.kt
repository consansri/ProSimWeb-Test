package emulator.kit.assembly

import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import debug.DebugTools

/**
 * This class behaves as a Template for the custom assembly integration. It has 2 main functions which are triggered from the [Compiler].
 *
 * [generateByteCode] this function converts the given syntax tree to a binary representation. Known as the assembly process.
 *
 * [generateTranscript] this function disassembles the binary representation from [generateByteCode] to resolve information and build a transcript disassembled view.
 */
abstract class Assembly {
    abstract fun generateTranscript(architecture: Architecture, syntaxTree: Syntax.SyntaxTree)
    abstract fun generateByteCode(architecture: Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap
    data class AssemblyMap(val lineAddressMap: Map<String, MapEntry> = mapOf()) {
        init {
            if (DebugTools.KIT_showAsmInfo) {
                console.log("Assembly.AssemblyMap(): $lineAddressMap")
            }
        }

        data class MapEntry(val file: FileHandler.File, val lineID: Int)
    }
}