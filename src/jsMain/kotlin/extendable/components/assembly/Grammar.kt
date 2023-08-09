package extendable.components.assembly

import extendable.components.connected.FileHandler
import extendable.components.connected.Transcript

abstract class Grammar {

    abstract val applyStandardHLForRest: Boolean

    abstract fun clear()

    abstract fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): GrammarTree

    class GrammarTree(val rootNode: TreeNode.RootNode? = null) {
        fun contains(token: Compiler.Token): SearchResult? {
            rootNode?.let {
                return it.searchTokenNode(token, "")
            }

            return null
        }

        fun errorsContain(token: Compiler.Token): Boolean {
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

    class TokenSequence(vararg val components: Component, val ignoreSpaces: Boolean = false) {

        fun getLength(): Int {
            return components.size
        }

        fun exactlyMatches(vararg tokens: Compiler.Token): SeqMatchResult {
            val trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Compiler.Token.Space) {
                        trimmedTokens.remove(token)
                    }
                }
            }

            return if (components.size != trimmedTokens.size) {
                SeqMatchResult(false, emptyList())
            } else {
                val sequenceResult = match(*trimmedTokens.toTypedArray())

                if (sequenceResult.size == components.size) {
                    SeqMatchResult(true, sequenceResult)
                } else {
                    SeqMatchResult(false, sequenceResult)
                }
            }
        }

        fun matches(vararg tokens: Compiler.Token): SeqMatchResult {
            val trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Compiler.Token.Space) {
                        trimmedTokens.remove(token)
                    }
                }
            }

            if (trimmedTokens.size < components.size) {
                return SeqMatchResult(false, emptyList())
            }

            val sequenceResult = match(*trimmedTokens.toTypedArray())

            return if (sequenceResult.size == components.size) {
                SeqMatchResult(true, sequenceResult)
            } else {
                SeqMatchResult(false, sequenceResult)
            }

        }

        private fun match(vararg tokens: Compiler.Token): List<SeqMap> {
            val sequenceResult = mutableListOf<SeqMap>()
            for (component in components) {
                val index = components.indexOf(component)
                val token = tokens[index]


                when (component) {
                    is Component.InSpecific -> {
                        when (component) {
                            is Component.InSpecific.Symbol -> {
                                if (token is Compiler.Token.Symbol) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is Component.InSpecific.Word -> {
                                if (token is Compiler.Token.Word) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is Component.InSpecific.AlphaNum -> {
                                if (token is Compiler.Token.AlphaNum) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is Component.InSpecific.Constant -> {
                                if (token is Compiler.Token.Constant) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is Component.InSpecific.Register -> {
                                if (token is Compiler.Token.Register) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is Component.InSpecific.Space -> {
                                if (token is Compiler.Token.Space) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }
                        }
                    }

                    is Component.Specific -> {
                        if (token.content == component.content) {
                            sequenceResult.add(SeqMap(component, token))
                        }
                    }
                }
            }
            return sequenceResult
        }

        data class SeqMap(val component: Component, val token: Compiler.Token)

        data class SeqMatchResult(val matches: Boolean, val sequenceMap: List<SeqMap>)

        sealed class Component {
            class Specific(val content: String) : Component() {

            }

            sealed class InSpecific : Component() {

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
        abstract fun getAllTokens(): Array<out Compiler.Token>
        abstract fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult?
        open class ElementNode(val highlighting: ConnectedHL, name: String, vararg val tokens: Compiler.Token) : TreeNode(name) {
            override fun getAllTokens(): Array<out Compiler.Token> {
                return tokens
            }

            override fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult? {
                return if (tokens.contains(token)) {
                    SearchResult(this, "$prevPath/${this.name}")
                } else {
                    null
                }
            }
        }
        open class RowNode(name: String, vararg val nullableElementNodes: ElementNode?) : TreeNode(name) {
            val elementNodes: Array<out ElementNode>

            init {
                elementNodes = nullableElementNodes.toMutableList().filterNotNull().toTypedArray()
            }

            override fun getAllTokens(): Array<out Compiler.Token> {
                val tokens = mutableListOf<Compiler.Token>()
                nullableElementNodes.filterNotNull().forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }

            override fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult? {
                elementNodes.forEach {
                    val result = it.searchTokenNode(token, "$prevPath/${this.name}")
                    if (result != null) {
                        return result
                    }
                }
                return null
            }
        }
        open class SectionNode(name: String, vararg val collNodes: RowNode) : TreeNode(name) {
            override fun getAllTokens(): Array<out Compiler.Token> {
                val tokens = mutableListOf<Compiler.Token>()
                collNodes.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }

            override fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult? {
                collNodes.forEach {
                    val result = it.searchTokenNode(token, "$prevPath/${this.name}")
                    if (result != null) {
                        return result
                    }
                }
                return null
            }
        }
        open class ContainerNode(name: String, vararg val nodes: TreeNode) : TreeNode(name) {
            override fun getAllTokens(): Array<out Compiler.Token> {
                val tokens = mutableListOf<Compiler.Token>()
                nodes.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }

            override fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult? {
                nodes.forEach {
                    val result = it.searchTokenNode(token,"$prevPath/${this.name}")
                    if (result != null) {
                        return result
                    }
                }
                return null
            }
        }
        open class RootNode(allErrors: List<Error>, allWarnings: List<Warning>, vararg val containers: ContainerNode) : TreeNode("root") {

            val allErrors: List<Error>
            val allWarnings: List<Warning>

            init {
                if (allErrors.isNotEmpty()) {
                    this.allErrors = allErrors.sortedBy {
                        it.linkedTreeNode.getAllTokens().first().id
                    }.reversed()
                } else {
                    this.allErrors = emptyList()
                }

                if (allWarnings.isNotEmpty()) {
                    this.allWarnings = allWarnings.sortedBy { it.linkedTreeNode.getAllTokens().first().id }.reversed()
                } else {
                    this.allWarnings = emptyList()
                }


            }

            override fun getAllTokens(): Array<out Compiler.Token> {
                val tokens = mutableListOf<Compiler.Token>()
                allErrors.forEach { tokens.addAll(it.linkedTreeNode.getAllTokens() ?: emptyArray()) }
                containers.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }

            override fun searchTokenNode(token: Compiler.Token, prevPath: String): SearchResult? {
                containers.forEach {
                    val result = it.searchTokenNode(token,"$prevPath/${this.name}")
                    if (result != null) {
                        return result
                    }
                }
                return null
            }
        }
    }

    class Error(val message: String, val linkedTreeNode: TreeNode) {
        constructor(message: String, vararg tokens: Compiler.Token) : this(message, TreeNode.ElementNode(ConnectedHL(), "unidentified", *tokens))
        constructor(message: String, vararg elementNodes: TreeNode.ElementNode) : this(message, TreeNode.RowNode("unidentified", *elementNodes))
        constructor(message: String, vararg rowNodes: TreeNode.RowNode) : this(message, TreeNode.SectionNode("unidentified", *rowNodes))
        constructor(message: String, vararg nodes: TreeNode) : this(message, TreeNode.ContainerNode("unidentified", *nodes))
    }

    class Warning(val message: String, val linkedTreeNode: TreeNode) {
        constructor(message: String, vararg tokens: Compiler.Token) : this(message, TreeNode.ElementNode(ConnectedHL(), "unidentified", *tokens))
        constructor(message: String, vararg elementNodes: TreeNode.ElementNode) : this(message, TreeNode.RowNode("unidentified", *elementNodes))
        constructor(message: String, vararg rowNodes: TreeNode.RowNode) : this(message, TreeNode.SectionNode("unidentified", *rowNodes))
        constructor(message: String, vararg nodes: TreeNode) : this(message, TreeNode.ContainerNode("unidentified", *nodes))
    }

    class RowSeq(vararg val components: Component) {

        fun exacltyMatches(vararg elementNodes: TreeNode.ElementNode): RowSeqResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode.ElementNode>()
            var valid = true
            var error: Error? = null

            val remainingTreeNodes = elementNodes.toMutableList()

            if (elementNodes.isEmpty()) {
                return RowSeqResult(false, matchingTreeNodes, remainingTreeNodes = remainingTreeNodes)
            }

            for (treeNode in elementNodes) {

                val treeNodeIndex = elementNodes.indexOf(treeNode)
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
                    if (treeNodeIndex + 1 < elementNodes.size) {
                        val nextTreeNode = elementNodes[treeNodeIndex + 1]
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
                val tokens = mutableListOf<Compiler.Token>()
                remainingTreeNodes.forEach { tokens.addAll(it.getAllTokens()) }

                error = Grammar.Error(message = "to many arguments for Sequence {${components.joinToString(" , ") { "${it.treeNodeNames} repeatable: ${it.repeatable}" }}}!", *tokens.toTypedArray())
            }
            return RowSeqResult(valid, matchingTreeNodes, error, remainingTreeNodes)
        }

        fun matches(vararg treeNodes: TreeNode.ElementNode): RowSeqResult {
            var syntaxIndex = 0
            val matchingTreeNodes = mutableListOf<TreeNode.ElementNode>()
            var valid = true
            var error: Error? = null

            val remainingTreeNodes = treeNodes.toMutableList()

            if (treeNodes.isEmpty()) {
                return RowSeqResult(false, matchingTreeNodes, remainingTreeNodes = remainingTreeNodes)
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
            return RowSeqResult(valid, matchingTreeNodes, error, remainingTreeNodes)
        }

        class Component(vararg val treeNodeNames: String, val repeatable: Boolean = false)
        data class RowSeqResult(val matches: Boolean, val matchingTreeNodes: List<TreeNode.ElementNode>, val error: Error? = null, val remainingTreeNodes: List<TreeNode.ElementNode>? = null)
    }

    class ConnectedHL(vararg val hlPairs: Pair<String, List<Compiler.Token>>, val global: Boolean = false, val applyNothing: Boolean = false) {

        val hlTokenMap: MutableMap<String, MutableList<Compiler.Token>>

        init {
            hlTokenMap = mutableMapOf()
            for (hlPair in hlPairs) {
                val entry = hlTokenMap.get(hlPair.first)

                if (entry != null) {
                    entry.addAll(hlPair.second)
                } else {
                    hlTokenMap.set(hlPair.first, hlPair.second.toMutableList())
                }
            }


            if (global && hlTokenMap.size != 1) {
                console.warn("Grammar.TokenHL(): to many or less hlFlags!")
            }
        }

        constructor(hlFlag: String) : this(hlFlag to emptyList(), global = true)
        constructor() : this(applyNothing = true)

        fun getHLFlag(token: Compiler.Token): String? {
            if (!applyNothing) {
                if (global) {
                    return hlTokenMap.entries.first().key
                } else {
                    for (entry in hlTokenMap) {
                        if (entry.value.contains(token)) {
                            return entry.key
                        }
                    }
                    return null
                }
            } else {
                return null
            }
        }


    }

    data class SearchResult(val elementNode: TreeNode.ElementNode, val path: String = "")

}