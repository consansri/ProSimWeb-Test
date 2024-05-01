package emulator.kit.assembler.parser

import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import kotlin.reflect.KClass
import kotlin.reflect.cast

sealed class Node {
    abstract fun getAllTokens(): Array<out Token>
    abstract fun searchBaseNode(token: Token, prevPath: List<Node>): Parser.SearchResult?
    abstract fun <T : Node> filterNodes(nodeType: KClass<T>): List<T>
    abstract fun print(prefix: String): String
    abstract fun getLineLoc(): Token.LineLoc?

    override fun toString(): String = print("")

    class BaseNode(val token: Token) : Node() {
        init {
            token.removeSeverityIfError()
        }

        override fun getLineLoc(): Token.LineLoc = token.lineLoc
        override fun getAllTokens(): Array<out Token> {
            return arrayOf(token)
        }

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

    abstract class HNode(vararg childs: Node) : Node() {
        val children: MutableList<Node> = childs.toMutableList()
        private var severity: Severity? = null

        override fun print(prefix: String): String = "$prefix${this::class.simpleName}:${children.joinToString("") { "\n${it.print("$prefix\t")}" }}"

        override fun getLineLoc(): Token.LineLoc? {
            return getAllTokens().firstOrNull()?.lineLoc
        }

        fun addChilds(vararg childs: Node) {
            this.children.addAll(childs)
        }

        fun addChild(child: Node) {
            children.add(child)
        }

        fun removeChild(child: Node) {
            children.remove(child)
        }

        fun removeAllChildren() {
            children.clear()
        }

        override fun getAllTokens(): Array<out Token> {
            val tokens = mutableListOf<Token>()
            children.forEach {
                tokens.addAll(it.getAllTokens())
            }
            return tokens.toTypedArray()
        }

        override fun <T : Node> filterNodes(nodeType: KClass<T>): List<T> {
            if (nodeType.isInstance(this)) return listOf(nodeType.cast(this)) + children.flatMap { it.filterNodes(nodeType) }
            return children.flatMap { it.filterNodes(nodeType) }
        }

        override fun searchBaseNode(token: Token, prevPath: List<Node>): Parser.SearchResult? {
            children.forEach {
                val subResult = searchBaseNode(token, prevPath + this)
                if (subResult != null) {
                    return subResult
                }
            }
            return null
        }
    }
}