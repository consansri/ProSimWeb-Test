package emulator.kit.compiler.lexer

import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import emulator.kit.compiler.CompilerFile
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.lexer.Token.LineLoc
import emulator.kit.compiler.lexer.Token

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

        while (remaining.isNotEmpty()) {

            /**
             * Resolve LineBreaks
             */
            val lineBreak = Token.LINEBREAK.REGEX.find(remaining)
            if (lineBreak != null) {
                val token = Token.LINEBREAK(LineLoc(file.name, lineID, startIndex, startIndex + lineBreak.value.length), lineBreak.value, tokenList.size)
                tokenList += token
                startIndex += lineBreak.value.length
                remaining = file.content.substring(startIndex)
                lineID += 1
                continue
            }

            /**
             * Resolve Spaces
             */
            val space = Token.SPACE.REGEX.find(remaining)
            if (space != null) {
                val token = Token.SPACE(LineLoc(file.name, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                tokenList += token
                startIndex += space.value.length
                remaining = file.content.substring(startIndex)
                continue
            }

            /**
             * Resolve Comments
             */
            var foundComment = false
            for (type in Token.COMMENT.CommentType.entries) {
                val result = type.regex.find(remaining)
                if (result != null) {
                    val token = Token.COMMENT(type, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    foundComment = true
                    break
                }
            }
            if (foundComment) continue


            /**
             * Resolve Labels
             */
            val localLabel = Token.LABEL.Local.REGEX.find(remaining)
            if (localLabel != null) {
                val identifier = localLabel.value.removeSuffix(":").toIntOrNull()
                if (identifier != null) {
                    val token = Token.LABEL.Local(identifier, LineLoc(file.name, lineID, startIndex, startIndex + localLabel.value.length), localLabel.value, tokenList.size)
                    tokenList += token
                    startIndex += localLabel.value.length
                    remaining = file.content.substring(startIndex)
                    continue
                }
            }

            val basicLabel = Token.LABEL.Basic.REGEX.find(remaining)
            if (basicLabel != null) {
                val token = Token.LABEL.Basic(LineLoc(file.name, lineID, startIndex, startIndex + basicLabel.value.length), basicLabel.value, tokenList.size)
                tokenList += token
                startIndex += basicLabel.value.length
                remaining = file.content.substring(startIndex)
                continue
            }

            /*
             * Resolve Symbols and Keywords
             */
            val symbol = Token.SYMBOL.REGEX.find(remaining)
            if (symbol != null) {
                // Directives
                var foundDir = false
                for (dir in dirs) {
                    val result = dir.second.matchEntire(symbol.value)
                    if (result != null) {
                        val token = Token.KEYWORD.Directive(dir.first, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                        tokenList += token
                        startIndex += result.value.length
                        remaining = file.content.substring(startIndex)
                        foundDir = true
                        break
                    }
                }
                if (foundDir) continue

                // Instrs
                var foundInstr = false
                for (instr in instrs) {
                    val result = instr.second.matchEntire(symbol.value)
                    if (result != null) {
                        val token = Token.KEYWORD.InstrName(instr.first, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                        tokenList += token
                        startIndex += result.value.length
                        remaining = file.content.substring(startIndex)
                        foundInstr = true
                        break
                    }
                }
                if (foundInstr) continue

                // Regs
                var foundReg = false
                regs?.let {
                    for (reg in regs) {
                        val result = reg.second.matchEntire(symbol.value)
                        if (result != null) {
                            val token = Token.KEYWORD.Register(reg.first, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                            tokenList += token
                            startIndex += result.value.length
                            remaining = file.content.substring(startIndex)
                            foundReg = true
                            break
                        }
                    }
                }
                if (foundReg) continue

                val token = Token.SYMBOL(LineLoc(file.name, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                tokenList += token
                startIndex += symbol.value.length
                remaining = file.content.substring(startIndex)
                continue
            }


            /**
             * Resolve Literals
             */
            // Numbers
            var foundInt = false
            for (intType in Token.LITERAL.NUMBER.INTEGER.IntegerFormat.entries) {
                val result = intType.regex.find(remaining)
                if (result != null) {
                    val token = Token.LITERAL.NUMBER.INTEGER(intType, result, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    foundInt = true
                    break
                }
            }
            if (foundInt) continue

            // Characters
            var foundString = false
            for (type in Token.LITERAL.CHARACTER.STRING.StringType.entries) {
                val result = type.regex.find(remaining)
                if (result != null) {
                    val token = Token.LITERAL.CHARACTER.STRING(type, result, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    foundString = true
                    break
                }
            }
            if (foundString) continue

            val char = Token.LITERAL.CHARACTER.CHAR.REGEX.find(remaining)
            if (char != null) {
                val token = Token.LITERAL.CHARACTER.CHAR(char, LineLoc(file.name, lineID, startIndex, startIndex + char.value.length), char.value, tokenList.size)
                tokenList += token
                startIndex += char.value.length
                remaining = file.content.substring(startIndex)
                continue
            }

            /**
             * Resolve Operators
             */
            var foundOperator = false
            for (type in Token.OPERATOR.OperatorType.entries) {
                val result = type.regex.find(remaining)
                if (result != null) {
                    val token = Token.OPERATOR(type, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    foundOperator = true
                    break
                }
            }
            if (foundOperator) continue

            /**
             * Resolve Punctuation
             */
            var foundPunctuation = false
            for (type in Token.PUNCTUATION.PunctuationType.entries) {
                val result = type.regex.find(remaining)
                if (result != null) {
                    val token = Token.PUNCTUATION(type, LineLoc(file.name, lineID, startIndex, startIndex + result.value.length), result.value, tokenList.size)
                    tokenList += token
                    startIndex += result.value.length
                    remaining = file.content.substring(startIndex)
                    foundPunctuation = true
                    break
                }
            }
            if (foundPunctuation) continue

            /**
             * Resolve [Token.ANYCHAR]
             */
            val anyChar = Token.ANYCHAR.REGEX.find(remaining)
            if (anyChar != null) {
                val token = Token.ANYCHAR(LineLoc(file.name, lineID, startIndex, startIndex + anyChar.value.length), anyChar.value, tokenList.size)
                tokenList += token
                startIndex += anyChar.value.length
                remaining = file.content.substring(startIndex)
                continue
            }

            /**
             * Resolve Error Token
             */
            val error = remaining.first().toString()
            val errorToken = Token.ERROR(LineLoc(file.name, lineID, startIndex, startIndex + error.length), error, tokenList.size)
            tokenList += errorToken
            startIndex += error.length
            remaining = file.content.substring(startIndex)
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