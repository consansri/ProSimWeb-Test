package extendable.components.assembly

abstract class Grammar {


    abstract fun clear()

    abstract fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree

    fun checkSequence() {

    }

    class GrammarTree(val nodes: MutableList<TreeNode>? = null) {
        fun contains(token: Assembly.Token): TreeNode.TokenNode? {
            nodes?.let {
                for (node in it) {
                    when (node) {
                        is TreeNode.CollectionNode -> {
                            for (tokenNode in node.tokenNodes) {
                                if (tokenNode.tokens.contains(token)) {
                                    return tokenNode
                                }
                            }
                        }

                        is TreeNode.TokenNode -> {
                            if (node.tokens.contains(token)) {
                                return node
                            }
                        }
                    }
                }
            }

            return null
        }
    }

    class Sequence(vararg val sequenceComponents: SequenceComponent, val ignoreSpaces: Boolean = false) {

        fun getLength(): Int {
            return sequenceComponents.size
        }

        fun match(vararg tokens: Assembly.Token): SeqMatchResult {
            val sequenceResult = mutableListOf<SeqMap>()
            var trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Assembly.Token.Space) {
                        trimmedTokens.remove(token)
                    }
                }
            }

            if (trimmedTokens.size < sequenceComponents.size) {
                return SeqMatchResult(false, sequenceResult)
            }
            val tokenList = trimmedTokens.toList()

            for (component in sequenceComponents) {
                val index = sequenceComponents.indexOf(component)
                val token = tokenList[index]


                when (component) {
                    is SequenceComponent.InSpecific -> {
                        when (component) {
                            is SequenceComponent.InSpecific.Symbol -> {
                                if (token is Assembly.Token.Symbol) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Word -> {
                                if (token is Assembly.Token.Word) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.AlphaNum -> {
                                if (token is Assembly.Token.AlphaNum) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Constant -> {
                                if (token is Assembly.Token.Constant) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Register -> {
                                if (token is Assembly.Token.Register) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Instruction -> {
                                if (token is Assembly.Token.Instruction) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Space -> {
                                if (token is Assembly.Token.Space) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }
                        }
                    }

                    is SequenceComponent.Specific -> {
                        if (token.content == component.content) {
                            sequenceResult.add(SeqMap(component, token))
                        }
                    }
                }
            }

            if (sequenceResult.size == sequenceComponents.size) {
                console.log(
                    "Grammar.Sequence.match(): found ${sequenceResult.joinToString("") { it.token.content }}"
                )
                return SeqMatchResult(true, sequenceResult)
            } else {
                return SeqMatchResult(false, sequenceResult)
            }

        }

        data class SeqMap(val component: SequenceComponent, val token: Assembly.Token)

        data class SeqMatchResult(val matches: Boolean, val sequenceMap: List<SeqMap>)

        sealed class SequenceComponent {
            open class Specific(val content: String) : SequenceComponent() {
                class Symbol(content: String) : Specific(content)

                class Word(content: String) : Specific(content)

                class AlphaNum(content: String) : Specific(content)

                class Constant(content: String) : Specific(content)

                class Register(content: String) : Specific(content)

                class Instruction(content: String) : Specific(content)

                class Space(content: String) : Specific(content)

            }

            sealed class InSpecific : SequenceComponent() {

                class Symbol : InSpecific()

                class Word : InSpecific()

                class AlphaNum : InSpecific()

                class Constant : InSpecific()

                class Register : InSpecific()

                class Instruction : InSpecific()

                class Space : InSpecific()

            }
        }
    }


    sealed class TreeNode() {
        open class TokenNode(val hlFlag: String, vararg val tokens: Assembly.Token) : TreeNode()
        open class CollectionNode(vararg val tokenNodes: TokenNode) : TreeNode()
    }


}