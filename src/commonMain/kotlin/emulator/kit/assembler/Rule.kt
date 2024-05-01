package emulator.kit.assembler

import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.Companion.dropSpaces
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.nativeLog

class Rule(val ignoreSpace: Boolean = true, comp: () -> Component = { Component.Nothing }) {

    companion object{
        fun dirNameRule(name: String) = Rule{
            Component.Specific(".${name}", ignoreCase = true)
        }
    }

    private val comp = comp()

    fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
        val result = comp.matchStart(source, allDirs, definedAssembly, ignoreSpace)
        //nativeLog("Rule: ${comp.print()}\nResult: ${result.matchingTokens.joinToString(" ") { it::class.simpleName.toString() }}")
        return result
    }

    sealed class Component {
        abstract fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult
        abstract fun print(): String

        class Optional(comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val result = comp.matchStart(source, allDirs, definedAssembly, ignoreSpace)
                return MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
            }

            override fun print(): String = "[opt:${comp.print()}]"
        }

        class XOR(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val filteredSource = source.toMutableList()
                val spaces = if (ignoreSpace) {
                    filteredSource.dropSpaces()
                } else listOf()

                var result: MatchResult = MatchResult(false, listOf(), listOf(), source)
                for (comp in comps) {
                    result = comp.matchStart(filteredSource, allDirs, definedAssembly, ignoreSpace)
                    if (result.matches) {
                        result = MatchResult(true, result.matchingTokens + spaces, result.matchingNodes, result.remainingTokens)
                        return result
                    }
                }
                return result
            }

            override fun print(): String = "[${comps.joinToString(" xor ") { it.print() }}]"
        }

        class Repeatable(private val maxLength: Int? = null, comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val remainingTokens = source.toMutableList()
                val matchingTokens = mutableListOf<Token>()
                val matchingNodes = mutableListOf<Node>()

                var iteration = 0
                var result = comp.matchStart(remainingTokens, allDirs, definedAssembly, ignoreSpace)
                while (result.matches) {
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remainingTokens.clear()
                    remainingTokens.addAll(result.remainingTokens)

                    iteration++
                    if (maxLength != null && iteration >= maxLength) {
                        break
                    }

                    result = comp.matchStart(remainingTokens, allDirs, definedAssembly, ignoreSpace)
                }

                return MatchResult(true, matchingTokens, matchingNodes, remainingTokens)
            }

            override fun print(): String = "[repeatable:${comp.print()}]"
        }

        class Seq(private vararg val comps: Component,private val ignoreSpaces: Boolean? = null) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val remaining = source.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                val matchingTokens = mutableListOf<Token>()

                for (comp in comps) {
                    val result = comp.matchStart(remaining, allDirs, definedAssembly, this.ignoreSpaces ?: ignoreSpace)
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
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                if (source.isEmpty()) return MatchResult(false, listOf(), listOf(), source)
                val result = comp.matchStart(source, allDirs, definedAssembly, ignoreSpace)
                if (result.matches) {
                    return MatchResult(false, listOf(), listOf(), source)
                }
                return MatchResult(true, listOf(source.first()), listOf(), source - source.first())
            }

            override fun print(): String = "[not:${comp.print()}]"
        }

        class Specific(private val content: String, private val ignoreCase: Boolean = false) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val filteredSource = source.toMutableList()
                val spaces = if (ignoreSpace) {
                    filteredSource.dropSpaces()
                } else listOf()

                val first = filteredSource.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (ignoreCase) {
                    if (first.content.uppercase() != content.uppercase()) return MatchResult(false, listOf(), listOf(), source)
                } else {
                    if (first.content != content) return MatchResult(false, listOf(), listOf(), source)
                }
                return MatchResult(true, listOf(first) + spaces, listOf(), filteredSource - first)
            }

            override fun print(): String = "[specific:${content}]"
        }

        class Dir(private val dirName: String) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val filteredSource = source.toMutableList()
                val spaces = if (ignoreSpace) {
                    filteredSource.dropSpaces()
                } else listOf()

                val first = filteredSource.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type == Token.Type.DIRECTIVE && ".${dirName.uppercase()}" == first.content.uppercase()) {
                    return MatchResult(true, listOf(first) + spaces, listOf(), filteredSource - first)
                }
                return MatchResult(false, listOf(), listOf(), source)
            }

            override fun print(): String = "[dir:${dirName}]"
        }

        class InSpecific(private val type: Token.Type) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val filteredSource = source.toMutableList()
                val spaces = if (ignoreSpace) {
                    filteredSource.dropSpaces()
                } else listOf()

                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type != type) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(first) + spaces, listOf(), filteredSource - first)
            }

            override fun print(): String = "[specific:${type}]"
        }

        class SpecNode(private val type: GASNodeType) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
                val node = GASNode.buildNode(type, source, allDirs, definedAssembly)
                if (node == null) return MatchResult(false, listOf(), listOf(), source)
                return MatchResult(true, listOf(), listOf(node), source - node.getAllTokens().toSet())
            }

            override fun print(): String = "[node:${type.name}]"
        }

        data object Nothing : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, ignoreSpace: Boolean): MatchResult {
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

    private fun MutableList<Token>.dropSpaces(): List<Token> {
        val dropped = mutableListOf<Token>()
        while (this.isNotEmpty()) {
            if (this.first().type != Token.Type.WHITESPACE) break
            dropped.add(this.removeFirst())
        }
        return dropped
    }

}