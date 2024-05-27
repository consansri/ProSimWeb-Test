package emulator.kit.assembler.parser

import emulator.kit.assembler.lexer.Token
import kotlin.reflect.KClass
import kotlin.reflect.cast


/**
 * Represents a node in the parse tree.
 */
sealed class Node {
    /** Retrieves the content of the node as a string. */
    fun getContentAsString(): String = tokens().sortedBy { it.id }.joinToString("") { it.getContentAsString() }

    /**
     * Retrieves all tokens associated with the node.
     *
     * @return Array of tokens.
     */
    abstract fun tokens(): Array<out Token>

    abstract fun tokensIncludingReferences(): List<Token>

    /**
     * Searches for the base node containing the specified token.
     *
     * @param token The token to search for.
     * @param prevPath List of previous nodes visited in the search path.
     * @return The search result if the token is found, null otherwise.
     */
    abstract fun searchBaseNode(token: Token, prevPath: List<Node>): Parser.SearchResult?

    /**
     * Filters nodes based on the specified node type.
     *
     * @param nodeType The class representing the node type.
     * @return List of nodes of the specified type.
     */
    abstract fun <T : Node> filterNodes(nodeType: KClass<T>): List<T>

    /**
     * Generates a string representation of the node.
     *
     * @param prefix The prefix string to prepend to each line of the output.
     * @return The string representation of the node.
     */
    abstract fun print(prefix: String): String

    /** Retrieves the line location associated with the node. */
    abstract fun getLineLoc(): Token.LineLoc?
    override fun toString(): String = print("")

    /**
     * Represents the base node containing a single token.
     *
     * @property token The token associated with the base node.
     */
    class BaseNode(val token: Token) : Node() {
        init {
            token.removeSeverityIfError()
        }

        override fun getLineLoc(): Token.LineLoc = token.lineLoc

        override fun tokens(): Array<out Token> {
            return arrayOf(token)
        }

        override fun tokensIncludingReferences(): List<Token> = listOfNotNull(token, token.isPseudoOf)

        override fun searchBaseNode(token: Token, prevPath: List<Node>): Parser.SearchResult? {
            return if (token == token) {
                Parser.SearchResult(this, prevPath + this)
            } else {
                null
            }
        }

        override fun <T : Node> filterNodes(nodeType: KClass<T>): List<T> {
            if (nodeType.isInstance(this)) return listOf(nodeType.cast(this))
            return emptyList()
        }

        override fun print(prefix: String): String = prefix + if (token.content != "\n") token.content else ""
    }

    /** Represents a hierarchical node containing child nodes. */
    abstract class HNode(vararg childs: Node) : Node() {
        val children: MutableList<Node> = childs.toMutableList()

        /** Prints the name of the node with the specified prefix. */
        fun printNodeName(prefix: String): String = "$prefix${this::class.simpleName}:"

        /** Prints the child nodes with the specified prefix. */
        fun printChilds(prefix: String): String = children.joinToString("") { "\n${it.print("$prefix\t")}" }

        /** Adds child nodes at the specified index. */
        fun addChilds(index: Int, childs: List<Node>) {
            children.addAll(index, childs)
        }

        /** Adds multiple child nodes. */
        fun addChilds(vararg childs: Node) {
            children.addAll(childs)
        }

        /** Adds a child node at the specified index. */
        fun addChild(index: Int, child: Node) {
            children.add(index, child)
        }

        /** Adds a single child node. */
        fun addChild(child: Node) {
            children.add(child)
        }

        /** Removes a child node. */
        fun removeChild(child: Node) {
            children.remove(child)
        }

        /** Removes multiple child nodes. */
        fun removeChilds(childs: List<Node>) {
            children.removeAll(childs.toSet())
        }

        /** Removes all child nodes. */
        fun removeAllChildren() {
            children.clear()
        }

        override fun print(prefix: String): String = "${printNodeName(prefix)}${printChilds(prefix)}"

        override fun getLineLoc(): Token.LineLoc? {
            return tokens().firstOrNull()?.lineLoc
        }

        override fun tokens(): Array<out Token> {
            val tokens = mutableListOf<Token>()
            children.forEach {
                tokens.addAll(it.tokens())
            }
            return tokens.toTypedArray()
        }

        override fun tokensIncludingReferences(): List<Token> = tokens().flatMap { listOfNotNull(it, it.isPseudoOf) }

        override fun <T : Node> filterNodes(nodeType: KClass<T>): List<T> {
            if (nodeType.isInstance(this)) return listOf(nodeType.cast(this)) + children.flatMap { it.filterNodes(nodeType) }
            return children.flatMap { it.filterNodes(nodeType) }
        }

        override fun searchBaseNode(token: Token, prevPath: List<Node>): Parser.SearchResult? {
            children.forEach { _ ->
                val subResult = searchBaseNode(token, prevPath + this)
                if (subResult != null) {
                    return subResult
                }
            }
            return null
        }
    }
}