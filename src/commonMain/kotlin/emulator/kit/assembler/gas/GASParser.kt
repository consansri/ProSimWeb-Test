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
         * - Expand MACRO and IRP Directives
         *   Iterate over statements if
         *   - Definition add to definitions
         *   - Resolve Unmatched Statements
         *
         * - Resolve Absolute Symbol Values
         *   Iterate over statements if
         *   - Directive is symbol defining -> add to local list of symbols (Valid Types: Undefined, String, Integer, Reg)
         *   - Directive is symbol setting -> change setted value and type
         *   - Statement contains symbol as Operand in Expression -> set Operand Value to symbol value (throw error if invalid type)
         *
         *
         *
         * - Calculate Relative Label Addresses
         *
         *
         *
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
            val replaceWithSpace = when (remaining.first().type) {
                Token.Type.COMMENT_SL -> true
                Token.Type.COMMENT_ML -> true
                else -> false
            }

            if (!replaceWithSpace) elements.add(remaining.removeFirst()) else {
                val replacing = remaining.removeFirst()
                elements.add(Token(Token.Type.WHITESPACE, replacing.lineLoc, " ", replacing.id))
            }
        }

        // Remove Spaces between DIRECTIVE and LINEBREAK

        return elements
    }


}