package emulator.kit.assembler.lexer

import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.lexer.Token.LineLoc
import emulator.kit.assembler.lexer.Token

/**
 * Lexing for GNU Assembler Syntax
 */
class Lexer(private val architecture: Architecture, private val detectRegisters: Boolean) {

    /**
     * Sequentially computes the [Lexer.Token]s from the [file] content.
     *
     * Sequence:
     * 1.  [Token.LINEBREAK] LineBreaks
     * 2.  [Token.SPACE] WhiteSpaces
     * 3.  [Token.COMMENT] Comments
     * 4.  [Token.KEYWORD] Keywords (Directives, Instructions, Registers)
     * 5.  [Token.LITERAL] Literals
     * 6.  [Token.SYMBOL] Symbols
     * 7.  [Token.OPERATOR] Operators
     * 8.  [Token.PUNCTUATION] Punctuation
     * 9.  [Token.ANYCHAR] Any single char
     * 10. [Token.ERROR] ErrorTokens
     *
     */
    fun tokenize(file: CompilerFile): List<Token> {
        val regs = if (detectRegisters) architecture.getAllRegs().map { it to it.getRegex() } else null
        val instrs = architecture.getAllInstrTypes().map { it to it.getInstrRegex() }
        val dirs = architecture.getAllDirTypes().map { it to it.getDirRegex() }

        val tokenList = mutableListOf<Token>()
        var remaining: String = file.content
        var lineID = 0
        var startIndex = 0

        var foundReg: RegContainer.Register?
        var foundInstr: InstrTypeInterface?
        var foundDir: DirTypeInterface?

        while (remaining.isNotEmpty()) {
            foundReg = null
            foundInstr = null
            foundDir = null

            var result: MatchResult?
            for (type in Token.Type.entries) {
                result = type.regex?.find(remaining)

                if (result == null) {
                    when (type) {
                        Token.Type.DIRECTIVE -> {
                            for (dir in dirs) {
                                result = dir.second.find(remaining)
                                if (result != null) {
                                    foundDir = dir.first
                                    break
                                }
                            }
                        }

                        Token.Type.INSTRNAME -> {
                            for (instr in instrs) {
                                result = instr.second.find(remaining)
                                if (result != null) {
                                    foundInstr = instr.first
                                    break
                                }
                            }
                        }

                        Token.Type.REGISTER -> {
                            if (regs == null) continue
                            for (reg in regs) {
                                result = reg.second.find(remaining)
                                if (result != null) {
                                    foundReg = reg.first
                                    break
                                }
                            }
                        }

                        else -> {
                            continue
                        }
                    }
                }

                if (result != null) {
                    val token = Token(type, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size, foundReg, foundDir, foundInstr)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    if (type == Token.Type.LINEBREAK) lineID += 1
                    break
                }
            }
        }

        return tokenList
    }

    companion object {
        val regex = Regex("^j?")
    }

    private fun InstrTypeInterface.getInstrRegex(): Regex = Regex("^${Regex.escape(this.getDetectionName())}?", RegexOption.IGNORE_CASE)
    private fun DirTypeInterface.getDirRegex(): Regex = Regex("^\\.${Regex.escape(this.getDetectionString())}?", RegexOption.IGNORE_CASE)
    private fun RegContainer.Register.getRegex(): Regex = Regex("^(?:${(this.names + this.aliases).joinToString("|") { Regex.escape(it) }})?")

}