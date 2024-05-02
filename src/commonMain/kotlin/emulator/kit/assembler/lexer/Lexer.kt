package emulator.kit.assembler.lexer

import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.lexer.Token.LineLoc
import emulator.kit.assembler.lexer.Token
import emulator.kit.nativeLog

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

        var keyWordType: Token.Type? = null
        var foundReg: RegContainer.Register?
        var foundInstr: InstrTypeInterface?
        var foundDir: DirTypeInterface?

        var result: MatchResult?
        while (remaining.isNotEmpty()) {
            result = null
            keyWordType = null
            foundReg = null
            foundInstr = null
            foundDir = null

            for (type in Token.Type.entries) {
                result = type.regex?.find(remaining)

                if (result == null) {
                    continue
                }

                if (type == Token.Type.SYMBOL) {
                    // Check directives
                    for (dir in dirs) {
                        if (dir.second.matches(result.value)) {
                            foundDir = dir.first
                            keyWordType = Token.Type.DIRECTIVE
                            break
                        }
                    }

                    // check for instruction
                    for (instr in instrs) {
                        if (instr.second.matches(result.value)) {
                            foundInstr = instr.first
                            keyWordType = Token.Type.INSTRNAME
                            break
                        }
                    }

                    if (regs != null) {
                        for (reg in regs) {
                            if (reg.second.matches(result.value)) {
                                foundReg = reg.first
                                keyWordType = Token.Type.REGISTER
                                break
                            }
                        }
                    }
                }

                //nativeLog("Found ${if (keyWordType != null) keyWordType else type}: ${result.value}")
                val token = Token(if (keyWordType != null) keyWordType else type, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size, foundReg, foundDir, foundInstr)
                tokenList += token
                startIndex += result.value.length
                remaining = file.content.substring(startIndex)
                if (type == Token.Type.LINEBREAK) lineID += 1
                break
            }
        }

        return tokenList
    }

    fun pseudoTokenize(lineLoc: LineLoc, content: String): List<Token>{
        val regs = if (detectRegisters) architecture.getAllRegs().map { it to it.getRegex() } else null
        val instrs = architecture.getAllInstrTypes().map { it to it.getInstrRegex() }
        val dirs = architecture.getAllDirTypes().map { it to it.getDirRegex() }

        val tokenList = mutableListOf<Token>()
        var remaining: String = content
        var lineID = 0
        var startIndex = 0

        var keyWordType: Token.Type? = null
        var foundReg: RegContainer.Register?
        var foundInstr: InstrTypeInterface?
        var foundDir: DirTypeInterface?

        var result: MatchResult?
        while (remaining.isNotEmpty()) {
            result = null
            keyWordType = null
            foundReg = null
            foundInstr = null
            foundDir = null

            for (type in Token.Type.entries) {
                result = type.regex?.find(remaining)

                if (result == null) {
                    continue
                }

                if (type == Token.Type.SYMBOL) {
                    // Check directives
                    for (dir in dirs) {
                        if (dir.second.matches(result.value)) {
                            foundDir = dir.first
                            keyWordType = Token.Type.DIRECTIVE
                            break
                        }
                    }

                    // check for instruction
                    for (instr in instrs) {
                        if (instr.second.matches(result.value)) {
                            foundInstr = instr.first
                            keyWordType = Token.Type.INSTRNAME
                            break
                        }
                    }

                    if (regs != null) {
                        for (reg in regs) {
                            if (reg.second.matches(result.value)) {
                                foundReg = reg.first
                                keyWordType = Token.Type.REGISTER
                                break
                            }
                        }
                    }
                }

                //nativeLog("Found ${if (keyWordType != null) keyWordType else type}: ${result.value}")
                val token = Token(if (keyWordType != null) keyWordType else type, lineLoc, result.value, tokenList.size, foundReg, foundDir, foundInstr)
                tokenList += token
                startIndex += result.value.length
                remaining = content.substring(startIndex)
                if (type == Token.Type.LINEBREAK) lineID += 1
                break
            }
        }

        return tokenList
    }


    val regex = Regex("^(?:jal|j)$")

    private fun InstrTypeInterface.getInstrRegex(): Regex = Regex("^${Regex.escape(this.getDetectionName())}$", RegexOption.IGNORE_CASE)
    private fun DirTypeInterface.getDirRegex(): Regex = Regex("^\\.${Regex.escape(this.getDetectionString())}$", RegexOption.IGNORE_CASE)
    private fun RegContainer.Register.getRegex(): Regex = Regex("^(?:${(this.names + this.aliases).joinToString("|") { Regex.escape(it) }})$")

}