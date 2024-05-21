package emulator.kit.assembler

import debug.DebugTools
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.TreeResult
import emulator.kit.common.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Represents a process for compiling a file, including tokenization, parsing, semantic analysis, and storing to memory.
 *
 * @property file The main file to be processed.
 * @property otherFiles Additional files to consider during the processing.
 * @property mode The mode of the process, determining at which stage to stop.
 * @property processStart The timestamp when the process started.
 */
data class Process(
    val file: CompilerFile,
    val otherFiles: List<CompilerFile>,
    val mode: Mode,
    val processStart: Instant = Clock.System.now()
) {
    var state: State = State.TOKENIZE
        set(value) {
            field = value
            currentStateStart = Clock.System.now()
        }
    var currentStateStart: Instant = Clock.System.now()

    /**
     * Launches the process with the given lexer, parser, memory, and features.
     *
     * @param lexer The lexer to tokenize the input file.
     * @param parser The parser to generate the parse tree and perform semantic analysis.
     * @param memory The memory where the result will be stored.
     * @param features The optional features to consider during processing.
     * @return The result of the process.
     */
    fun launch(lexer: Lexer, parser: Parser, memory: Memory, features: List<Feature>): Result {
        //Tokenization
        val tokens = lexer.tokenize(file)

        // Parsing the Tree
        state = State.PARSETREE
        val treeResult = parser.parseTree(tokens, otherFiles, features)

        // Caching Results
        state = State.CACHE_RESULTS
        parser.treeCache[file] = treeResult

        if (mode == Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD) return Result(tokens, treeResult, Parser.SemanticResult(), mapOf(), this)

        if (DebugTools.KIT_showGrammarTree) nativeLog("Tree:\n${treeResult}")

        // Semantic Analysis
        state = State.SEMANTICANALYSIS
        val sectionResult = parser.semanticAnalysis(lexer,treeResult, otherFiles, features)

        if (mode == Mode.STOP_AFTER_ANALYSIS) return Result(tokens, treeResult, sectionResult, mapOf(), this)

        if (treeResult.hasErrors()) return Result(tokens, treeResult, sectionResult, mapOf(), this)

        // Store Sections
        state = State.STORETOMEMORY
        val lineMap = storeToMemory(sectionResult.sections, memory)

        return Result(tokens, treeResult, sectionResult, lineMap, this)
    }

    /**
     * Stores the sections into memory and creates a map of line addresses.
     *
     * @param sections The sections to be stored.
     * @param memory The memory to store the sections into.
     * @return A map of line locations by their addresses.
     */
    private fun storeToMemory(sections: Array<GASParser.Section>, memory: Memory): Map<String, Token.LineLoc> {
        val lineAddressMap = mutableMapOf<String, Token.LineLoc>()
        sections.forEach { sec ->
            val secAddr = sec.getSectionAddr()
            sec.getContent().forEach {
                val addr = secAddr + it.offset
                lineAddressMap[addr.toHex().getRawHexStr()] = it.content.getFirstToken().lineLoc
                memory.storeArray(addr, *it.bytes, mark = it.content.getMark())
            }
        }
        return lineAddressMap
    }

    override fun toString(): String {
        return "${file.name} (${state.displayName} ${(Clock.System.now() - currentStateStart).inWholeSeconds}s) ${(Clock.System.now() - processStart).inWholeSeconds}s"
    }

    /**
     * Returns a string indicating whether the process succeeded or failed.
     *
     * @param success Whether the process was successful.
     * @return A string message indicating the result of the process.
     */
    fun getFinishedStr(success: Boolean): String {
        return "${file.name} ${if (success) "build" else "failed"} in ${(Clock.System.now() - currentStateStart).inWholeSeconds}s!"
    }

    /**
     * Enum representing the possible modes of the process.
     */
    enum class Mode {
        FULLBUILD,
        STOP_AFTER_ANALYSIS,
        STOP_AFTER_TREE_HAS_BEEN_BUILD
    }

    /**
     * Enum representing the possible states of the process.
     *
     * @property displayName The display name of the state.
     */
    enum class State(val displayName: String) {
        TOKENIZE("tokenizing"),
        PARSETREE("parsing tree"),
        SEMANTICANALYSIS("semantic analysis"),
        STORETOMEMORY("store sections"),
        CACHE_RESULTS("caching"),
    }

    /**
     * Data class representing the result of the process.
     *
     * @property tokens The tokens generated during tokenization.
     * @property tree The parse tree result.
     * @property semanticResult The result of the semantic analysis.
     * @property assemblyMap The map of line locations by addresses.
     * @property process The process that produced this result.
     */
    data class Result(val tokens: List<Token>, val tree: TreeResult, val semanticResult: Parser.SemanticResult, val assemblyMap: Map<String, Token.LineLoc>, val process: Process) {

        val success: Boolean = !tree.hasErrors()
        val sections = semanticResult.sections
        val root = tree.rootNode

        /**
         * Checks if the result contains any errors.
         *
         * @return True if there are errors, false otherwise.
         */
        fun hasErrors(): Boolean {
            return tree.hasErrors()
        }

        /**
         * Checks if the result contains any warnings.
         *
         * @return True if there are warnings, false otherwise.
         */
        fun hasWarnings(): Boolean {
            return tree.hasWarnings()
        }

        /**
         * Generates a string representation of the sections.
         *
         * @return A string representing the sections.
         */
        fun generateTS(): String = sections.joinToString("\n") { it.toString() }

        /**
         * Generates a short information string about the process result.
         *
         * @return A short string with process result information.
         */
        fun shortInfoStr(): String = process.getFinishedStr(success)
    }

}