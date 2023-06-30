package extendable.components.assembly

abstract class Grammar {

    abstract val applyStandardHLForRest: Boolean

    abstract fun clear()

    abstract fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree

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

                        is TreeNode.SectionNode -> {
                            for (collNode in node.collNodes) {
                                for (tokenNode in collNode.tokenNodes) {
                                    if (tokenNode.tokens.contains(token)) {
                                        return tokenNode
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return null
        }
    }

    class TokenSequence(vararg val sequenceComponents: SequenceComponent, val ignoreSpaces: Boolean = false) {

        fun getLength(): Int {
            return sequenceComponents.size
        }

        fun exactlyMatches(vararg tokens: Assembly.Token): SeqMatchResult {
            var trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Assembly.Token.Space) {
                        trimmedTokens.remove(token)
                    }
                }
            }

            if (sequenceComponents.size != trimmedTokens.size) {
                return SeqMatchResult(false, emptyList())
            } else {
                val sequenceResult = match(*trimmedTokens.toTypedArray())

                if (sequenceResult.size == sequenceComponents.size) {
                    return SeqMatchResult(true, sequenceResult)
                } else {
                    return SeqMatchResult(false, sequenceResult)
                }
            }
        }

        fun matches(vararg tokens: Assembly.Token): SeqMatchResult {
            var trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Assembly.Token.Space) {
                        trimmedTokens.remove(token)
                    }
                }
            }

            if (trimmedTokens.size < sequenceComponents.size) {
                return SeqMatchResult(false, emptyList())
            }

            val sequenceResult = match(*trimmedTokens.toTypedArray())

            if (sequenceResult.size == sequenceComponents.size) {
                console.log(
                    "Grammar.Sequence.match(): found ${sequenceResult.joinToString("") { it.token.content }}"
                )
                return SeqMatchResult(true, sequenceResult)
            } else {
                return SeqMatchResult(false, sequenceResult)
            }

        }

        private fun match(vararg tokens: Assembly.Token): List<SeqMap> {
            val sequenceResult = mutableListOf<SeqMap>()
            for (component in sequenceComponents) {
                val index = sequenceComponents.indexOf(component)
                val token = tokens[index]


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
            return sequenceResult
        }

        data class SeqMap(val component: SequenceComponent, val token: Assembly.Token)

        data class SeqMatchResult(val matches: Boolean, val sequenceMap: List<SeqMap>)

        sealed class SequenceComponent {
            class Specific(val content: String) : SequenceComponent() {

            }

            sealed class InSpecific : SequenceComponent() {

                class Symbol : InSpecific()

                class Word : InSpecific()

                class AlphaNum : InSpecific()

                class Constant : InSpecific()

                class Register : InSpecific()

                class Space : InSpecific()

            }
        }
    }


    sealed class TreeNode(val name: String) {
        open class TokenNode(val hlFlag: String, name: String, vararg val tokens: Assembly.Token) : TreeNode(name)
        open class CollectionNode(name: String, vararg val tokenNodes: TokenNode) : TreeNode(name)
        open class SectionNode(name: String, vararg val collNodes: CollectionNode) : TreeNode(name)
    }

    data class Error(val treeNode: TreeNode, val message: String)

    class SyntaxSequence(vararg val components: Component) {

        fun exacltyMatches(vararg treeNodes: TreeNode): SyntaxSeqMatchResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode>()
            var valid = true
            var error: Error? = null

            var remainingTreeNodes = treeNodes.toMutableList()

            for (treeNode in treeNodes) {
                val treeNodeIndex = treeNodes.indexOf(treeNode)
                val component = components[syntaxIndex]
                if (component.treeNodeName == treeNode.name) {
                    matchingTreeNodes.add(treeNode)
                    remainingTreeNodes.remove(treeNode)
                } else {
                    valid = false
                    error = Grammar.Error(treeNode, "expecting ${component.treeNodeName} but found ${treeNode.name}")
                    break
                }

                // increase Syntax Index if next treeNode doesn't equal this sequence component type
                if (component.repeatable) {
                    if (treeNodeIndex + 1 < treeNodes.size) {
                        val nextTreeNode = treeNodes[treeNodeIndex + 1]
                        if (nextTreeNode.name != component.treeNodeName) {
                            if (syntaxIndex + 1 < components.size) {
                                syntaxIndex++
                            }
                        }
                    }
                } else {
                    if (syntaxIndex + 1 < components.size) {
                        syntaxIndex++
                    } else {
                        break
                    }
                }
            }
            if (remainingTreeNodes.isNotEmpty()) {
                valid = false
                error = Error(remainingTreeNodes.first(), "to many arguments for Sequence {${components.joinToString(" , ") { "${it.treeNodeName} repeatable: ${it.repeatable}" }}}!")
            }
            return SyntaxSeqMatchResult(valid, matchingTreeNodes, error, remainingTreeNodes)
        }

        fun matches(vararg treeNodes: TreeNode): SyntaxSeqMatchResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode>()
            var valid = true
            var error: Error? = null

            var remainingTreeNodes = treeNodes.toMutableList()

            for (treeNode in treeNodes) {
                val treeNodeIndex = treeNodes.indexOf(treeNode)
                val component = components[syntaxIndex]
                if (component.treeNodeName == treeNode.name) {
                    matchingTreeNodes.add(treeNode)
                    remainingTreeNodes.remove(treeNode)
                } else {
                    valid = false
                    error = Grammar.Error(treeNode, "expecting ${component.treeNodeName} but found ${treeNode.name}")
                    break
                }

                // increase Syntax Index if next treeNode doesn't equal this sequence component type
                if (component.repeatable) {
                    if (treeNodeIndex + 1 < treeNodes.size) {
                        val nextTreeNode = treeNodes[treeNodeIndex + 1]
                        if (nextTreeNode.name != component.treeNodeName) {
                            if (syntaxIndex + 1 < components.size) {
                                syntaxIndex++
                            } else {
                                break
                            }
                        }
                    }
                } else {
                    if (syntaxIndex + 1 < components.size) {
                        syntaxIndex++
                    } else {
                        break
                    }
                }
            }
            return SyntaxSeqMatchResult(valid, matchingTreeNodes, error, remainingTreeNodes)
        }

        data class Component(val treeNodeName: String, val repeatable: Boolean = false)

        data class SyntaxSeqMatchResult(val matches: Boolean, val matchingTreeNodes: List<TreeNode>, val error: Error? = null, val remainingTreeNodes: List<TreeNode>? = null)
    }


}