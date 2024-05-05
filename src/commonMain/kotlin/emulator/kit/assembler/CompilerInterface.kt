package emulator.kit.assembler

import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser

interface CompilerInterface {
    val parser: Parser
    fun getLastLineMap(): Map<String, Token.LineLoc>
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean = true): Process.Result
    fun runningProcesses(): List<Process>

    fun isInTreeCacheAndHasNoErrors(compilerFile: CompilerFile): Boolean

}