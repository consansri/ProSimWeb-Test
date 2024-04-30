package emulator.kit.compiler

import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.gas.nodes.GASNodeType
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import emulator.kit.nativeLog
import kotlin.reflect.KClass

class Rule(comp: () -> Component = { Component.Nothing }) {

    private val comp = comp()

    fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
        val result = comp.matchStart(source, definedAssembly)
        //nativeLog("Rule: ${comp.print()}\nPerform on: ${source.joinToString(" ") { it::class.simpleName.toString() }}\nResult: $result")
        return result
    }

    sealed class Component {
        abstract fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult
        abstract fun print(): String

        class Optional(comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val result = comp.matchStart(source,definedAssembly)
                return MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
            }

            override fun print(): String = "[opt:${comp.print()}]"
        }

        class XOR(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                var result: MatchResult = MatchResult(false, listOf(), listOf(), source)
                for (comp in comps) {
                    result = comp.matchStart(source, definedAssembly)
                    if (result.matches) {
                        return result
                    }
                }
                return result
            }

            override fun print(): String = "[${comps.joinToString(" xor ") { it.print() }}]"
        }

        class Repeatable(private val maxLength: Int? = null, comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val remainingTokens = source.toMutableList()
                val matchingTokens = mutableListOf<Token>()
                val matchingNodes = mutableListOf<Node>()

                var iteration = 0
                var result = comp.matchStart(remainingTokens,definedAssembly)
                while (result.matches) {
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remainingTokens.clear()
                    remainingTokens.addAll(result.remainingTokens)

                    iteration++
                    if (maxLength != null && iteration >= maxLength) {
                        break
                    }

                    result = comp.matchStart(remainingTokens,definedAssembly)
                }

                return MatchResult(true, matchingTokens, matchingNodes, remainingTokens)
            }

            override fun print(): String = "[repeatable:${comp.print()}]"
        }

        class Seq(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val remaining = source.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                val matchingTokens = mutableListOf<Token>()

                for (comp in comps) {
                    val result = comp.matchStart(remaining,definedAssembly)
                    if (!result.matches) return MatchResult(false, listOf(), listOf(), source)
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remaining.clear()
                    remaining.addAll(result.remainingTokens)
                }

                return MatchResult(true, matchingTokens, matchingNodes, remaining)
            }

            override fun print(): String = "[${comps.joinToString(" , ") { it.print() }}]"
        }

        class Except(private val comp: Component) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                if (source.isEmpty()) return MatchResult(false, listOf(), listOf(), source)
                val result = comp.matchStart(source,definedAssembly)
                if (result.matches) {
                    return MatchResult(false, listOf(), listOf(), source)
                }
                return MatchResult(true, listOf(source.first()), listOf(), source - source.first())
            }

            override fun print(): String = "[not:${comp.print()}]"
        }

        class Specific(private val content: String, private val ignoreCase: Boolean = false) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (ignoreCase) {
                    if (first.content.uppercase() != content.uppercase()) return MatchResult(false, listOf(), listOf(), source)
                } else {
                    if (first.content != content) return MatchResult(false, listOf(), listOf(), source)
                }
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(): String = "[specific:${content}]"
        }

        class Dir(private val dirName: String) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                nativeLog("Dir check if (${dirName}) matches: ${first.content}")
                if (first is Token.KEYWORD.Directive && first.content.uppercase() == dirName.uppercase()) {
                    return MatchResult(true, listOf(first), listOf(), source - first)
                }
                return MatchResult(false, listOf(), listOf(), source)
            }

            override fun print(): String = "[dir:${dirName}]"
        }

        class InSpecific<T : Token>(private val type: KClass<T>) : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (!type.isInstance(first)) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(): String = "[specific:${type.simpleName}]"
        }

        class SpecNode(private val type: GASNodeType) : Component(){
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                val node = GASNode.buildNode(type,source,definedAssembly)
                if (node == null) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(), listOf(node), source - node.getAllTokens().toSet())
            }

            override fun print(): String = "[node:${type.name}]"
        }

        data object Nothing : Component() {
            override fun matchStart(source: List<Token>, definedAssembly: DefinedAssembly): MatchResult {
                return MatchResult(true, listOf(), listOf(), source)
            }

            override fun print(): String = "[]"
        }
    }

    data class MatchResult(val matches: Boolean, val matchingTokens: List<Token>, val matchingNodes: List<Node>, val remainingTokens: List<Token>) {
        override fun toString(): String {
            return "Matches: $matches,${matchingTokens.joinToString("") { "\n\t${it::class.simpleName}" }}${matchingNodes.joinToString("") { it.print("\n\t") }}"
        }
    }

}