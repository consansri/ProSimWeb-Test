package emulator.kit.assembler.parser

import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token

/**
 * Represents the result of parsing, including the root node, source tokens, and relevant tokens in the parse tree.
 *
 * @property rootNode The root node of the parse tree.
 * @property source The list of source tokens.
 * @property treeRelevantTokens The relevant tokens in the parse tree.
 */
class TreeResult(val rootNode: GASNode.Root?, val source: List<Token>, val treeRelevantTokens: List<Token>) {

    /**
     * Checks if the parse tree contains the specified token.
     *
     * @param token The token to search for.
     * @return The search result containing information about the token if found, null otherwise.
     */
    fun contains(token: Token): Parser.SearchResult? = rootNode?.searchBaseNode(token, listOf())

    /**
     * Checks if the source tokens contain any errors.
     *
     * @return True if there are errors, false otherwise.
     */
    fun hasErrors(): Boolean {
        source.forEach {
            if (it.getMajorSeverity()?.type == Severity.Type.ERROR) return true
        }
        return false
    }

    /**
     * Checks if the source tokens contain any warnings.
     *
     * @return True if there are warnings, false otherwise.
     */
    fun hasWarnings(): Boolean{
        source.forEach {
            if(it.getMajorSeverity()?.type == Severity.Type.WARNING) return true
        }
        return false
    }

    /**
     * Generates a string containing error messages from the source tokens.
     *
     * @return A string with error messages, or null if no errors are present.
     */
    fun printError(): String? {
        return source.mapNotNull { it.printError() }.ifEmpty { return null }.joinToString("\n") { it }
    }

    /**
     * Generates a string containing warning messages from the source tokens.
     *
     * @return A string with warning messages, or null if no warnings are present.
     */
    fun printWarning(): String? {
        return source.mapNotNull { it.printWarning() }.ifEmpty { return null }.joinToString("\n") { it }
    }

    override fun toString(): String {
        return rootNode?.print("") ?: "[null]"
    }
}