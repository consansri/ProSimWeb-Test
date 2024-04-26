package emulator.kit.compiler

import emulator.kit.compiler.parser.Parser
import kotlinx.coroutines.Deferred

interface CompilerInterface {
    val parser: Parser
    val assembly: Assembly
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean = true): Process.Result
    fun runningProcesses(): List<Process>

    fun isInTreeCacheAndHasNoErrors(compilerFile: CompilerFile): Boolean

}