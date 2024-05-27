package emulator.kit.assembler

import debug.DebugTools
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASNode.Companion.dropSpaces
import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.common.RegContainer
import emulator.kit.nativeLog

/**
 * Class representing a rule for the assembler, which matches components based on specified criteria.
 *
 * @param comp A lambda that returns a [Component] which defines the matching logic.
 */
class Rule(comp: () -> Component = { Component.Nothing }) {

    companion object {
        /**
         * Creates a rule that matches a directive name.
         *
         * @param name The directive name to match.
         * @return A [Rule] that matches the specified directive.
         */
        fun dirNameRule(name: String) = Rule {
            Component.Specific(".${name}", ignoreCase = true)
        }
    }

    private val comp = comp()

    /**
     * Attempts to match the rule against the start of the provided token source.
     *
     * @param source The list of tokens to match against.
     * @param allDirs The list of directive types available.
     * @param definedAssembly The defined assembly context.
     * @param assignedSymbols The list of assigned symbols in the current context.
     * @return A [MatchResult] indicating whether the match was successful and the details of the match.
     */
    fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
        val result = comp.matchStart(source, allDirs, definedAssembly, assignedSymbols)
        return result
    }

    /**
     * Returns a string representation of the rule.
     */
    override fun toString(): String = comp.print("")

    /**
     * Sealed class representing a component that can be matched by a rule.
     */
    sealed class Component {
        /**
         * Attempts to match the component against the start of the provided token source.
         *
         * @param source The list of tokens to match against.
         * @param allDirs The list of directive types available.
         * @param definedAssembly The defined assembly context.
         * @param assignedSymbols The list of assigned symbols in the current context.
         * @return A [MatchResult] indicating whether the match was successful and the details of the match.
         */
        abstract fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult

        /**
         * Returns a string representation of the component with the specified prefix.
         *
         * @param prefix The prefix to append to the string representation.
         */
        abstract fun print(prefix: String): String

        /**
         * A component that optionally matches another component.
         */
        class Optional(comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val result = comp.matchStart(source, allDirs, definedAssembly, assignedSymbols)
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Optional ${comp.print("")}")
                return MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
            }

            override fun print(prefix: String): String = "$prefix[${comp.print("")}]"
        }

        /**
         * A component that matches one of the provided components exclusively.
         */
        class XOR(private vararg val comps: Component) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {

                var result: MatchResult = MatchResult(false, listOf(), listOf(), source)
                for (comp in comps) {
                    result = comp.matchStart(source, allDirs, definedAssembly, assignedSymbols)
                    if (result.matches) {
                        if (DebugTools.KIT_showRuleChecks) nativeLog("Match: XOR ${result.matchingTokens.joinToString { it.type.name }}")
                        result = MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
                        return result
                    }
                }
                return result
            }

            override fun print(prefix: String): String = "$prefix${comps.joinToString("|") { it.print("") }}"
        }

        /**
         * A component that matches the same component repeatedly.
         *
         * @param ignoreSpaces Whether to ignore spaces between matches.
         * @param maxLength The maximum number of repetitions allowed.
         */
        class Repeatable(private val ignoreSpaces: Boolean = true, private val maxLength: Int? = null, comp: () -> Component) : Component() {
            private val comp = comp()
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val remainingTokens = source.toMutableList()
                val ignoredSpaces = mutableListOf<Token>()
                val matchingTokens = mutableListOf<Token>()
                val matchingNodes = mutableListOf<Node>()

                var iteration = 0
                if (ignoreSpaces) {
                    ignoredSpaces.addAll(remainingTokens.dropSpaces())
                }

                var result = comp.matchStart(remainingTokens, allDirs, definedAssembly, assignedSymbols)
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
                    result = comp.matchStart(remainingTokens, allDirs, definedAssembly, assignedSymbols)
                }

                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Repeatable ${comp.print("")} iterations: ${iteration + 1}")

                return MatchResult(true, matchingTokens, matchingNodes, remainingTokens, ignoredSpaces)
            }

            override fun print(prefix: String): String = "$prefix(vararg ${comp.print("")})"
        }

        /**
         * A component that matches a sequence of other components.
         *
         * @param comps The components to match in sequence.
         * @param ignoreSpaces Whether to ignore spaces between matches.
         */
        class Seq(vararg val comps: Component, private val ignoreSpaces: Boolean = true) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val ignoredSpaces = mutableListOf<Token>()
                val remaining = source.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                val matchingTokens = mutableListOf<Token>()

                for (comp in comps) {
                    if (ignoreSpaces) {
                        ignoredSpaces.addAll(remaining.dropSpaces())
                    }

                    val result = comp.matchStart(remaining, allDirs, definedAssembly, assignedSymbols)
                    if (!result.matches) {
                        //nativeLog("seq comp isn't matching: ${comp.print()} -> ${remaining.joinToString(" ") { "[${it.type.toString()} ${it.content}]" }}")
                        return MatchResult(false, listOf(), listOf(), source)
                    }

                    matchingNodes.addAll(result.matchingNodes)
                    matchingTokens.addAll(result.matchingTokens)
                    remaining.clear()
                    remaining.addAll(result.remainingTokens)
                }

                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Seq ${comps.joinToString { it.print("") }}")

                return MatchResult(true, matchingTokens, matchingNodes, remaining, ignoredSpaces)
            }

            override fun print(prefix: String): String = "$prefix${comps.joinToString(" ") { it.print("") }}"
        }

        /**
         * A component that matches if the specified component does not match.
         *
         * @param comp The component that should not match.
         */
        class Except(private val comp: Component) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                if (source.isEmpty()) return MatchResult(false, listOf(), listOf(), source)
                val result = comp.matchStart(source, allDirs, definedAssembly, assignedSymbols)
                if (result.matches) {
                    return MatchResult(false, listOf(), listOf(), source)
                }
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Except ${comp.print("")}")
                return MatchResult(true, listOf(source.first()), listOf(), source - source.first())
            }

            override fun print(prefix: String): String = "$prefix!${comp.print("")}"
        }

        /**
         * A component that matches a specific content string.
         *
         * @param content The string content to match.
         * @param ignoreCase Whether to ignore case when matching the content.
         */
        class Specific(val content: String, private val ignoreCase: Boolean = false) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (ignoreCase) {
                    if (first.content.uppercase() != content.uppercase()) return MatchResult(false, listOf(), listOf(), source)
                } else {
                    if (first.content != content) return MatchResult(false, listOf(), listOf(), source)
                }
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Specific $content -> $first")
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(prefix: String): String = "$prefix${content}"
        }

        /**
         * A component that matches a register token.
         *
         * @param inRegFile The register file that the register should belong to.
         * @param notInRegFile The register file that the register should not belong to.
         */
        class Reg(private val inRegFile: RegContainer.RegisterFile? = null, private val notInRegFile: RegContainer.RegisterFile? = null) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                val reg = first.reg
                if (first.type != Token.Type.REGISTER || reg == null) return MatchResult(false, listOf(), listOf(), source)

                if (inRegFile != null && !inRegFile.unsortedRegisters.contains(reg)) return MatchResult(false, listOf(), listOf(), source)

                if (notInRegFile != null && notInRegFile.unsortedRegisters.contains(reg)) return MatchResult(false, listOf(), listOf(), source)

                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Reg ${reg} -> ${first.content}")

                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(prefix: String): String = "$prefix reg${if (inRegFile != null) "in ${inRegFile.name}" else ""} and ${if (notInRegFile != null) "not in ${notInRegFile.name}" else ""}"

        }

        /**
         * A component that matches a directive token with the specified name.
         *
         * @param dirName The directive name to match.
         */
        class Dir(private val dirName: String) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type == Token.Type.DIRECTIVE && ".${dirName.uppercase()}" == first.content.uppercase()) {
                    if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Dir ${dirName}")
                    return MatchResult(true, listOf(first), listOf(), source - first)
                }
                return MatchResult(false, listOf(), listOf(), source)
            }

            override fun print(prefix: String): String = "$prefix.${dirName}"
        }

        /**
         * A component that matches a token of a specific type.
         *
         * @param type The token type to match.
         */
        class InSpecific(private val type: Token.Type) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val first = source.firstOrNull() ?: return MatchResult(false, listOf(), listOf(), source)
                if (first.type != type) return MatchResult(false, listOf(), listOf(), source)
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: InSpecific ${type.name}")
                return MatchResult(true, listOf(first), listOf(), source - first)
            }

            override fun print(prefix: String): String = "$prefix${type.name}"
        }

        /**
         * A component that matches a specific GAS node type.
         *
         * @param type The GAS node type to match.
         */
        class SpecNode(private val type: GASNodeType) : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                val node = GASNode.buildNode(type, source, allDirs, definedAssembly, assignedSymbols)
                if (node == null) {
                    if (DebugTools.KIT_showRuleChecks) nativeLog("Mismatch: SpecNode ${type.name}")
                    return MatchResult(false, listOf(), listOf(), source)
                }
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: SpecNode ${type.name}")
                return MatchResult(true, listOf(), listOf(node), source - node.tokens().toSet())
            }

            override fun print(prefix: String): String = "$prefix${type.name}"
        }

        /**
         * A component that matches nothing, always successful.
         */
        data object Nothing : Component() {
            override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol>): MatchResult {
                return MatchResult(true, listOf(), listOf(), source)
            }

            override fun print(prefix: String): String = "$prefix{}"
        }
    }

    /**
     * Data class representing the result of a match attempt.
     *
     * @param matches Whether the match was successful.
     * @param matchingTokens The tokens that were matched.
     * @param matchingNodes The nodes that were matched.
     * @param remainingTokens The remaining tokens after the match.
     * @param ignoredSpaces The tokens that were ignored as spaces.
     */
    data class MatchResult(val matches: Boolean, val matchingTokens: List<Token>, val matchingNodes: List<Node>, val remainingTokens: List<Token>, val ignoredSpaces: List<Token> = listOf()) {
        override fun toString(): String {
            return "Matches: $matches,${matchingTokens.joinToString("") { "\n\t${it::class.simpleName}" }}${matchingNodes.joinToString("") { it.print("\n\t") }}"
        }
    }
}