package cengine.lang.asm.lexer

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.gas.GASDirType
import cengine.psi.lexer.impl.BaseLexer
import emulator.kit.common.RegContainer
import emulator.kit.nativeWarn

class AsmLexer(input: String, val asmSpec: AsmSpec) : BaseLexer(input) {

    private val prefices: Prefices get() = asmSpec.prefices
    private val regs: List<Pair<RegContainer.Register, Regex>> = asmSpec.allRegs.map { it to it.getRegex() }
    private val instrs: List<Pair<InstrTypeInterface, Regex>> = asmSpec.allInstrTypes().map { it to it.getInstrRegex() }
    private val dirs: List<Pair<DirTypeInterface, Regex>> = (GASDirType.entries + asmSpec.additionalDirectives()).map { it to it.getDirRegex() }
    private val regexMap: Map<AsmTokenType, Regex?> = AsmTokenType.entries.associateWith { it.getRegex(prefices) }

    override fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): AsmToken {
        val initialPos = position
        val token = consume(ignoreLeadingSpaces, ignoreComments)
        position = initialPos
        return token
    }

    override fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): AsmToken {
        val regs = regs

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position, position)
        }

        if (ignoreComments) {
            val token = peek(ignoreLeadingSpaces, false)
            if (token.type.isComment()) consume(ignoreLeadingSpaces, false)
        }

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position, position)
        }

        if (ignoreLeadingSpaces) skipSpaces()

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position, position)
        }

        for (type in AsmTokenType.entries) {

            val regex = regexMap[type]
            if (regex == null) {
                continue
            }
            //val contentToMatch = input.substring(index)
            val match = regex.matchAt(input, index)
            if (match == null) {
                continue
            }

            //regex.find(input, index) ?: continue

            val matchedText = match.value
            val startPosition = position

            advance(matchedText.length)

            val endPosition = position

            var keyWordType: AsmTokenType? = null

            if (type == AsmTokenType.SYMBOL) {
                // Check directives
                for (dir in dirs) {
                    if (dir.second.matchEntire(matchedText) != null) {
                        keyWordType = AsmTokenType.DIRECTIVE
                        break
                    }
                }

                // check for instruction
                for (instr in instrs) {
                    if (instr.second.matchEntire(matchedText) != null) {
                        keyWordType = AsmTokenType.INSTRNAME
                        break
                    }
                }

                for (reg in regs) {
                    if (reg.second.matchEntire(matchedText) != null) {
                        keyWordType = AsmTokenType.REGISTER
                        break
                    }
                }
            }

            val onlyNumber = when (type) {
                AsmTokenType.INT_BIN -> matchedText.removePrefix(prefices.bin)
                AsmTokenType.INT_HEX -> matchedText.removePrefix(prefices.hex)
                AsmTokenType.INT_OCT -> matchedText.removePrefix(prefices.oct)
                AsmTokenType.INT_DEC -> matchedText.removePrefix(prefices.dec)
                else -> null
            }

            val token = AsmToken(
                keyWordType ?: type,
                matchedText,
                startPosition,
                endPosition,
                onlyNumber
            )

            return token
        }

        nativeWarn("AsmLexer: retry $index ${input.substring(index).take(5)}")

        //throw Lexer.InvalidTokenException(foundToken ?: token, startPosition.index)
        //nativeError("InvalidTokenException $token $index")
        return consume(ignoreLeadingSpaces, ignoreComments)
    }

    private fun InstrTypeInterface.getInstrRegex(): Regex = Regex("${Regex.escape(this.getDetectionName())}", RegexOption.IGNORE_CASE)
    private fun DirTypeInterface.getDirRegex(): Regex = Regex("\\.${Regex.escape(this.getDetectionString())}", RegexOption.IGNORE_CASE)
    private fun RegContainer.Register.getRegex(): Regex = Regex("(?:${(this.names + this.aliases).joinToString("|") { Regex.escape(it) }})")

    interface Prefices {
        val hex: String
        val oct: String
        val bin: String
        val dec: String
        val comment: String
        val symbol: Regex
    }
}