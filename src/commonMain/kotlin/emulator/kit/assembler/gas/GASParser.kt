package emulator.kit.assembler.gas

import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.CompilerInterface
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.*
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.ParserTree
import emulator.kit.optional.Feature

class GASParser(compiler: CompilerInterface, val definedAssembly: DefinedAssembly) : Parser(compiler) {
    override fun getDirs(features: List<Feature>): List<DirTypeInterface> = definedAssembly.getAdditionalDirectives() + GASDirType.entries
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = definedAssembly.getInstrs(features)
    override fun parse(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): ParserTree {
        // Preprocess and Filter Tokens
        val filteredSource = filter(source)

        // Build the tree
        val root = GASNode.buildNode(GASNodeType.ROOT, filteredSource, getDirs(features), definedAssembly)

        /**
         * SEMANTIC ANALYSIS
         * - LINK Providers with Receivers
         * - TYPE CHECKING
         */

        /**
         * Collect Providers
         */




        return ParserTree(root, source, filteredSource)
    }

    /**
     * Filter the [tokens] and Build up the List of Matching [GASElement]s
     */
    private fun filter(tokens: List<Token>): List<Token> {
        val remaining = tokens.toMutableList()
        val elements = mutableListOf<Token>()

        while (remaining.isNotEmpty()) {
            // Add Base Node if not found any special node
            val replaceWithWhitespace = when (remaining.first().type) {
                Token.Type.COMMENT_SL -> false
                Token.Type.COMMENT_ML -> false
                else -> true
            }

            if (replaceWithWhitespace) elements.add(remaining.removeFirst()) else {
                val old = remaining.removeFirst()
                elements.add(Token(Token.Type.WHITESPACE, old.lineLoc, " ", old.id))
            }
        }

        return elements
    }


}