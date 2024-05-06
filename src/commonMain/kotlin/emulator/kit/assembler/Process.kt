package emulator.kit.assembler

import debug.DebugTools
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.ParserTree
import emulator.kit.common.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Process(
    val file: CompilerFile,
    val otherFiles: List<CompilerFile>,
    val build: Boolean,
    val processStart: Instant = Clock.System.now()
) {
    var state: State = State.TOKENIZE
        set(value) {
            field = value
            currentStateStart = Clock.System.now()
        }
    var currentStateStart: Instant = Clock.System.now()

    fun launch(lexer: Lexer, parser: Parser, memory: Memory, features: List<Feature>): Result {
        nativeLog("Process: ${state.displayName}")
        val tokens = lexer.tokenize(file)
        state = State.PARSE
        nativeLog("Process: ${state.displayName}")
        val tree = parser.parse(tokens, otherFiles, features)

        if (DebugTools.KIT_showGrammarTree) nativeLog("Process: Tree:\n${tree}")

        val lineMap = if (build && !tree.hasErrors()) {
            state = State.ASSEMBLE
            nativeLog("Process: ${state.displayName}")

            val lineAddressMap = mutableMapOf<String, Token.LineLoc>()
            tree.sections.forEach {sec ->
                val secAddr = sec.getSectionAddr()
                sec.getContent().forEach {
                    val addr = secAddr + it.offset
                    lineAddressMap[addr.toHex().getRawHexStr()] = it.content.getFirstToken().lineLoc
                    memory.storeArray(addr, *it.bytes, mark = it.content.getMark())
                }
            }
            lineAddressMap
        } else {
            mapOf()
        }

        state = State.CACHE_RESULTS
        nativeLog("Process: ${state.displayName}")
        parser.treeCache[file] = tree

        val success = !tree.hasErrors()

        val result = Result(success, tokens, tree, lineMap)

        return result
    }

    override fun toString(): String {
        return "${file.name} (${state.displayName} ${(Clock.System.now() - currentStateStart).inWholeSeconds}s) ${(Clock.System.now() - processStart).inWholeSeconds}s"
    }

    enum class State(val displayName: String) {
        TOKENIZE("tokenizing"),
        PARSE("parsing"),
        ASSEMBLE("assembling"),
        CACHE_RESULTS("caching"),
    }

    data class Result(val success: Boolean, val tokens: List<Token>, val tree: ParserTree?, val assemblyMap: Map<String, Token.LineLoc>) {
        fun hasErrors(): Boolean {
            return tree?.hasErrors() ?: false
        }

        fun hasWarnings(): Boolean {
            return tree?.hasWarnings() ?: false
        }
    }

}