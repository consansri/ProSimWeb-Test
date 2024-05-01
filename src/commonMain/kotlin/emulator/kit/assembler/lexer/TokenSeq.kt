package emulator.kit.assembler.lexer

import emulator.kit.common.RegContainer
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.parser.Node
import emulator.kit.nativeWarn
import emulator.kit.types.Variable

class TokenSeq(private vararg val components: Component, val ignoreSpaces: Boolean = false, val addIgnoredSpacesToMap: Boolean = false) {

    init {
        if (components.isEmpty()) {
            nativeWarn("Empty TokenSequence Defined!")
        }
    }

    fun getLength(): Int {
        return components.size
    }

    fun matches(vararg tokens: Token): SeqMatchResult {
        val trimmedTokens = tokens.toMutableList()
        if (trimmedTokens.isEmpty()) return SeqMatchResult(false, emptyList(), emptyList())

        while (true) {
            val first = components.first()
            val matches = when (first) {
                is Component.SpecNode -> {
                    first.parse(trimmedTokens) != null
                }

                is Component.Specific -> {
                    first.matches(trimmedTokens.first())
                }

                is Component.InSpecific -> {
                    first.matches(trimmedTokens.first())
                }
            }

            if (matches) {
                val result = matchStart(*trimmedTokens.toTypedArray())
                if (result.matches) {
                    return result
                }
            }

            trimmedTokens.removeFirst()

            if (trimmedTokens.isEmpty()) break
        }

        return SeqMatchResult(false, emptyList(), emptyList())
    }


    fun matchStart(vararg tokens: Token): SeqMatchResult {
        val trimmedTokens = tokens.toMutableList()
        if (trimmedTokens.isEmpty()) return SeqMatchResult(false, emptyList(), emptyList())

        val sequenceMap = mutableListOf<SeqMap>()
        val nodes = mutableListOf<Node>()

        for (component in components) {
            if (component is Component.InSpecific.SPACE && ignoreSpaces) {
                if (trimmedTokens.isNotEmpty() && component.matches(trimmedTokens.first())) {
                    sequenceMap.add(SeqMap(component, trimmedTokens.first()))
                    trimmedTokens.removeFirst()
                } else {
                    return SeqMatchResult(false, emptyList(), emptyList())
                }
            } else {
                if (ignoreSpaces) {
                    while (trimmedTokens.isNotEmpty() && trimmedTokens.first().type == Token.Type.WHITESPACE) {
                        val space = trimmedTokens.removeFirst()
                        if (addIgnoredSpacesToMap) {
                            sequenceMap.add(SeqMap(Component.InSpecific.SPACE, space))
                        }
                    }
                }

                if (trimmedTokens.isEmpty()) {
                    return SeqMatchResult(false, emptyList(), emptyList())
                }

                if (trimmedTokens.isNotEmpty()) {
                    val node: GASNode?
                    val matchesFirst: Boolean?
                    when (component) {
                        is Component.SpecNode -> {
                            node = component.parse(trimmedTokens)
                            matchesFirst = null
                        }

                        is Component.Specific -> {
                            node = null
                            matchesFirst = component.matches(trimmedTokens.first())
                        }

                        is Component.InSpecific -> {
                            node = null
                            matchesFirst = component.matches(trimmedTokens.first())
                        }
                    }
                    if (node == null && matchesFirst == null) return SeqMatchResult(false, emptyList(), emptyList())

                    if (node != null) {
                        sequenceMap.add(SeqMap(component, *node.getAllTokens()))
                        nodes.add(node)
                        trimmedTokens.removeAll(node.getAllTokens().toSet())
                    }

                    if (matchesFirst != null) {
                        sequenceMap.add(SeqMap(component, trimmedTokens.first()))
                        trimmedTokens.removeFirst()
                    }
                }
            }
        }

        return SeqMatchResult(true, sequenceMap, nodes)
    }

    class SeqMap(val component: Component, vararg val token: Token)

    data class SeqMatchResult(val matches: Boolean, val sequenceMap: List<SeqMap>, val nodes: List<Node>)

    sealed class Component() {
        override fun toString(): String = this::class.simpleName.toString()

        sealed class SpecNode : Component() {
            abstract fun parse(tokens: List<Token>): GASNode.NumericExpr?
            class EXPRESSION() : SpecNode() {
                override fun parse(tokens: List<Token>): GASNode.NumericExpr? {
                    return GASNode.NumericExpr.parse(tokens)
                }
            }
        }

        sealed class InSpecific : Component() {
            abstract fun matches(token: Token): Boolean
            data class REG(val regFile: RegContainer.RegisterFile? = null, val notInRegFile: RegContainer.RegisterFile? = null) : InSpecific() {
                override fun matches(token: Token): Boolean {
                    if (regFile == null && notInRegFile == null) return token.type == Token.Type.REGISTER

                    val regInMatches = if (regFile != null) {
                        token.type == Token.Type.REGISTER && regFile.unsortedRegisters.contains(token.reg)
                    } else {
                        true
                    }

                    val notInMatches = if (notInRegFile != null) {
                        token.type == Token.Type.REGISTER && !notInRegFile.unsortedRegisters.contains(token.reg)
                    } else {
                        true
                    }

                    return regInMatches && notInMatches
                }
            }

            data object NEWLINE : InSpecific() {
                override fun matches(token: Token): Boolean = token.type == Token.Type.LINEBREAK
            }

            data object SPACE : InSpecific() {
                override fun matches(token: Token): Boolean = token.type == Token.Type.WHITESPACE
            }

            class INTEGER(val size: Variable.Size? = null) : InSpecific() {
                override fun matches(token: Token): Boolean {
                    if (!token.type.isNumberLiteral) return false
                    return true
                }
            }


            class SYMBOL(val endsWidth: String? = null, val startsWith: String? = null, val ignoreCase: Boolean = false) : InSpecific() {
                override fun matches(token: Token): Boolean {
                    if (token.type != Token.Type.SYMBOL) return false

                    if (endsWidth != null && !token.content.endsWith(token.content, ignoreCase)) return false
                    if (startsWith != null && !token.content.startsWith(token.content, ignoreCase)) return false

                    return true
                }
            }
        }

        class Specific(private val content: String, val ignoreCase: Boolean = false) : Component() {
            fun matches(token: Token): Boolean = if (ignoreCase) token.content.uppercase() == content.uppercase() else token.content == content
        }
    }
}