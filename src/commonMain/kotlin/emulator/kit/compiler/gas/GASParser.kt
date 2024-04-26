package emulator.kit.compiler.gas

import emulator.kit.compiler.CompilerFile
import emulator.kit.compiler.CompilerInterface
import emulator.kit.compiler.DirTypeInterface
import emulator.kit.compiler.InstrTypeInterface
import emulator.kit.compiler.gas.nodes.GASNode
import emulator.kit.compiler.gas.nodes.GASNode.*
import emulator.kit.compiler.gas.nodes.GASNodeType
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Parser
import emulator.kit.compiler.parser.ParserTree
import emulator.kit.nativeLog
import emulator.kit.optional.Feature

class GASParser(compiler: CompilerInterface, val definedAssembly: DefinedAssembly) : Parser(compiler) {
    override fun getDirs(features: List<Feature>): List<DirTypeInterface> = definedAssembly.getAdditionalDirectives() + GASDirType.entries
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = definedAssembly.getInstrs(features)
    override fun parse(tokens: List<Token>, others: List<CompilerFile>): ParserTree {
        // Preprocess and Filter Tokens
        nativeLog("GASParser: Build Parser Base from ${tokens.joinToString("") {"\n\t${it::class.simpleName.toString()}: ${it.content}"  }}")
        val source = filter(tokens)
        nativeLog("GASParser: Parser Base: ${source.joinToString("") { "\n\t${it::class.simpleName.toString()}: ${it.content}"  }}")

        val root = GASNode.buildNode(GASNodeType.ROOT, source, definedAssembly)


        // PRE EQUS, MACROS


        // EXPRESSIONS

        /**
         * SEMANTIC ANALYSIS
         * - LINKING
         * - TYPE CHECKING
         */


        return ParserTree(root, tokens)
    }

    /**
     * Filter the [tokens] and Build up the List of Matching [GASElement]s
     */
    private fun filter(tokens: List<Token>): List<Token> {
        val remaining = tokens.toMutableList()
        val elements = mutableListOf<Token>()

        while (remaining.isNotEmpty()) {
            // Add Base Node if not found any special node
            val shouldAdd = when (remaining.first()) {
                is Token.COMMENT -> false
                is Token.SPACE -> false
                is Token.LINEBREAK -> true
                is Token.ANYCHAR -> true
                is Token.ERROR -> true
                is Token.KEYWORD.InstrName -> true
                is Token.KEYWORD.Register -> true
                is Token.KEYWORD.Directive -> true
                is Token.LITERAL.CHARACTER.CHAR -> true
                is Token.LITERAL.CHARACTER.STRING -> true
                is Token.LITERAL.NUMBER.INTEGER -> true
                is Token.OPERATOR -> true
                is Token.PUNCTUATION -> true
                is Token.SYMBOL -> true
                is Token.LABEL.Basic -> true
                is Token.LABEL.Local -> true
            }
            if (shouldAdd) elements.add(remaining.removeFirst()) else remaining.removeFirst()
        }

        return elements
    }


}