package extendable.components.assembly

abstract class Grammar {


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
                    }
                }
            }

            return null
        }
    }

    sealed class TreeNode() {
        open class TokenNode(val hlFlag: String, vararg val tokens: Assembly.Token) : TreeNode()
        open class CollectionNode(vararg val tokenNodes: TokenNode) : TreeNode()
    }


}