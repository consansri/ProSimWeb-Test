package emulator.kit.compiler

import emulator.kit.Architecture
import emulator.kit.optional.FileHandler
import debug.DebugTools
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.ParserTree

/**
 * This class behaves as a Template for the custom assembly integration. It has 2 main functions which are triggered from the [Compiler].
 *
 * [assemble] this function converts the given syntax tree to a binary representation. Known as the assembly process.
 *
 * [disassemble] this function disassembles the binary representation from [assemble] to resolve information and build a transcript disassembled view.
 */
abstract class Assembly(val arch: Architecture) {

    var currentAssemblyMap: AssemblyMap = AssemblyMap()

    /**
     * Generates the disassembled [Architecture.transcript] from current bytes in the [Architecture.memory]
     */
    abstract fun disassemble()

    fun assembleTree(tree: ParserTree): AssemblyMap {
        currentAssemblyMap = assemble(tree)
        return currentAssemblyMap
    }

    /**
     * Generates and stores the bytes resolved from the [tree] in the [Architecture.memory].
     */
    protected abstract fun assemble(tree: ParserTree): AssemblyMap

    /**
     * Used to hold the identification of editor lines in [FileHandler.File]'s with memory addresses
     */
    data class AssemblyMap(val lineAddressMap: Map<String, Token.LineLoc> = mapOf()) {
        init {
            if (DebugTools.KIT_showAsmInfo) {
                println("Assembly.AssemblyMap(): $lineAddressMap")
            }
        }
    }
}