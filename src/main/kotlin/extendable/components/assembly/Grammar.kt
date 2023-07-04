package extendable.components.assembly

import emotion.css.keyframes
import extendable.ArchConst
import extendable.Architecture

abstract class Grammar {

    abstract val applyStandardHLForRest: Boolean

    abstract fun clear()

    abstract fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree

    class GrammarTree(val rootNode: TreeNode.RootNode? = null) {
        fun contains(token: Assembly.Token): TreeNode.TokenNode? {
            rootNode?.let {
                for (comment in it.allComments.tokenNodes) {
                    if (comment.tokens.contains(token)) {
                        return comment
                    }
                }

                for (section in it.sections) {
                    for (collection in section.collNodes) {
                        for (tokenNode in collection.tokenNodes) {
                            if (tokenNode.tokens.contains(token)) {
                                return tokenNode
                            }
                        }
                    }
                }
            }

            return null
        }

        fun errorsContain(token: Assembly.Token): Boolean {
            rootNode?.let {
                for (error in rootNode.allErrors) {
                    if (error.linkedTreeNode.getAllTokens().contains(token)) {
                        return true
                    }
                }
            }

            return false
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
        abstract fun getAllTokens(): Array<out Assembly.Token>

        open class TokenNode(val hlFlag: String, name: String, vararg val tokens: Assembly.Token) : TreeNode(name) {
            override fun getAllTokens(): Array<out Assembly.Token> {
                return tokens
            }
        }

        open class CollectionNode(name: String, vararg val nullableTokenNodes: TokenNode?) : TreeNode(name) {
            val tokenNodes: Array<out TokenNode>

            init {
                tokenNodes = nullableTokenNodes.toMutableList().filterNotNull().toTypedArray()
            }

            override fun getAllTokens(): Array<out Assembly.Token> {
                val tokens = mutableListOf<Assembly.Token>()
                nullableTokenNodes.filterNotNull().forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }
        }

        open class SectionNode(name: String, vararg val collNodes: CollectionNode) : TreeNode(name) {
            override fun getAllTokens(): Array<out Assembly.Token> {
                val tokens = mutableListOf<Assembly.Token>()
                collNodes.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }
        }

        open class FlexibleNode(name: String, vararg val nodes: TreeNode) : TreeNode(name) {
            override fun getAllTokens(): Array<out Assembly.Token> {
                val tokens = mutableListOf<Assembly.Token>()
                nodes.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }
        }

        open class RootNode(name: String, val allComments: CollectionNode, allErrors: List<Error>, vararg val sections: SectionNode) : TreeNode(name) {

            val allErrors: List<Error>

            init {
                this.allErrors = allErrors.sortedBy { it.linkedTreeNode.getAllTokens().first().id }.reversed()
            }

            override fun getAllTokens(): Array<out Assembly.Token> {
                val tokens = mutableListOf<Assembly.Token>()
                tokens.addAll(allComments.getAllTokens())
                allErrors.forEach { tokens.addAll(it.linkedTreeNode.getAllTokens() ?: emptyArray()) }
                sections.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }
        }
    }

    class Error(val message: String, val linkedTreeNode: TreeNode) {
        constructor(message: String, vararg tokens: Assembly.Token) : this(message, TreeNode.TokenNode("", "unidentified", *tokens))

        constructor(message: String, vararg tokenNodes: TreeNode.TokenNode) : this(message, TreeNode.CollectionNode("unidentified", *tokenNodes))

        constructor(message: String, vararg collectionNodes: TreeNode.CollectionNode) : this(message, TreeNode.SectionNode("unidentified", *collectionNodes))

        constructor(message: String, vararg nodes: TreeNode) : this(message, TreeNode.FlexibleNode("unidentified", *nodes))

    }

    class SyntaxSequence(vararg val components: Component) {

        fun exacltyMatches(vararg treeNodes: TreeNode): SyntaxSeqMatchResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode>()
            var valid = true
            var error: Error? = null

            var remainingTreeNodes = treeNodes.toMutableList()

            if (treeNodes.isEmpty()) {
                return SyntaxSeqMatchResult(false, matchingTreeNodes, remainingTreeNodes = remainingTreeNodes)
            }

            for (treeNode in treeNodes) {

                val treeNodeIndex = treeNodes.indexOf(treeNode)
                val component = components[syntaxIndex]
                if (component.treeNodeNames.contains(treeNode.name)) {
                    matchingTreeNodes.add(treeNode)
                    remainingTreeNodes.remove(treeNode)
                } else {
                    valid = false
                    error = Grammar.Error(message = "expecting ${component.treeNodeNames} but found ${treeNode.name}", linkedTreeNode = treeNode)
                    break
                }

                // increase Syntax Index if next treeNode doesn't equal this sequence component type
                if (component.repeatable) {
                    if (treeNodeIndex + 1 < treeNodes.size) {
                        val nextTreeNode = treeNodes[treeNodeIndex + 1]
                        if (!component.treeNodeNames.contains(nextTreeNode.name)) {
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
                val tokens = mutableListOf<Assembly.Token>()
                remainingTreeNodes.forEach { tokens.addAll(it.getAllTokens()) }

                error = Grammar.Error(message = "to many arguments for Sequence {${components.joinToString(" , ") { "${it.treeNodeNames} repeatable: ${it.repeatable}" }}}!", *tokens.toTypedArray())
            }
            return SyntaxSeqMatchResult(valid, matchingTreeNodes, error, remainingTreeNodes)
        }

        fun matches(vararg treeNodes: TreeNode): SyntaxSeqMatchResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode>()
            var valid = true
            var error: Error? = null

            var remainingTreeNodes = treeNodes.toMutableList()

            if (treeNodes.isEmpty()) {
                return SyntaxSeqMatchResult(false, matchingTreeNodes, remainingTreeNodes = remainingTreeNodes)
            }

            for (treeNode in treeNodes) {
                val treeNodeIndex = treeNodes.indexOf(treeNode)
                val component = components[syntaxIndex]
                if (component.treeNodeNames.contains(treeNode.name)) {
                    matchingTreeNodes.add(treeNode)
                    remainingTreeNodes.remove(treeNode)
                } else {
                    valid = false
                    error = Grammar.Error(message = "expecting ${component.treeNodeNames} but found ${treeNode.name}", linkedTreeNode = treeNode)
                    break
                }

                // increase Syntax Index if next treeNode doesn't equal this sequence component type
                if (component.repeatable) {
                    if (treeNodeIndex + 1 < treeNodes.size) {
                        val nextTreeNode = treeNodes[treeNodeIndex + 1]
                        if (!component.treeNodeNames.contains(nextTreeNode.name)) {
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

        class Component(vararg val treeNodeNames: String, val repeatable: Boolean = false)


        data class SyntaxSeqMatchResult(val matches: Boolean, val matchingTreeNodes: List<TreeNode>, val error: Error? = null, val remainingTreeNodes: List<TreeNode>? = null)
    }


}