package emulator.kit.assembler.syntax

import debug.DebugTools
import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.gas.GASNode.Companion.dropSpaces
import emulator.kit.assembler.gas.GASNodeType
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.common.RegContainer
import emulator.kit.nativeLog

/**
 * Sealed class representing a component that can be matched by a rule.
 */
sealed class Component {
    /**
     * Attempts to match the component against the start of the provided token source.
     *
     * @param source The list of tokens to match against.
     * @param allDirs The list of directive types available.
     * @param asmHeader The defined assembly context.
     * @param assignedSymbols The list of assigned symbols in the current context.
     * @return A [MatchResult] indicating whether the match was successful and the details of the match.
     */
    abstract fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult

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
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val result = comp.matchStart(source, allDirs, asmHeader, assignedSymbols)
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Optional ${comp.print("")}")
            return Rule.MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
        }

        override fun print(prefix: String): String = "$prefix[${comp.print("")}]"
    }

    /**
     * A component that matches one of the provided components exclusively.
     */
    class XOR(private vararg val comps: Component) : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {

            var result: Rule.MatchResult = Rule.MatchResult(false, listOf(), listOf(), source)
            for (comp in comps) {
                result = comp.matchStart(source, allDirs, asmHeader, assignedSymbols)
                if (result.matches) {
                    if (DebugTools.KIT_showRuleChecks) nativeLog("Match: XOR ${result.matchingTokens.joinToString { it.type.name }}")
                    result = Rule.MatchResult(true, result.matchingTokens, result.matchingNodes, result.remainingTokens)
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
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val remainingTokens = source.toMutableList()
            val ignoredSpaces = mutableListOf<Token>()
            val matchingTokens = mutableListOf<Token>()
            val matchingNodes = mutableListOf<Node>()

            var iteration = 0
            if (ignoreSpaces) {
                ignoredSpaces.addAll(remainingTokens.dropSpaces())
            }

            var result = comp.matchStart(remainingTokens, allDirs, asmHeader, assignedSymbols)
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
                result = comp.matchStart(remainingTokens, allDirs, asmHeader, assignedSymbols)
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Repeatable ${comp.print("")} iterations: ${iteration + 1}")

            return Rule.MatchResult(true, matchingTokens, matchingNodes, remainingTokens, ignoredSpaces)
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
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val ignoredSpaces = mutableListOf<Token>()
            val remaining = source.toMutableList()
            val matchingNodes = mutableListOf<Node>()
            val matchingTokens = mutableListOf<Token>()

            for (comp in comps) {
                if (ignoreSpaces) {
                    ignoredSpaces.addAll(remaining.dropSpaces())
                }

                val result = comp.matchStart(remaining, allDirs, asmHeader, assignedSymbols)
                if (!result.matches) {
                    //nativeLog("seq comp isn't matching: ${comp.print()} -> ${remaining.joinToString(" ") { "[${it.type.toString()} ${it.content}]" }}")
                    return Rule.MatchResult(false, listOf(), listOf(), source)
                }

                matchingNodes.addAll(result.matchingNodes)
                matchingTokens.addAll(result.matchingTokens)
                remaining.clear()
                remaining.addAll(result.remainingTokens)
            }

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Seq ${comps.joinToString { it.print("") }}")

            return Rule.MatchResult(true, matchingTokens, matchingNodes, remaining, ignoredSpaces)
        }

        override fun print(prefix: String): String = "$prefix${comps.joinToString(" ") { it.print("") }}"
    }

    /**
     * A component that matches if the specified component does not match.
     *
     * @param comp The component that should not match.
     */
    class Except(private val comp: Component) : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            if (source.isEmpty()) return Rule.MatchResult(false, listOf(), listOf(), source)
            val result = comp.matchStart(source, allDirs, asmHeader, assignedSymbols)
            if (result.matches) {
                return Rule.MatchResult(false, listOf(), listOf(), source)
            }
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Except ${comp.print("")}")
            return Rule.MatchResult(true, listOf(source.first()), listOf(), source - source.first())
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
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val first = source.firstOrNull() ?: return Rule.MatchResult(false, listOf(), listOf(), source)
            if (ignoreCase) {
                if (first.content.uppercase() != content.uppercase()) return Rule.MatchResult(false, listOf(), listOf(), source)
            } else {
                if (first.content != content) return Rule.MatchResult(false, listOf(), listOf(), source)
            }
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Specific $content -> $first")
            return Rule.MatchResult(true, listOf(first), listOf(), source - first)
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
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val first = source.firstOrNull() ?: return Rule.MatchResult(false, listOf(), listOf(), source)
            val reg = first.reg
            if (first.type != Token.Type.REGISTER || reg == null) return Rule.MatchResult(false, listOf(), listOf(), source)

            if (inRegFile != null && !inRegFile.unsortedRegisters.contains(reg)) return Rule.MatchResult(false, listOf(), listOf(), source)

            if (notInRegFile != null && notInRegFile.unsortedRegisters.contains(reg)) return Rule.MatchResult(false, listOf(), listOf(), source)

            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Reg ${reg} -> ${first.content}")

            return Rule.MatchResult(true, listOf(first), listOf(), source - first)
        }

        override fun print(prefix: String): String = "$prefix reg${if (inRegFile != null) "in ${inRegFile.name}" else ""} and ${if (notInRegFile != null) "not in ${notInRegFile.name}" else ""}"

    }

    /**
     * A component that matches a directive token with the specified name.
     *
     * @param dirName The directive name to match.
     */
    class Dir(private val dirName: String) : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val first = source.firstOrNull() ?: return Rule.MatchResult(false, listOf(), listOf(), source)
            if (first.type == Token.Type.DIRECTIVE && ".${dirName.uppercase()}" == first.content.uppercase()) {
                if (DebugTools.KIT_showRuleChecks) nativeLog("Match: Dir ${dirName}")
                return Rule.MatchResult(true, listOf(first), listOf(), source - first)
            }
            return Rule.MatchResult(false, listOf(), listOf(), source)
        }

        override fun print(prefix: String): String = "$prefix.${dirName}"
    }

    /**
     * A component that matches a token of a specific type.
     *
     * @param type The token type to match.
     */
    class InSpecific(private val type: Token.Type) : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val first = source.firstOrNull() ?: return Rule.MatchResult(false, listOf(), listOf(), source)
            if (first.type != type) return Rule.MatchResult(false, listOf(), listOf(), source)
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: InSpecific ${type.name}")
            return Rule.MatchResult(true, listOf(first), listOf(), source - first)
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    /**
     * A component that matches a specific GAS node type.
     *
     * @param type The GAS node type to match.
     */
    class SpecNode(private val type: GASNodeType) : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            val node = GASNode.buildNode(type, source, allDirs, asmHeader, assignedSymbols)
            if (node == null) {
                if (DebugTools.KIT_showRuleChecks) nativeLog("Mismatch: SpecNode ${type.name}")
                return Rule.MatchResult(false, listOf(), listOf(), source)
            }
            if (DebugTools.KIT_showRuleChecks) nativeLog("Match: SpecNode ${type.name}")
            return Rule.MatchResult(true, listOf(), listOf(node), source - node.tokens().toSet())
        }

        override fun print(prefix: String): String = "$prefix${type.name}"
    }

    /**
     * A component that matches nothing, always successful.
     */
    data object Nothing : Component() {
        override fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): Rule.MatchResult {
            return Rule.MatchResult(true, listOf(), listOf(), source)
        }

        override fun print(prefix: String): String = "$prefix{}"
    }
}