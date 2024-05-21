package emulator.kit.assembler

import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

/**
 * Interface representing a defined assembly configuration.
 */
interface DefinedAssembly {

    /** The size of memory addresses. */
    val memAddrSize: Variable.Size

    /** The size of words. */
    val wordSize: Variable.Size

    /** Determines if registers are detected by name. */
    val detectRegistersByName: Boolean

    /** The lexer prefices used for parsing instructions and directives. */
    val prefices: Lexer.Prefices

    /**
     * Retrieves the instruction types based on the provided features.
     *
     * @param features The optional features influencing instruction retrieval.
     * @return A list of instruction type interfaces.
     */
    fun getInstrs(features: List<Feature>): List<InstrTypeInterface>

    /**
     * Retrieves additional directive types.
     *
     * @return A list of directive type interfaces.
     */
    fun getAdditionalDirectives(): List<DirTypeInterface>

    /**
     * Parses instruction parameters from the provided raw instruction node.
     *
     * @param rawInstr The raw instruction node to parse.
     * @param tempContainer The temporary container for parsing.
     * @return A list of section contents representing parsed instruction parameters.
     */
    fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent>
}