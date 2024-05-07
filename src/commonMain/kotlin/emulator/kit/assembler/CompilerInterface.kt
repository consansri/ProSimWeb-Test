package emulator.kit.assembler

import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser

interface CompilerInterface {
    val parser: Parser
    val lexer: Lexer
    fun getLastLineMap(): Map<String, Token.LineLoc>
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Process.Mode = Process.Mode.FULLBUILD): Process.Result
    fun runningProcesses(): List<Process>

    fun isInTreeCacheAndHasNoErrors(compilerFile: CompilerFile): Boolean
}