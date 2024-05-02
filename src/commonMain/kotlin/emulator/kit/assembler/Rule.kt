package emulator.kit.assembler

import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.Companion.dropSpaces
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.common.RegContainer
import emulator.kit.nativeLog

class Rule(comp: () -> Component = { Component.Nothing }) {

    companion object {
        fun dirNameRule(name: String) = Rule {
            Component.Specific(".${name}", ignoreCase = true)
        }
    }

    private val comp = comp()

    fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
        val result = comp.matchStart(source, allDirs, definedAssembly)
        val nameComp = if (comp is Component.Seq) {
            comp.comps.firstOrNull()
        } else null
        if (source.firstOrNull()?.content == ".set" && nameComp is Component.Specific && nameComp.content == ".set") {
            nativeLog("Rule: ${comp.print()}\nTokens: ${source.joinToString(" ") { "${it.lineLoc} $it" }}\nResult: ${result.matchingTokens.joinToString(" ") { it::class.simpleName.toString() }}")
        }
        return result
    }

    sealed class Component {
        abstract fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult
        abstract fun print(): String

        class Optional(comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val result = comp.matchStart(source, allDirs, definedAssembly)
                nativeLog("Match: Optional ${comp.print()}")
                return MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
            }

            override fun print(): String = "[opt:${comp.print()}]"
        }

        class XOR(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {

                var result: MatchResult = MatchResult(false, listOf(), listOf(), source)
                for (comp in comps) {
                    result = comp.matchStart(source, allDirs, definedAssembly)
                    if (result.matches) {
                        nativeLog("Match: XOR ${result.matchingTokens.joinToString { it.type.name }}")
                        result = MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
                        return result
                    }
                }
                return result
            }

            override fun print(): String = "[${comps.joinToString(" xor ") { it.print() }}]"
        }

        class Repeatable(private val ignoreSpaces: Boolean = true, private val maxLength: Int? = null, comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val remainingTokens = source.toMutableList()
                val ignoredSpaces = mutableListOf<Token>()
                val matchingTokens = mutableListOf<Token>()
                val matchingNodes = mutableListOf<Node>()

                var iteration = 0
                if (ignoreSpaces) {
                    ignoredSpaces.addAll(remainingTokens.dropSpaces())
                }

                var result = comp.matchStart(remainingTokens, allDirs, definedAssembly)
                while (result.matches) {
                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remainingTokens.clear()
                    remainingTokens.addAll(result.remainingTokens)

                    iteration++
                    if (maxLength != null && iteration >= maxLength) {
                        break
                    }

                    if (ignoreSpaces) {
                        ignoredSpaces.addAll(remainingTokens.dropSpaces())
                    }
                    result = comp.matchStart(remainingTokens, allDirs, definedAssembly)
                }

                nativeLog("Match: Repeatable ${comp.print()} iterations: ${iteration + 1}")

                return MatchResult(true, matchingTokens, matchingNodes, remainingTokens, ignoredSpaces)
            }

            override fun print(): String = "[repeatable:${comp.print()}]"
        }

        class Seq(vararg val comps: Component, private val ignoreSpaces: Boolean = true) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val ignoredSpaces = mutableListOf<Token>()
                val remaining = source.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                val matchingTokens = mutableListOf<Token>()

                for (comp in comps) {
                    if (ignoreSpaces) {
                        ignoredSpaces.addAll(remaining.dropSpaces())
                    }

                    val result = comp.matchStart(remaining, allDirs, definedAssembly)
                    if (!result.matches) {
                        //nativeLog("seq comp isn't matching: ${comp.print()} -> ${remaining.joinToString(" ") { "[${it.type.toString()} ${it.content}]" }}")
                        return MatchResult(false, listOf(), listOf(), source)
                    }

                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remaining.clear()
                    remaining.addAll(result.remainingTokens)
                }

                nativeLog("Match: Seq ${comps.joinToString { it.print() }}")

                return MatchResult(true, matchingTokens, matchingNodes, remaining, ignoredSpaces)
            }

            override fun print(): String = "[${comps.joinToString(" , ") { it.print() }}]"
        }

        class Except(private val comp: Component) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                if (source.isEmpty()) return MatchResult(false, listOf(), listOf(), source)
                val result = comp.matchStart(source, allDirs, definedAssembly)
                if (result.matches) {
                    return MatchResult(false, listOf(), listOf(), source)
                }
                nativeLog("Match: Except ${comp.print()}")
                return MatchResult(true, listOf(source.first()), listOf(), source - source.first())
            }

            override fun print(): String = "[not:${comp.print()}]"
        }

        class Specific(val content: String, private val ignoreCase: Boolean = false) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (ignoreCase) {
                    if (first.content.uppercase() != content.uppercase()) return MatchResult(false, listOf(), listOf(), source)
                } else {
                    if (first.content != content) return MatchResult(false, listOf(), listOf(), source)
                }
                nativeLog("Match: Specific $content -> $first")
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(): String = "[specific:${content}]"
        }

        class Reg(private val inRegFile: RegContainer.RegisterFile? = null, private val notInRegFile: RegContainer.RegisterFile? = null) : Component(){
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                val reg = first.reg
                if(first.type != Token.Type.REGISTER || reg == null) return MatchResult(false, listOf(), listOf(), source)

                if(inRegFile != null && !inRegFile.unsortedRegisters.contains(reg)) return MatchResult(false, listOf(), listOf(), source)

                if(notInRegFile != null && notInRegFile.unsortedRegisters.contains(reg)) return MatchResult(false, listOf(), listOf(), source)

                nativeLog("Match: Reg ${reg} -> ${first.content}")

                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(): String = "[reg: ${if(inRegFile != null) "in ${inRegFile.name}"  else ""} and ${if(notInRegFile != null) "not in ${notInRegFile.name}" else ""}]"

        }

        class Dir(private val dirName: String) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type == Token.Type.DIRECTIVE && ".${dirName.uppercase()}" == first.content.uppercase()) {
                    nativeLog("Match: Dir ${dirName}")
                    return MatchResult(true, listOf(first), listOf(), source - first)
                }
                return MatchResult(false, listOf(), listOf(), source)
            }

            override fun print(): String = "[dir:${dirName}]"
        }

        class InSpecific(private val type: Token.Type) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type != type) return MatchResult(false, listOf(), listOf(), source)
                nativeLog("Match: InSpecific ${type.name}")
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(): String = "[specific:${type}]"
        }

        class SpecNode(private val type: GASNodeType) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                val node = GASNode.buildNode(type, source, allDirs, definedAssembly)
                if (node == null) return MatchResult(false, listOf(), listOf(), source)
                nativeLog("Match: SpecNode ${type.name}")
                return MatchResult(true, listOf(), listOf(node), source - node.getAllTokens().toSet())
            }

            override fun print(): String = "[node:${type.name}]"
        }

        data object Nothing : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): MatchResult {
                return MatchResult(true, listOf(), listOf(), source)
            }

            override fun print(): String = "[]"
        }
    }

    data class MatchResult(val matches: Boolean, val matchingTokens: List<Token>, val matchingNodes: List<Node>, val remainingTokens: List<Token>, val ignoredSpaces: List<Token> = listOf()) {
        override fun toString(): String {
            return "Matches: $matches,${matchingTokens.joinToString("") { "\n\t${it::class.simpleName}" }}${matchingNodes.joinToString("") { it.print("\n\t") }}"
        }
    }
}