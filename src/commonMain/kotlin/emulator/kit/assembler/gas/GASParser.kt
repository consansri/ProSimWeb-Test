package emulator.kit.assembler.gas

import emulator.Link
import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.CompilerInterface
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.*
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
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
        if (root == null || root !is Root) return ParserTree(null, source, filteredSource)

        // Filter
        root.removeEmptyStatements()

        /**
         * SEMANTIC ANALYSIS
         * - Resolve Directive Statements
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
         * - Fix Instruction ParamType and WordSizes
         *
         * - Calculate Relative Label Addresses
         *
         */

        /**
         * - Resolve Statements
         */

        val tempContainer = TempContainer(root)

        while (root.getAllStatements().isNotEmpty()) {
            val firstStatement = root.getAllStatements().first()
            when (firstStatement) {
                is Statement.Dir -> {
                    firstStatement.directive.type.executeDirective(firstStatement, tempContainer)
                }

                is Statement.Empty -> TODO()
                is Statement.Instr -> TODO()
                is Statement.Unresolved -> TODO()
            }

            root.removeChild(firstStatement)
        }


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
                Token.Type.COMMENT_NATIVE -> true
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

    data class TempContainer(
        val root: Root,
        val symbols: MutableList<Symbol> = mutableListOf(),
        val sections: MutableList<Section> = mutableListOf(Section("text"), Section("data"), Section("bss")),
        val macros: MutableList<Macro> = mutableListOf(),
        var currSection: Section = sections.first(),
    )

    data class Macro(val name: String, val arguments: List<Argument>, val content: List<Statement>) {
        fun generatePseudoStatements(lexer: Lexer, lineLoc: Token.LineLoc, argMap: List<ArgDef>): List<Token> {
            var content = content.map { it.contentBackToString() }.joinToString("") { it }
            val args = arguments.map { it.argName.content to it.getDefaultValue() }.toTypedArray()

            // Check for mixture of positional and indexed arguments
            argMap.forEach { def ->
                when (def) {
                    is ArgDef.KeyWord -> {
                        val argID = args.indexOfFirst { it.first == def.keyWord }
                        args[argID] = args[argID].first to def.content
                    }

                    is ArgDef.Positional -> {
                        args[def.position] = args[def.position].first to def.content
                    }
                }
            }

            args.forEach {
                content = content.replace("\\${it.first}", it.second)
            }

            return lexer.pseudoTokenize(lineLoc, content)
        }

        sealed class ArgDef(val content: String) {
            class Positional(token: Token, content: String, val position: Int) : ArgDef(content)
            class KeyWord(token: Token, content: String, val keyWord: String) : ArgDef(content)

        }
    }

    sealed class Symbol(val name: String) {
        class Undefined(name: String) : Symbol(name)
        class StringExpr(name: String, val expr: GASNode.StringExpr) : Symbol(name)
        class IntegerExpr(name: String, val expr: GASNode.NumericExpr) : Symbol(name)
        class TokenRef(name: String, val token: Token) : Symbol(name)
    }

    data class Section(val name: String, val statements: MutableList<Statement> = mutableListOf())

}