package cengine.lang.asm.ast.gas

import cengine.editor.annotation.Notation
import cengine.editor.widgets.Widget
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.InstrTypeInterface
import cengine.lang.asm.ast.gas.GASNode.*
import cengine.lang.asm.lexer.AsmLexer
import cengine.lang.asm.lexer.AsmToken
import cengine.lang.asm.lexer.AsmTokenType
import cengine.lang.asm.parser.Component.*
import cengine.lang.asm.parser.Rule
import cengine.psi.core.*
import cengine.psi.lexer.core.Token
import debug.DebugTools
import emulator.core.Size
import emulator.core.Value
import emulator.kit.nativeLog

/**
 * Node Relatives
 * --------------------
 * 1. [Program]
 *   - [Statement]
 * --------------------
 * 2. [Statement]
 *   Each Statement can contain an optional [Label]
 *
 *   - [Statement.Empty] No Content
 *   - [Statement.Dir] Content determined by [DirTypeInterface]
 *   - [Statement.Instr] Content determined by [InstrTypeInterface]
 *   - [Statement.Unresolved] Unresolved Content
 * --------------------
 */
sealed class GASNode(override var range: IntRange, vararg children: GASNode) : PsiElement, PsiFormatter {
    override var parent: PsiElement? = null

    final override val children: MutableList<GASNode> = mutableListOf(*children)
    final override val notations: MutableList<Notation> = mutableListOf()
    override val inlayWidgets: MutableList<Widget> = mutableListOf()
    override val interlineWidgets: MutableList<Widget> = mutableListOf()

    override val additionalInfo: String
        get() = ""

    open fun getCodeStyle(position: Int): CodeStyle? {
        return children.firstOrNull {
            position in it.range
        }?.getCodeStyle(position)
    }

    override fun accept(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
    }

    class Comment(val token: AsmToken) : GASNode(token.range) {
        override val pathName: String
            get() = PATHNAME

        companion object {
            const val PATHNAME = "COMMENT"
        }

        override fun getFormatted(identSize: Int): String = token.value
    }

    class Error(val token: AsmToken) : GASNode(token.range) {
        override val pathName: String
            get() = PATHNAME

        companion object {
            const val PATHNAME = "COMMENT"
        }

        init {
            notations.add(Notation.error(this, "Unexpected $token!"))
        }

        override fun getFormatted(identSize: Int): String = token.value
    }

    companion object {
        /**
         * Severities will be set by the Lowest Node, which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, lexer: AsmLexer, asmSpec: AsmSpec): GASNode? {
            val initialPos = lexer.position

            when (gasNodeType) {
                GASNodeType.PROGRAM -> {
                    val statements = mutableListOf<Statement>()
                    val annotations = mutableListOf<Notation>()

                    while (lexer.hasMoreTokens()) {
                        val node = buildNode(GASNodeType.STATEMENT, lexer, asmSpec)

                        if (node == null) {
                            val token = lexer.consume(true)
                            annotations.add(Notation.error(token, "Expected a Statement!"))
                            continue
                        }

                        if (node is Statement.Unresolved && node.lineTokens.isEmpty() && node.label == null) {
                            // node is empty
                            continue
                        }

                        if (node !is Statement) {
                            throw PsiParser.NodeException(node, "Didn't get a statement node for GASNode.buildNode(${GASNodeType.STATEMENT})!")
                        }

                        statements.add(node)
                    }

                    val node = Program(statements, lexer.ignored.map { Comment(it as AsmToken) }, lexer.error.map { Error(it as AsmToken) })
                    node.notations.addAll(annotations)
                    return node
                }

                GASNodeType.STATEMENT -> {
                    val label = buildNode(GASNodeType.LABEL, lexer, asmSpec) as? Label

                    val directive = buildNode(GASNodeType.DIRECTIVE, lexer, asmSpec)
                    if (directive != null && directive is Directive) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Dir(label, directive, lineBreak)
                            node.notations.add(Notation.error(lineBreak, "Linebreak is missing!"))
                            return node
                        }

                        return Statement.Dir(label, directive, lineBreak)
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, lexer, asmSpec)
                    if (instruction != null && instruction is Instruction) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Instr(label, instruction, lineBreak)
                            node.notations.add(Notation.error(lineBreak, "Linebreak is missing!"))
                            return node
                        }

                        return Statement.Instr(label, instruction, lineBreak)
                    }

                    val lineBreak = lexer.consume(true)
                    if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                        val unresolvedTokens = mutableListOf<AsmToken>(lineBreak)
                        var token: AsmToken

                        while (true) {
                            token = lexer.consume(false)

                            if (token.type == AsmTokenType.LINEBREAK || token.type == AsmTokenType.EOF) {
                                break
                            }

                            if (!lexer.hasMoreTokens()) {
                                // TODO Error Handling
                                val node = Statement.Unresolved(label, unresolvedTokens, token)
                                node.notations.add(Notation.error(lineBreak, "Linebreak is missing!"))
                                return node
                            }

                            unresolvedTokens.add(token)
                        }

                        return Statement.Unresolved(label, unresolvedTokens, token)
                    }

                    return Statement.Empty(label, lineBreak)
                }

                GASNodeType.DIRECTIVE -> {
                    (asmSpec.customDirs + GASDirType.entries).forEach {
                        val node = it.buildDirectiveContent(lexer, asmSpec)
                        if (node != null) {
                            //nativeLog("Found directive ${it.getDetectionString()} ${node::class.simpleName}")
                            return node
                        }
                        lexer.position = initialPos
                    }
                    return null
                }

                GASNodeType.INSTRUCTION -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.INSTRNAME) {
                        lexer.position = initialPos
                        return null
                    }

                    val validTypes = asmSpec.allInstrs.filter { it.getDetectionName().lowercase() == first.value.lowercase() }
                    if (validTypes.isEmpty()) {
                        val node = Error(first)
                        node.notations.add(Notation.error(node, "No valid instruction type found for $first."))
                        return node
                    }

                    validTypes.forEach {
                        val rule = it.paramRule ?: return Instruction(it, first, emptyList(), emptyList())
                        val result = rule.matchStart(lexer, asmSpec)
                        if (result.matches) {
                            return Instruction(it, first, result.matchingTokens, result.matchingNodes)
                        }
                    }

                    val node = Error(first)
                    node.notations.add(Notation.error(node, "Invalid Arguments for valid types: ${validTypes.joinToString { it.typeName }}!"))
                    return node
                }

                GASNodeType.INT_EXPR -> {
                    return NumericExpr.parse(lexer)
                }

                GASNodeType.ANY_EXPR -> {
                    val stringExpr = StringExpr.parse(lexer)
                    if (stringExpr != null) return stringExpr

                    val numericExpr = NumericExpr.parse(lexer)
                    return numericExpr
                }

                GASNodeType.STRING_EXPR -> {
                    return StringExpr.parse(lexer)
                }

                GASNodeType.LABEL -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.SYMBOL && first.type != AsmTokenType.INT_DEC) {
                        lexer.position = initialPos
                        return null
                    }

                    val second = lexer.consume(false)
                    if (second.value != ":") {
                        lexer.position = initialPos
                        return null
                    }
                    return Label(first, second)
                }

                GASNodeType.ARG -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.SYMBOL) {
                        lexer.position = initialPos
                        return null
                    }

                    val second = lexer.consume(true)
                    if (second.value != "=") {
                        lexer.position = second.start
                        return Argument.Basic(first)
                    }

                    val third = buildNode(GASNodeType.ANY_EXPR, lexer, asmSpec)
                    if (third != null) {
                        return Argument.DefaultValue(first, second, third)
                    }

                    val thirdNotExpr = lexer.consume(true)
                    return Argument.DefaultValue(first, second, TokenExpr(thirdNotExpr))
                }

                GASNodeType.ARG_DEF -> {
                    val named = ArgDef.Named.rule.matchStart(lexer, asmSpec)
                    if (named.matches) {
                        return ArgDef.Named(named.matchingTokens[0], named.matchingTokens[1], named.matchingTokens.drop(2))
                    }

                    val pos = ArgDef.Positional.rule.matchStart(lexer, asmSpec)
                    if (pos.matches && pos.matchingTokens.isNotEmpty()) {
                        return ArgDef.Positional(pos.matchingTokens)
                    }

                    lexer.position = initialPos
                    return null
                }
            }
        }
    }

    /**
     * [Program]
     * A RootNode only contains several [Statement]s.
     */
    class Program(statements: List<Statement>, comments: List<Comment>, errors: List<Error>) : GASNode(
        statements.minBy { it.range.start }.range.start..statements.maxBy { it.range.last }.range.last,
        *(statements + comments + errors).sortedBy { it.range.start }.toTypedArray()
    ) {

        override val pathName: String = this::class.simpleName.toString()

        init {
            //removeEmptyStatements()
        }

        override fun getFormatted(identSize: Int): String = children.joinToString("") { it.getFormatted(identSize) }

        /*private fun removeEmptyStatements() {
            ArrayList(children).filterIsInstance<Statement.Empty>().forEach {
                if (it.label == null) children.remove(it)
            }
        }*/

        fun getAllStatements(): List<Statement> = children.filterIsInstance<Statement>()
    }

    /**
     * [Statement]
     * Each Statement can contain an optional [Label].
     *
     *  - [Statement.Empty] No Content
     *  - [Statement.Dir] Content determined by [Directive]
     *  - [Statement.Instr] Content determined by [Instruction]
     *  - [Statement.Unresolved] Unresolved Content
     */
    sealed class Statement(val label: Label?, val lineBreak: AsmToken, range: IntRange? = null, vararg childs: GASNode) : GASNode(
        range ?: (label?.range?.start ?: childs.firstOrNull()?.range?.start ?: lineBreak.start)..<lineBreak.end
    ) {
        override val pathName: String get() = PATHNAME

        override fun getCodeStyle(position: Int): CodeStyle? {
            if (label != null && position in label.range) {
                return label.getCodeStyle(position)
            }

            return super.getCodeStyle(position)
        }

        init {
            if (label != null) {
                children.add(label)
            }
            children.addAll(childs)
        }

        companion object {
            const val PATHNAME = "Statement"
        }

        class Dir(label: Label?, val dir: Directive, lineBreak: AsmToken) : Statement(label, lineBreak, null, dir) {
            override val interlineWidgets: MutableList<Widget> = mutableListOf(Widget("directive", dir.type.typeName, Widget.Type.INTERLINE, { this.range.first }))
            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + " ".repeat(identSize - label.range.count() % identSize) + dir.getFormatted(identSize) + lineBreak.value
                } else {
                    dir.getFormatted(identSize) + lineBreak.value
                }
            }
        }

        class Instr(label: Label?, val instruction: Instruction, lineBreak: AsmToken) : Statement(label, lineBreak, null, instruction) {
            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + " ".repeat(identSize - label.range.count() % identSize) + instruction.getFormatted(identSize) + lineBreak.value
                } else {
                    " ".repeat(identSize) + instruction.getFormatted(identSize) + lineBreak.value
                }
            }
        }

        class Unresolved(label: Label?, val lineTokens: List<AsmToken>, lineBreak: AsmToken) : Statement(
            label, lineBreak,
            (label?.range?.start ?: lineTokens.firstOrNull()?.range?.start ?: lineBreak.range.first)..lineBreak.range.last
        ) {
            val content = lineTokens.joinToString("") { it.value } + lineBreak.value

            init {
                notations.add(Notation.error(this, "Statement is unresolved!"))
            }

            override fun getFormatted(identSize: Int): String = (label?.getFormatted(identSize) ?: "") + lineTokens.joinToString("") { it.value } + lineBreak.value
        }

        class Empty(label: Label?, lineBreak: AsmToken) : Statement(label, lineBreak) {

            override fun getFormatted(identSize: Int): String {
                return if (label != null) {
                    label.getFormatted(identSize) + lineBreak.value
                } else {
                    lineBreak.value
                }
            }
        }
    }

    class Label(val nameToken: AsmToken, val colon: AsmToken) : GASNode(nameToken.start..<colon.end) {
        override val pathName get() = nameToken.value + colon.value
        val type = if (nameToken.type == AsmTokenType.INT_DEC) Type.LOCAL else Type.GLOBAL
        val identifier = nameToken.value

        override fun getFormatted(identSize: Int): String = "${nameToken.value}${colon.value}"

        override fun getCodeStyle(position: Int): CodeStyle = CodeStyle.label

        enum class Type {
            LOCAL,
            GLOBAL
        }
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, val optionalIdentificationToken: AsmToken?, val allTokens: List<AsmToken> = listOf(), val additionalNodes: List<GASNode> = listOf()) : GASNode(
        (optionalIdentificationToken?.range?.start ?: allTokens.first().range.first)..maxOf(allTokens.lastOrNull()?.range?.start ?: 0, additionalNodes.lastOrNull()?.range?.last ?: 0),
        *additionalNodes.toTypedArray()
    ) {
        override val pathName: String get() = type.typeName

        override val additionalInfo: String
            get() = type.typeName + " " + optionalIdentificationToken

        private val sortedContent = (allTokens + additionalNodes).sortedBy { it.range.start }

        override fun getFormatted(identSize: Int): String = (optionalIdentificationToken?.value ?: "") + sortedContent.joinToString("", " ") {
            when (it) {
                is GASNode -> it.getFormatted(identSize)
                is Token -> it.value
                else -> ""
            }
        }
    }

    sealed class Argument(val argName: AsmToken, range: IntRange) : GASNode(range) {
        override val pathName: String
            get() = argName.value

        override fun getCodeStyle(position: Int): CodeStyle? {
            if (position in argName.range) return CodeStyle.argument
            return super.getCodeStyle(position)
        }

        abstract fun getDefaultValue(): String

        class Basic(argName: AsmToken) : Argument(argName, argName.range) {
            override fun getDefaultValue(): String = ""

            override fun getFormatted(identSize: Int): String = argName.value
        }

        class DefaultValue(argName: AsmToken, val assignment: AsmToken, private val expression: GASNode? = null) : Argument(argName, argName.range.first..(expression?.range?.last ?: assignment.range.last)) {
            init {
                expression?.let {
                    children.add(expression)
                }
            }

            override fun getCodeStyle(position: Int): CodeStyle? {
                if (position in argName.range || position in assignment.range) {
                    return CodeStyle.argument
                }

                return super.getCodeStyle(position)
            }

            override fun getDefaultValue(): String = expression?.getFormatted(identSize = 4) ?: ""

            override fun getFormatted(identSize: Int): String = "${argName.value} ${assignment.value}${if (expression != null) " ${expression.getFormatted(identSize)}" else ""}"
        }
    }

    sealed class ArgDef(val content: List<AsmToken>, range: IntRange) : GASNode(range) {

        class Positional(content: List<AsmToken>) : ArgDef(content, content.first().range.first..content.last().range.last) {
            override val pathName: String
                get() = PATHNAME

            companion object {
                const val PATHNAME = "Positional Argument"
                val rule = Rule {
                    Seq(
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(AsmTokenType.LINEBREAK)))
                        }
                    )
                }
            }

            override fun getFormatted(identSize: Int): String = content.joinToString("") { it.value }
        }

        class Named(val nameToken: AsmToken, val assignment: AsmToken, content: List<AsmToken>) : ArgDef(content, nameToken.range.first..(content.lastOrNull()?.range?.last ?: assignment.range.last)) {
            override val pathName: String
                get() = nameToken.value

            companion object {
                val rule = Rule {
                    Seq(
                        InSpecific(AsmTokenType.SYMBOL),
                        Specific("="),
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(AsmTokenType.LINEBREAK)))
                        }
                    )
                }
            }

            override fun getCodeStyle(position: Int): CodeStyle? {
                if (position in nameToken.range || position in assignment.range) {
                    return CodeStyle.argument
                }

                return super.getCodeStyle(position)
            }

            override fun getFormatted(identSize: Int): String = "${nameToken.value} ${assignment.value} ${content.joinToString("") { it.value }}"
        }
    }

    class Instruction(val type: InstrTypeInterface, val instrName: AsmToken, val tokens: List<AsmToken>, val nodes: List<GASNode>) : GASNode(instrName.range.first..maxOf(tokens.lastOrNull()?.range?.last ?: 0, instrName.range.last, nodes.lastOrNull()?.range?.last ?: 0), *nodes.toTypedArray()) {
        var addr: Value? = null
        override val pathName: String
            get() = instrName.value

        override fun getFormatted(identSize: Int): String = "${instrName.value} ${
            (tokens + nodes).sortedBy { it.range.first }.joinToString("") {
                when (it) {
                    is AsmToken -> it.value
                    is GASNode -> it.getFormatted(identSize)
                    else -> ""
                }
            }
        }"

        override fun getCodeStyle(position: Int): CodeStyle? {
            if (position in instrName.range) {
                return CodeStyle.keyWordInstr
            }

            tokens.firstOrNull {
                position in it.range
            }?.let {
                return it.type.style
            }

            nodes.firstOrNull {
                position in it.range
            }?.let {
                return it.getCodeStyle(position)
            }

            return super.getCodeStyle(position)
        }
    }

    class TokenExpr(val token: AsmToken) : GASNode(token.range) {
        companion object {
            const val PATHNAME = "Token"
        }

        override val pathName: String
            get() = PATHNAME

        override fun getFormatted(identSize: Int): String = token.value
    }

    sealed class StringExpr(range: IntRange, vararg operands: StringExpr) : GASNode(range, *operands) {

        abstract fun evaluate(printErrors: Boolean): String

        class Concatenation(private val exprs: Array<StringExpr>) : StringExpr(exprs.first().range.first..exprs.last().range.last, *exprs) {
            override val pathName: String
                get() = "Concatenation"

            override fun evaluate(printErrors: Boolean): String = exprs.joinToString("") { it.evaluate(printErrors) }

            override fun getFormatted(identSize: Int): String = exprs.joinToString(" ") { it.getFormatted(identSize) }
        }

        sealed class Operand(val token: AsmToken, range: IntRange) : StringExpr(range) {

            class Identifier(val symbol: AsmToken) : Operand(symbol, symbol.range), PsiReference {
                private var expr: StringExpr? = null
                override val pathName: String
                    get() = symbol.value

                override val element: PsiElement = this
                override var referencedElement: PsiElement? = null

                override fun getCodeStyle(position: Int): CodeStyle? {
                    if (expr != null) {
                        return CodeStyle.symbol
                    }
                    return null
                }

                override fun evaluate(printErrors: Boolean): String {
                    val currExpr = expr

                    if (currExpr != null) {
                        return currExpr.evaluate(printErrors)
                    }

                    if (printErrors) throw PsiParser.NodeException(this, "Unknown String Identifier!")

                    return ""
                }

                override fun getFormatted(identSize: Int): String = symbol.value


                override fun resolve(): PsiElement? = referencedElement

                override fun isReferenceTo(element: PsiElement): Boolean = referencedElement == element
            }

            class StringLiteral(val string: AsmToken) : Operand(string, string.range) {
                private val stringContent: String = token.getContentAsString()
                override val pathName: String
                    get() = string.value

                override fun evaluate(printErrors: Boolean): String = stringContent

                override fun getFormatted(identSize: Int): String = string.value
            }
        }

        companion object {
            const val PATHNAME = "String"

            fun parse(lexer: AsmLexer, allowSymbolsAsOperands: Boolean = true): StringExpr? {
                val initialPos = lexer.position

                val operands = mutableListOf<Operand>()
                while (true) {
                    val token = lexer.consume(true)

                    when {
                        token.type.isStringLiteral -> operands.add(Operand.StringLiteral(token))
                        allowSymbolsAsOperands && token.type == AsmTokenType.SYMBOL -> operands.add(Operand.Identifier(token))
                        else -> {
                            lexer.position = token.start
                            break
                        }
                    }
                }

                if (operands.isEmpty()) {
                    lexer.position = initialPos
                    return null
                }

                val expr = if (operands.size == 1) {
                    operands.first()
                } else {
                    Concatenation(operands.toTypedArray())
                }

                return expr
            }
        }
    }

    /**
     * [NumericExpr]
     *
     */
    sealed class NumericExpr(val brackets: List<AsmToken>, range: IntRange, vararg operands: NumericExpr) : GASNode(range, *operands) {
        abstract fun evaluate(throwErrors: Boolean): Value.Dec
        abstract fun isDefined(): Boolean

        companion object {
            fun parse(lexer: AsmLexer, allowSymbolsAsOperands: Boolean = true): NumericExpr? {
                val initialPos = lexer.position
                val relevantTokens = mutableListOf<AsmToken>()

                while (true) {
                    val token = lexer.consume(true)
                    when {
                        token.type.isOperator -> relevantTokens.add(token)
                        token.type.isNumberLiteral -> relevantTokens.add(token)
                        token.type.isCharLiteral -> relevantTokens.add(token)
                        token.type.isBasicBracket() -> relevantTokens.add(token)
                        allowSymbolsAsOperands && token.type.isLinkableSymbol() -> relevantTokens.add(token)
                        else -> {
                            lexer.position = token.start
                            break
                        }
                    }
                }

                if (relevantTokens.lastOrNull()?.type?.isOpeningBracket == true) relevantTokens.removeLast()
                if (relevantTokens.isEmpty()) {
                    lexer.position = initialPos
                    return null
                }

                val markedAsPrefix = computePrefixList(relevantTokens)

                // Convert tokens to postfix notation
                val postFixTokens = convertToPostfix(relevantTokens, markedAsPrefix)
                val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet(), markedAsPrefix)

                if (expression != null) {
                    val unusedTokens = (relevantTokens - postFixTokens)
                    if (unusedTokens.isNotEmpty()) {
                        unusedTokens.forEach {
                            expression.notations.add(Notation.error(it, "Invalid Token $it for Numeric Expression!"))
                        }
                    }
                }

                if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                    "NumericExpr:" +
                            "\n\tRelevantTokens: ${relevantTokens.joinToString(" ") { it.value }}" +
                            "\n\tPostFixNotation: ${postFixTokens.joinToString(" ") { it.value }}" +
                            "\n\tExpression: ${expression?.getFormatted(4)}"
                )

                return expression
            }

            private fun computePrefixList(tokens: List<AsmToken>): List<AsmToken> {
                return tokens.filterIndexed { i, currentToken ->
                    if (currentToken.type.isOperator && currentToken.type.couldBePrefix) {
                        // Check if '-' is a negation prefix
                        val previousToken = tokens.getOrNull(i - 1)
                        val nextToken = tokens.getOrNull(i + 1)

                        /**
                         * Is Prefix if previous is:
                         * - null
                         * - not a literal or a symbol
                         */
                        val prevMatchesPrefix: Boolean = previousToken == null || !(previousToken.type.isLiteral() || previousToken.type.isLinkableSymbol())

                        /**
                         * Is Prefix if next is:
                         * - not null
                         * - a literal or a symbol or an opening bracket
                         */
                        val nextMatchesPrefix = nextToken != null && (nextToken.type.isLiteral() || nextToken.type.isLinkableSymbol() || nextToken.value == "(")

                        val isPrefix = prevMatchesPrefix && nextMatchesPrefix

                        isPrefix
                    } else {
                        false
                    }
                }
            }

            private fun buildExpressionFromPostfixNotation(tokens: MutableList<AsmToken>, brackets: List<AsmToken>, prefixTokens: List<AsmToken>): NumericExpr? {
                val uncheckedLast1 = tokens.removeLastOrNull() ?: return null
                val operator = when {
                    uncheckedLast1.type.isOperator -> {
                        uncheckedLast1
                    }

                    uncheckedLast1.type.isLinkableSymbol() -> {
                        return Operand.Identifier(uncheckedLast1)
                    }

                    uncheckedLast1.type.isNumberLiteral -> {
                        return Operand.Number(uncheckedLast1)
                    }

                    uncheckedLast1.type.isCharLiteral -> {
                        return Operand.Char(uncheckedLast1)
                    }

                    else -> {
                        throw PsiParser.TokenException(uncheckedLast1, "Token is not an operator!")
                    }
                }
                val uncheckedLast2 = tokens.lastOrNull() ?: return null
                val operandA = when {
                    uncheckedLast2.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, prefixTokens)
                    uncheckedLast2.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())

                    else -> {
                        throw PsiParser.TokenException(uncheckedLast2, "Token is not an operator!")
                    }
                } ?: return null

                if (prefixTokens.contains(operator)) {
                    return Prefix(operator, operandA, brackets)
                }

                val uncheckedLast3 = tokens.lastOrNull()
                val operandB = when {
                    uncheckedLast3 == null -> null
                    uncheckedLast3.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, prefixTokens)
                    uncheckedLast3.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast3.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast3.type.isCharLiteral -> Operand.Char(tokens.removeLast())
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets)
                } else null
            }

            /**
             * [convertToPostfix] converts infix notated expression to postfix notated expression
             *
             * Operators: [AsmTokenType.isOperator] -> Precedence through [Token.getPrecedence] which contains [Token.Precedence]
             * Operands: [AsmTokenType.isLiteral]
             * Brackets: [AsmTokenType.isBasicBracket]
             *
             */
            private fun convertToPostfix(infix: List<AsmToken>, markedAsPrefix: List<AsmToken>): List<AsmToken> {
                val output = mutableListOf<AsmToken>()
                val operatorStack = mutableListOf<AsmToken>()

                for (token in infix) {
                    if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                        "PostFixIteration: for ${token.type}:${token.getPrecedence(markedAsPrefix)} -> ${token.value}" +
                                "\n\tOutput: ${output.joinToString(" ") { it.value }}" +
                                "\n\tOperatorStack: ${operatorStack.joinToString("") { it.value }}"
                    )
                    if (token.type.isLiteral() || token.type.isLinkableSymbol()) {
                        output.add(token)
                        continue
                    }

                    if (token.type.isOperator) {
                        val higherOrEqualPrecedence = mutableListOf<Token>()
                        for (op in operatorStack) {
                            if (op.type.isOperator && op.higherOrEqualPrecedenceAs(token, markedAsPrefix)) {
                                output.add(op)
                                higherOrEqualPrecedence.add(op)
                            }
                            if (op.type.isPunctuation) break
                        }
                        operatorStack.removeAll(higherOrEqualPrecedence)
                        operatorStack.add(token)
                        continue
                    }

                    if (token.type.isPunctuation) {
                        if (token.type.isOpeningBracket) {
                            operatorStack.add(token)
                            continue
                        }
                        if (token.type.isClosingBracket) {
                            var peekedOpToken = operatorStack.lastOrNull()
                            while (peekedOpToken != null) {
                                if (peekedOpToken.type.isOpeningBracket) {
                                    operatorStack.removeLast()
                                    break
                                }
                                output.add(operatorStack.removeLast())
                                peekedOpToken = operatorStack.lastOrNull()
                            }
                            continue
                        }
                    }

                    throw PsiParser.TokenException(token, "Token (${token::class.simpleName}: ${token.value}) is not valid in a numeric expression!")
                }

                while (operatorStack.isNotEmpty()) {
                    output.add(operatorStack.removeLast())
                }

                return output
            }
        }

        /**
         * [Prefix]
         * - [operator] [Operand]
         *
         */
        class Prefix(val operator: AsmToken, val operand: NumericExpr, brackets: List<AsmToken>) : NumericExpr(
            brackets,
            operator.range.first..(brackets.lastOrNull()?.range?.last ?: operand.range.last),
            operand
        ) {
            override val pathName: String
                get() = operator.value

            override fun evaluate(throwErrors: Boolean): Value.Dec {
                return when (operator.type) {
                    AsmTokenType.COMPLEMENT -> operand.evaluate(throwErrors).toBin().inv().toDec()
                    AsmTokenType.MINUS -> (-operand.evaluate(throwErrors)).toDec()
                    AsmTokenType.PLUS -> operand.evaluate(throwErrors)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operator.value}${operand.getFormatted(identSize)}" else "${operator.value}(${operand.getFormatted(identSize)})"

            override fun isDefined(): Boolean = operand.isDefined()
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(private val operandA: NumericExpr, private val operator: AsmToken, private val operandB: NumericExpr, brackets: List<AsmToken>) : NumericExpr(
            brackets,
            (brackets.firstOrNull()?.range?.first ?: operandA.range.first)..(brackets.lastOrNull()?.range?.last ?: operandB.range.last), operandA,
            operandB
        ) {
            override val pathName: String
                get() = operator.value

            override fun evaluate(throwErrors: Boolean): Value.Dec {
                return (when (operator.type) {
                    AsmTokenType.MULT -> operandA.evaluate(throwErrors) * operandB.evaluate(throwErrors)
                    AsmTokenType.DIV -> operandA.evaluate(throwErrors) / operandB.evaluate(throwErrors)
                    AsmTokenType.REM -> operandA.evaluate(throwErrors) % operandB.evaluate(throwErrors)
                    AsmTokenType.SHL -> operandA.evaluate(throwErrors).toBin() shl (operandB.evaluate(throwErrors).toUDec().toIntOrNull() ?: return operandA.evaluate(throwErrors))
                    AsmTokenType.SHR -> operandA.evaluate(throwErrors).toBin() shl (operandB.evaluate(throwErrors).toUDec().toIntOrNull() ?: return operandA.evaluate(throwErrors))
                    AsmTokenType.BITWISE_OR -> operandA.evaluate(throwErrors).toBin() or operandB.evaluate(throwErrors).toBin()
                    AsmTokenType.BITWISE_AND -> operandA.evaluate(throwErrors).toBin() and operandB.evaluate(throwErrors).toBin()
                    AsmTokenType.BITWISE_XOR -> operandA.evaluate(throwErrors).toBin() xor operandB.evaluate(throwErrors).toBin()
                    AsmTokenType.BITWISE_ORNOT -> operandA.evaluate(throwErrors).toBin() or operandB.evaluate(throwErrors).toBin().inv()
                    AsmTokenType.PLUS -> operandA.evaluate(throwErrors) + operandB.evaluate(throwErrors)
                    AsmTokenType.MINUS -> operandA.evaluate(throwErrors) - operandB.evaluate(throwErrors)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }).toDec()
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)}" else "(${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)})"

            override fun isDefined(): Boolean = operandA.isDefined() && operandB.isDefined()
        }

        sealed class Operand(val token: AsmToken, range: IntRange) : NumericExpr(listOf(), range) {
            override val pathName: String
                get() = additionalInfo

            class Identifier(val symbol: AsmToken) : Operand(symbol, symbol.range), PsiReference {
                private var expr: NumericExpr? = null
                private var labelAddr: Value? = null
                override val additionalInfo: String
                    get() = token.value

                override fun getFormatted(identSize: Int): String = symbol.value
                fun isLinked(): Boolean = expr != null
                override fun evaluate(throwErrors: Boolean): Value.Dec {
                    val currExpr = expr
                    if (currExpr != null) {
                        return currExpr.evaluate(throwErrors)
                    }

                    val currLabelAddr = labelAddr
                    if (currLabelAddr != null) {
                        return currLabelAddr.toDec()
                    }

                    if (throwErrors) throw PsiParser.NodeException(this, "Unknown Numeric Identifier!")
                    //symbol.addSeverity(Severity.Type.ERROR, "Can't evaluate from a undefined symbol. Returning 0.")
                    return Value.Dec("0", Size.Bit32)
                }

                override fun getCodeStyle(position: Int): CodeStyle? {
                    if (expr != null) return CodeStyle.symbol
                    if (labelAddr != null) return CodeStyle.label
                    return null
                }

                override fun isDefined(): Boolean = expr != null || labelAddr != null

                override val element: PsiElement = this
                override var referencedElement: NumericExpr? = null

                override fun resolve(): PsiElement? = referencedElement

                override fun isReferenceTo(element: PsiElement): Boolean = referencedElement == element
            }

            class Number(val number: AsmToken) : Operand(number, number.range) {
                val type: Type
                val value: Value.Dec
                override val additionalInfo: String
                    get() = number.value

                init {
                    val intVal: Value?
                    val bigNum64: Value?
                    val bigNum128: Value?
                    when (number.type) {
                        AsmTokenType.INT_BIN -> {
                            val possibleInt = Value.Bin(number.asNumber, Size.Bit32)
                            intVal = if (possibleInt.getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum64 = Value.Bin(number.asNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum64.getBit(0)?.toRawString() == "1") null else possibleBigNum64

                            val possibleBigNum128 = Value.Bin(number.asNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        AsmTokenType.INT_HEX -> {
                            val possibleInt = Value.Hex(number.asNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.Hex(number.asNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.Hex(number.asNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        AsmTokenType.INT_OCT -> {
                            val possibleInt = Value.Oct(number.asNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.Oct(number.asNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.Oct(number.asNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        AsmTokenType.INT_DEC -> {
                            val possibleInt = Value.UDec(number.asNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.UDec(number.asNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.UDec(number.asNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        else -> {
                            throw PsiParser.NodeException(this, "$number is not an Integer, BigNum64 or BigNum128.")
                        }
                    }

                    if (intVal?.valid == true) {
                        value = intVal.toDec()
                        type = Type.Integer
                    } else if (bigNum64?.valid == true) {
                        value = bigNum64.toDec()
                        type = Type.BigNum64
                    } else if (bigNum128?.valid == true) {
                        value = bigNum128.toDec()
                        type = Type.BigNum128
                    } else {
                        throw PsiParser.NodeException(this, "$number is not supported. Supported Types: Integer, BigNum64, BigNum128.")
                    }
                }

                override fun evaluate(throwErrors: Boolean): Value.Dec = value

                override fun getFormatted(identSize: Int): String = number.value
                override fun getCodeStyle(position: Int): CodeStyle? = number.type.style

                override fun isDefined(): Boolean = true

                enum class Type {
                    Integer,
                    BigNum64,
                    BigNum128
                }
            }

            class Char(val char: AsmToken) : Operand(char, char.range) {

                val value: Value.Dec
                override val additionalInfo: String
                    get() = char.value

                override fun getFormatted(identSize: Int): String = char.value

                init {
                    val hexString = Value.Tools.asciiToHex(char.getContentAsString())
                    value = Value.Hex(hexString, Size.Bit32).toDec()
                }

                override fun evaluate(throwErrors: Boolean): Value.Dec = value

                override fun isDefined(): Boolean = true

                override fun getCodeStyle(position: Int): CodeStyle? = char.type.style
            }
        }
    }


}







