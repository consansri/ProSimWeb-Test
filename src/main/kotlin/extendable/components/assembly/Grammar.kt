package extendable.components.assembly

abstract class Grammar {

    abstract val applyStandardHLForRest: Boolean

    abstract fun clear()

    abstract fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<Compiler.OtherFile>): GrammarTree

    class GrammarTree(val rootNode: TreeNode.RootNode? = null) {
        fun contains(token: Compiler.Token): TreeNode.ElementNode? {
            rootNode?.let {
                return it.searchTokenNode(token)
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

    class TokenSequence(vararg val sequenceComponents: SequenceComponent, val ignoreSpaces: Boolean = false) {

        fun getLength(): Int {
            return sequenceComponents.size
        }

        fun exactlyMatches(vararg tokens: Compiler.Token): SeqMatchResult {
            var trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Compiler.Token.Space) {
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

        fun matches(vararg tokens: Compiler.Token): SeqMatchResult {
            var trimmedTokens = tokens.toMutableList()
            if (ignoreSpaces) {
                for (token in trimmedTokens) {
                    if (token is Compiler.Token.Space) {
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

        private fun match(vararg tokens: Compiler.Token): List<SeqMap> {
            val sequenceResult = mutableListOf<SeqMap>()
            for (component in sequenceComponents) {
                val index = sequenceComponents.indexOf(component)
                val token = tokens[index]


                when (component) {
                    is SequenceComponent.InSpecific -> {
                        when (component) {
                            is SequenceComponent.InSpecific.Symbol -> {
                                if (token is Compiler.Token.Symbol) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Word -> {
                                if (token is Compiler.Token.Word) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.AlphaNum -> {
                                if (token is Compiler.Token.AlphaNum) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Constant -> {
                                if (token is Compiler.Token.Constant) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Register -> {
                                if (token is Compiler.Token.Register) {
                                    sequenceResult.add(SeqMap(component, token))
                                }
                            }

                            is SequenceComponent.InSpecific.Space -> {
                                if (token is Compiler.Token.Space) {
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

        data class SeqMap(val component: SequenceComponent, val token: Compiler.Token)

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
        abstract fun getAllTokens(): Array<out Compiler.Token>

        abstract fun searchTokenNode(token: Compiler.Token): ElementNode?

        open class ElementNode(val highlighting: ConnectedHL, name: String, vararg val tokens: Compiler.Token) : TreeNode(name) {
            override fun getAllTokens(): Array<out Compiler.Token> {
                return tokens
            }

            override fun searchTokenNode(token: Compiler.Token): ElementNode? {

                if (tokens.contains(token)) {
                    return this
                } else {
                    return null
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

            override fun searchTokenNode(token: Compiler.Token): ElementNode? {
                elementNodes.forEach {
                    val result = it.searchTokenNode(token)
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

            override fun searchTokenNode(token: Compiler.Token): ElementNode? {
                collNodes.forEach {
                    val result = it.searchTokenNode(token)
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

            override fun searchTokenNode(token: Compiler.Token): ElementNode? {
                nodes.forEach {
                    val result = it.searchTokenNode(token)
                    if (result != null) {
                        return result
                    }
                }
                return null
            }
        }

        open class RootNode(allErrors: List<Error>, allWarnings: List<Warning>, vararg val containers: ContainerNode) : TreeNode("name") {

            val allErrors: List<Error>
            val allWarnings: List<Warning>

            init {
                this.allErrors = allErrors.sortedBy { it.linkedTreeNode.getAllTokens().first().id }.reversed()
                this.allWarnings = allWarnings.sortedBy { it.linkedTreeNode.getAllTokens().first().id }.reversed()
            }

            override fun getAllTokens(): Array<out Compiler.Token> {
                val tokens = mutableListOf<Compiler.Token>()
                allErrors.forEach { tokens.addAll(it.linkedTreeNode.getAllTokens() ?: emptyArray()) }
                containers.forEach { tokens.addAll(it.getAllTokens()) }
                return tokens.toTypedArray()
            }

            override fun searchTokenNode(token: Compiler.Token): ElementNode? {
                containers.forEach {
                    val result = it.searchTokenNode(token)
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
                val tokens = mutableListOf<Compiler.Token>()
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

    class ConnectedHL(vararg val hlPairs: Pair<String, List<Compiler.Token>>, val global: Boolean = false, val applyNothing: Boolean = false) {

        val hlTokenMap: MutableMap<String, MutableList<Compiler.Token>>

        init {
            hlTokenMap = mutableMapOf()
            for (hlPair in hlPairs) {
                val entry = hlTokenMap.get(hlPair.first)

                if(entry != null){
                    entry.addAll(hlPair.second)
                }else{
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

}