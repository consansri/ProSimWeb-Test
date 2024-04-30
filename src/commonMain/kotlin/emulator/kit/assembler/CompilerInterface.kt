package emulator.kit.assembler

import emulator.kit.assembler.parser.Parser

interface CompilerInterface {
    val parser: Parser
    val assembly: Assembly
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean = true): Process.Result
    fun runningProcesses(): List<Process>

    fun isInTreeCacheAndHasNoErrors(compilerFile: CompilerFile): Boolean

}