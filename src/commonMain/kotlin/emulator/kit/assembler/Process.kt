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

    fun launch(lexer: Lexer, parser: Parser, memory: Memory, features: List<Feature>): Result {
        /**
         * Tokenization
         *
         * returns tokens
         */
        val tokens = lexer.tokenize(file)

        /**
         * Parsing the Tree
         *
         * returns tree
         */
        state = State.PARSETREE
        val treeResult = parser.parseTree(tokens, otherFiles, features)

        state = State.CACHE_RESULTS
        parser.treeCache[file] = treeResult

        if (mode == Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD) return Result(tokens, treeResult, Parser.SemanticResult(), mapOf(), this)

        if (DebugTools.KIT_showGrammarTree) nativeLog("Tree:\n${treeResult}")

        /**
         * Semantic Analysis
         *
         * returns parsed Result
         */
        state = State.SEMANTICANALYSIS
        val sectionResult = parser.semanticAnalysis(lexer,treeResult, otherFiles, features)

        if (mode == Mode.STOP_AFTER_ANALYSIS) return Result(tokens, treeResult, sectionResult, mapOf(), this)

        if (treeResult.hasErrors()) return Result(tokens, treeResult, sectionResult, mapOf(), this)

        /**
         * Store Sections
         *
         * returns lineMap
         */
        state = State.STORETOMEMORY
        val lineMap = storeToMemory(sectionResult.sections, memory)

        return Result(tokens, treeResult, sectionResult, lineMap, this)
    }

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

    fun getFinishedStr(success: Boolean): String {
        return "${file.name} ${if (success) "build" else "failed"} in ${(Clock.System.now() - currentStateStart).inWholeSeconds}s!"
    }

    enum class Mode {
        FULLBUILD,
        STOP_AFTER_ANALYSIS,
        STOP_AFTER_TREE_HAS_BEEN_BUILD
    }

    enum class State(val displayName: String) {
        TOKENIZE("tokenizing"),
        PARSETREE("parsing tree"),
        SEMANTICANALYSIS("semantic analysis"),
        STORETOMEMORY("store sections"),
        CACHE_RESULTS("caching"),
    }

    data class Result(val tokens: List<Token>, val tree: TreeResult, val semanticResult: Parser.SemanticResult, val assemblyMap: Map<String, Token.LineLoc>, val process: Process) {

        val success: Boolean = !tree.hasErrors()
        val sections = semanticResult.sections
        val root = tree.rootNode


        fun hasErrors(): Boolean {
            return tree.hasErrors()
        }

        fun generateTS(): String = sections.joinToString("\n") { it.toString() }

        fun shortInfoStr(): String = process.getFinishedStr(success)

        fun hasWarnings(): Boolean {
            return tree.hasWarnings()
        }
    }

}