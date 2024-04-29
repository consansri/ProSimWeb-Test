package emulator.kit.compiler

import emulator.kit.common.IConsole
import emulator.kit.common.Transcript
import emulator.kit.compiler.lexer.Lexer
import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Parser
import emulator.kit.compiler.parser.ParserTree
import emulator.kit.nativeError
import emulator.kit.nativeLog
import kotlinx.coroutines.*
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

    fun launch(transcript: Transcript, lexer: Lexer, parser: Parser, assembly: Assembly): Result {
        nativeLog("Process: ${state.displayName}")
        val tokens = lexer.tokenize(file)
        state = State.PARSE
        nativeLog("Process: ${state.displayName}")
        val tree = parser.parse(tokens, otherFiles)

        nativeLog("Process: Tree:\n${tree}")

        val assemblyMap: Assembly.AssemblyMap? = if (build && !tree.hasErrors()) {
            state = State.ASSEMBLE
            nativeLog("Process: ${state.displayName}")
            assembly.assembleTree(tree)
        } else {
            null
        }

        state = State.CACHE_RESULTS
        nativeLog("Process: ${state.displayName}")
        parser.treeCache[file] = tree

        val success = !tree.hasErrors()

        val result = Result(success, tokens, tree, assemblyMap)

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

    data class Result(val success: Boolean, val tokens: List<Token>, val tree: ParserTree?, val assemblyMap: Assembly.AssemblyMap?){
        fun hasErrors(): Boolean{
            return tree?.hasErrors() ?: false
        }
    }

}