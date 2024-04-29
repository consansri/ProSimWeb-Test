package emulator.kit.compiler

import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import kotlin.reflect.KClass

class Rule(comp: () -> Component = { Component.Nothing }) {

    private val comp = comp()

    fun matchStart(source: List<Token>): MatchResult {
        return comp.matchStart(source)
    }

    sealed class Component {
        abstract fun matchStart(source: List<Token>): MatchResult

        class Optional(comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>): MatchResult {
                val result = comp.matchStart(source)
                return MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
            }
        }

        class XOR(vararg comps: () -> Component) : Component() {
            private val comps = comps.map { it() }
            override fun matchStart(source: List<Token>): MatchResult {
                var result: MatchResult = MatchResult(false, listOf(), listOf(), source)
                for (comp in comps) {
                    result = comp.matchStart(source)
                    if (result.matches) {
                        return result
                    }
                }
                return result
            }
        }

        class Repeatable(private val maxLength: Int? = null, comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>): MatchResult {
                val remainingTokens = source.toMutableList()
                val matchingTokens = mutableListOf<Token>()
                val matchingNodes = mutableListOf<Node>()

                var iteration = 0
                var result = comp.matchStart(remainingTokens)
                while (result.matches) {
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remainingTokens.clear()
                    remainingTokens.addAll(result.remainingTokens)

                    iteration++
                    if (maxLength != null && iteration >= maxLength) {
                        break
                    }

                    result = comp.matchStart(remainingTokens)
                }

                return MatchResult(true, matchingTokens, matchingNodes, remainingTokens)
            }
        }

        class Seq(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                val remaining = source.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                val matchingTokens = mutableListOf<Token>()

                for (comp in comps) {
                    val result = comp.matchStart(remaining)
                    if (!result.matches) return MatchResult(false, listOf(), listOf(), source)
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remaining.clear()
                    remaining.addAll(result.remainingTokens)
                }

                return MatchResult(true, matchingTokens, matchingNodes, remaining)
            }
        }

        class Except(private val comp: Component) : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                if (source.isEmpty()) return MatchResult(false, listOf(), listOf(), source)
                val result = comp.matchStart(source)
                if (result.matches) {
                    return MatchResult(false, listOf(), listOf(), source)
                }
                return MatchResult(true, listOf(source.first()), listOf(), source - source.first())
            }
        }

        class Specific(private val content: String) : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.content != content) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(first), listOf(), source - first)
            }
        }

        class InSpecific<T : Token>(private val type: KClass<T>) : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (type.isInstance(first)) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(first), listOf(), source - first)
            }
        }

        data object Expression : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                val node = GASNode.Expression.parse(source)
                if (node == null) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(), listOf(node), source - node.getAllTokens().toSet())
            }
        }

        data object Nothing : Component() {
            override fun matchStart(source: List<Token>): MatchResult {
                return MatchResult(true, listOf(), listOf(), source)
            }
        }
    }

    data class MatchResult(val matches: Boolean, val matchingTokens: List<Token>, val matchingNodes: List<Node>, val remainingTokens: List<Token>)

}