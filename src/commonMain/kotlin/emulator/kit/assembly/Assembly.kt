package emulator.kit.assembly

import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import debug.DebugTools

/**
 * This class behaves as a Template for the custom assembly integration. It has 2 main functions which are triggered from the [Compiler].
 *
 * [assemble] this function converts the given syntax tree to a binary representation. Known as the assembly process.
 *
 * [disassemble] this function disassembles the binary representation from [assemble] to resolve information and build a transcript disassembled view.
 */
abstract class Assembly {

    /**
     * Generates the disassembled [Architecture.transcript] from current bytes in the [Architecture.memory]
     */
    abstract fun disassemble(architecture: emulator.kit.Architecture)

    /**
     * Generates and stores the bytes resolved from the [syntaxTree] in the [Architecture.memory].
     */
    abstract fun assemble(architecture: emulator.kit.Architecture, syntaxTree: Syntax.SyntaxTree): AssemblyMap

    /**
     * Used to hold the identification of editor lines in [FileHandler.File]'s with memory addresses
     */
    data class AssemblyMap(val lineAddressMap: Map<String, Compiler.LineLoc> = mapOf()) {
        init {
            if (DebugTools.KIT_showAsmInfo) {
                println("Assembly.AssemblyMap(): $lineAddressMap")
            }
        }
    }
}