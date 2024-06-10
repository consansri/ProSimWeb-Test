package emulator.kit.assembler.syntax

import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node

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
     * @param asmHeader The defined assembly context.
     * @param assignedSymbols The list of assigned symbols in the current context.
     * @return A [MatchResult] indicating whether the match was successful and the details of the match.
     */
    fun matchStart(source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol>): MatchResult {
        val result = comp.matchStart(source, allDirs, asmHeader, assignedSymbols)
        return result
    }

    /**
     * Returns a string representation of the rule.
     */
    override fun toString(): String = comp.print("")


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