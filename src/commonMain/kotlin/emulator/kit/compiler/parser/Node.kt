package emulator.kit.compiler.parser

import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token

sealed class Node {
    abstract fun getAllTokens(): Array<out Token>
    abstract fun searchTokenNode(token: Token, prevPath: List<Node>): Parser.SearchResult?
    abstract fun print(prefix: String): String

    class BaseNode(val token: Token) : Node() {
        override fun getAllTokens(): Array<out Token> {
            return arrayOf(token)
        }

        override fun searchTokenNode(token: Token, prevPath: List<Node>): Parser.SearchResult? {
            return if (token == token) {
                Parser.SearchResult(this, prevPath + this)
            } else {
                null
            }
        }

        override fun print(prefix: String): String = prefix + if(token.content != "\n") token.content else ""
    }

    abstract class HNode(vararg childs: Node) : Node() {
        val children: MutableList<Node> = childs.toMutableList()
        var severity: Severity? = null

        override fun print(prefix: String): String = "$prefix${this::class.simpleName}:${children.joinToString("") { "\n${it.print("$prefix\t")}" }}"

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

        fun addSeverity(severity: Severity) {
            this.severity = severity
        }

        override fun getAllTokens(): Array<out Token> {
            val tokens = mutableListOf<Token>()
            children.forEach {
                tokens.addAll(it.getAllTokens())
            }
            return tokens.toTypedArray()
        }

        override fun searchTokenNode(token: Token, prevPath: List<Node>): Parser.SearchResult? {
            children.forEach {
                val subResult = searchTokenNode(token, prevPath + this)
                if (subResult != null) {
                    return subResult
                }
            }
            return null
        }
    }
}