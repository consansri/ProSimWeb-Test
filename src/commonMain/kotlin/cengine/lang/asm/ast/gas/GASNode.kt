package cengine.lang.asm.ast.gas

import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.DirTypeInterface
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
 *
 */
sealed class GASNode(vararg children: GASNode) : PsiElement, PsiFormatter {
    override var parent: PsiElement? = null

    final override val children: MutableList<GASNode> = mutableListOf(*children)
    open fun getCodeStyle(position: TextPosition): CodeStyle? {
        return children.firstOrNull {
            position in it.textRange
        }?.getCodeStyle(position)
    }

    override fun accept(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }

    class Label(val name: AsmToken, val colon: AsmToken) : GASNode() {
        val type = if (name.type == AsmTokenType.INT_DEC) Type.LOCAL else Type.GLOBAL
        val identifier = name.value
        override var textRange: TextRange = name.start..<colon.end

        override fun getFormatted(): String = "$name$colon"

        override fun getCodeStyle(position: TextPosition): CodeStyle = CodeStyle.label

        enum class Type {
            LOCAL,
            GLOBAL
        }
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
                    while (lexer.hasMoreTokens()) {
                        val node = buildNode(GASNodeType.STATEMENT, lexer, asmSpec)

                        if (node == null) {
                            continue
                        }

                        if (node is Statement.Unresolved && node.lineTokens.isEmpty() && node.label == null) {
                            continue
                        }

                        nativeLog("Found Statement: ${node::class.simpleName}")

                        if (node !is Statement) {
                            throw PsiParser.NodeException(node, "Didn't get a statement node for GASNode.buildNode(${GASNodeType.STATEMENT})!")
                        }

                        statements.add(node)
                    }

                    return Program(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val label = buildNode(GASNodeType.LABEL, lexer, asmSpec) as? Label
                    nativeLog("Parsing Statement...")

                    val directive = buildNode(GASNodeType.DIRECTIVE, lexer, asmSpec)
                    if (directive != null && directive is Directive) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            nativeLog("Returning null (from Directive) $lineBreak ${directive.type} ${directive.print("")}")
                            lexer.position = initialPos
                            return null
                        }
                        nativeLog("Returning Directive")
                        return Statement.Dir(label, directive, lineBreak)
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, lexer, asmSpec)
                    if (instruction != null && instruction is RawInstr) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            nativeLog("Returning null (from Instruction)")
                            lexer.position = initialPos
                            return null
                        }
                        nativeLog("Returning Instruction")
                        return Statement.Instr(label, instruction, lineBreak)
                    }

                    val lineBreak = lexer.consume(true)
                    if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                        val unresolvedTokens = mutableListOf<AsmToken>()
                        var token: AsmToken

                        while (true) {
                            token = lexer.consume(false)

                            if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                                break
                            }

                            if (!lexer.hasMoreTokens()) {
                                nativeLog("Returning null (from Unresolved)")
                                lexer.position = initialPos
                                return null
                            }

                            unresolvedTokens.add(token)
                        }

                        nativeLog("Returning Unresolved")
                        return Statement.Unresolved(label, unresolvedTokens, token)
                    }

                    nativeLog("Returning Empty")
                    return Statement.Empty(label, lineBreak)
                }

                GASNodeType.DIRECTIVE -> {
                    (GASDirType.entries + asmSpec.additionalDirectives()).forEach {
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

                    val paramTokens = mutableListOf<AsmToken>()
                    while (true) {
                        val token = lexer.consume(true)

                        if (token.type == AsmTokenType.LINEBREAK || token.type == AsmTokenType.EOF) {
                            lexer.position = (paramTokens.firstOrNull() ?: first).end
                            break
                        }

                        paramTokens.add(token)
                    }

                    return RawInstr(first, paramTokens)
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

        fun MutableList<Token>.dropSpaces(): List<Token> {
            val dropped = mutableListOf<Token>()
            while (this.isNotEmpty()) {
                if (this.first().type != AsmTokenType.WHITESPACE) break
                dropped.add(this.removeFirst())
            }
            return dropped
        }
    }

    /**
     * [Program]
     * A RootNode only contains several [Statement]s.
     */
    class Program(vararg statements: Statement) : GASNode(*statements) {

        override var textRange: TextRange = statements.minBy { it.textRange.startOffset }.textRange.startOffset..<statements.maxBy { it.textRange.endOffset }.textRange.endOffset

        override fun getFormatted(): String = children.joinToString("") { it.getFormatted() }

        fun removeEmptyStatements() {
            ArrayList(children).filterIsInstance<Statement.Empty>().forEach {
                if (it.label == null) children.remove(it)
            }
        }

        fun getAllStatements(): List<Statement> = children.filterIsInstance<Statement>()
    }

    /**
     * [Statement]
     * Each Statement can contain an optional [Label].
     *
     *  - [Statement.Empty] No Content
     *  - [Statement.Dir] Content determined by [Directive]
     *  - [Statement.Instr] Content determined by [RawInstr]
     *  - [Statement.Unresolved] Unresolved Content
     */
    sealed class Statement(val label: Label?, val lineBreak: AsmToken, vararg childs: GASNode) : GASNode() {
        override var textRange: TextRange = (label?.textRange?.startOffset ?: childs.firstOrNull()?.textRange?.startOffset ?: lineBreak.start)..<lineBreak.end

        override fun getCodeStyle(position: TextPosition): CodeStyle? {
            if (label != null && position in label.textRange) {
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

        override fun getFormatted(): String = children.joinToString(" ") { it.getFormatted() } + lineBreak.value

        class Dir(label: Label?, val dir: Directive, lineBreak: AsmToken) : Statement(label, lineBreak, dir) {
        }

        class Instr(label: Label?, val rawInstr: RawInstr, lineBreak: AsmToken) : Statement(label, lineBreak, rawInstr) {

        }

        class Unresolved(label: Label?, val lineTokens: List<AsmToken>, lineBreak: AsmToken) : Statement(label, lineBreak) {
            init {
                //throw PsiParser.NodeException(this, "Found unresolved Statement!")
            }

        }

        class Empty(label: Label?, lineBreak: AsmToken) : Statement(label, lineBreak) {

        }
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, val allTokens: List<AsmToken> = listOf(), val additionalNodes: List<GASNode> = listOf()) : GASNode(*additionalNodes.toTypedArray()) {

        override var textRange: TextRange = allTokens.first().textRange.startOffset..<maxOf(allTokens.lastOrNull()?.textRange?.endOffset ?: TextPosition(), additionalNodes.lastOrNull()?.textRange?.endOffset ?: TextPosition())

        private val sortedContent = (allTokens + additionalNodes).sortedBy { it.textRange.startOffset }

        override fun getFormatted(): String = sortedContent.joinToString(" ") {
            when (it) {
                is GASNode -> it.getFormatted()
                is Token -> it.value
                else -> ""
            }
        }
    }

    sealed class Argument(val argName: AsmToken) : GASNode() {

        override fun getCodeStyle(position: TextPosition): CodeStyle? {
            if (position in argName.textRange) return CodeStyle.argument
            return super.getCodeStyle(position)
        }

        abstract fun getDefaultValue(): String

        class Basic(argName: AsmToken) : Argument(argName) {
            override fun getDefaultValue(): String = ""
            override var textRange: TextRange = argName.textRange

            override fun getFormatted(): String = argName.value
        }

        class DefaultValue(argName: AsmToken, val assignment: AsmToken, private val expression: GASNode? = null) : Argument(argName) {
            init {
                expression?.let {
                    children.add(expression)
                }
            }

            override fun getCodeStyle(position: TextPosition): CodeStyle? {
                if (position in argName.textRange || position in assignment.textRange) {
                    return CodeStyle.argument
                }

                return super.getCodeStyle(position)
            }

            override fun getDefaultValue(): String = expression?.getFormatted() ?: ""
            override var textRange: TextRange = argName.textRange.startOffset..<(expression?.textRange?.endOffset ?: assignment.textRange.endOffset)

            override fun accept(visitor: PsiElementVisitor) {

            }

            override fun getFormatted(): String = "$argName ${assignment.value}${if (expression != null) " ${expression.getFormatted()}" else ""}"
        }
    }

    sealed class ArgDef(val content: List<AsmToken>) : GASNode() {

        class Positional(content: List<AsmToken>) : ArgDef(content) {

            override var textRange: TextRange = content.first().textRange.startOffset..<content.last().textRange.endOffset

            companion object {
                val rule = Rule {
                    Seq(
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(AsmTokenType.LINEBREAK)))
                        }
                    )
                }
            }

            override fun getFormatted(): String = content.joinToString("") { it.value }
        }

        class Named(val name: AsmToken, val assignment: AsmToken, content: List<AsmToken>) : ArgDef(content) {
            override var textRange: TextRange = name.textRange.startOffset..<(content.lastOrNull()?.textRange?.endOffset ?: assignment.textRange.endOffset)

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

            override fun getCodeStyle(position: TextPosition): CodeStyle? {
                if (position in name.textRange || position in assignment.textRange) {
                    return CodeStyle.argument
                }

                return super.getCodeStyle(position)
            }

            override fun getFormatted(): String = "$name $assignment ${content.joinToString("") { it.value }}"
        }
    }

    class RawInstr(val instrName: AsmToken, val remainingTokens: List<AsmToken>) : GASNode() {
        var addr: Value? = null
        override var textRange: TextRange = instrName.textRange.startOffset..<(remainingTokens.lastOrNull()?.textRange?.endOffset ?: instrName.textRange.endOffset)

        override fun getFormatted(): String = "$instrName ${remainingTokens.joinToString("") { it.value }}"

        override fun getCodeStyle(position: TextPosition): CodeStyle? {
            if (position in instrName.textRange) {
                return CodeStyle.keyWordInstr
            }

            remainingTokens.firstOrNull {
                position in it.textRange
            }?.let {
                return it.type.style
            }

            return super.getCodeStyle(position)
        }
    }


    class TokenExpr(val token: AsmToken) : GASNode() {
        override var textRange: TextRange = token.textRange

        override fun getFormatted(): String = token.value
    }

    sealed class StringExpr(vararg operands: StringExpr) : GASNode(*operands) {

        abstract fun evaluate(printErrors: Boolean): String

        class Concatenation(private val exprs: Array<StringExpr>) : StringExpr(*exprs) {

            override fun evaluate(printErrors: Boolean): String = exprs.joinToString("") { it.evaluate(printErrors) }

            override var textRange: TextRange = exprs.first().textRange.startOffset..<exprs.last().textRange.endOffset

            override fun getFormatted(): String = exprs.joinToString(" ") { it.getFormatted() }
        }

        sealed class Operand(val token: AsmToken) : StringExpr() {

            class Identifier(val symbol: AsmToken) : Operand(symbol), PsiReference {
                private var expr: StringExpr? = null

                override var textRange: TextRange = symbol.textRange
                override val element: PsiElement = this
                override var referencedElement: PsiElement? = null

                override fun getCodeStyle(position: TextPosition): CodeStyle? {
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

                override fun getFormatted(): String = symbol.value


                override fun resolve(): PsiElement? = referencedElement

                override fun isReferenceTo(element: PsiElement): Boolean = referencedElement == element
            }

            class StringLiteral(val string: AsmToken) : Operand(string) {
                private val stringContent: String = token.getContentAsString()
                override fun evaluate(printErrors: Boolean): String = stringContent

                override var textRange: TextRange = string.textRange

                override fun getFormatted(): String = string.value
            }
        }

        companion object {
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
    sealed class NumericExpr(brackets: List<AsmToken>, vararg operands: NumericExpr) : GASNode(*operands) {
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
                        throw PsiParser.TokenException(unusedTokens.first(), "Invalid Token ${unusedTokens.first().type} for the Numeric Expression!")
                    }
                }

                if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                    "NumericExpr:" +
                            "\n\tRelevantTokens: ${relevantTokens.joinToString(" ") { it.value }}" +
                            "\n\tPostFixNotation: ${postFixTokens.joinToString(" ") { it.value }}" +
                            "\n\tExpression: ${expression?.getFormatted()}"
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
        class Prefix(private val operator: AsmToken, private val operand: NumericExpr, val brackets: List<AsmToken>) : NumericExpr(brackets, operand) {

            override var textRange: TextRange = operator.textRange.startOffset..<(brackets.lastOrNull()?.textRange?.endOffset ?: operand.textRange.endOffset)

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

            override fun getFormatted(): String = if (brackets.isEmpty()) "${operator.value}${operand.getFormatted()}" else "${operator.value}(${operand.getFormatted()})"

            override fun isDefined(): Boolean = operand.isDefined()
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(private val operandA: NumericExpr, private val operator: AsmToken, private val operandB: NumericExpr, val brackets: List<AsmToken>) : NumericExpr(brackets, operandA, operandB) {
            override var textRange: TextRange = (brackets.firstOrNull()?.textRange?.startOffset ?: operandA.textRange.startOffset)..<(brackets.lastOrNull()?.textRange?.endOffset ?: operandB.textRange.endOffset)
            override fun evaluate(throwErrors: Boolean): Value.Dec {
                return (when (operator.type) {
                    AsmTokenType.MULT -> operandA.evaluate(throwErrors) * operandB.evaluate(throwErrors)
                    AsmTokenType.DIV -> operandA.evaluate(throwErrors) * operandB.evaluate(throwErrors)
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

            override fun getFormatted(): String = if (brackets.isEmpty()) "${operandA.getFormatted()} ${operator.value} ${operandB.getFormatted()}" else "(${operandA.getFormatted()} ${operator.value} ${operandB.getFormatted()})"

            override fun isDefined(): Boolean = operandA.isDefined() && operandB.isDefined()
        }

        sealed class Operand(val token: AsmToken) : NumericExpr(brackets = listOf()) {

            class Identifier(val symbol: AsmToken) : Operand(symbol), PsiReference {
                private var expr: NumericExpr? = null
                private var labelAddr: Value? = null

                override var textRange: TextRange = symbol.textRange
                override fun getFormatted(): String = symbol.value
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

                override fun getCodeStyle(position: TextPosition): CodeStyle? {
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

            class Number(val number: AsmToken) : Operand(number) {
                val type: Type
                val value: Value.Dec
                override var textRange: TextRange = number.textRange

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

                override fun getFormatted(): String = number.value
                override fun getCodeStyle(position: TextPosition): CodeStyle? = number.type.style

                override fun isDefined(): Boolean = true

                enum class Type {
                    Integer,
                    BigNum64,
                    BigNum128
                }
            }

            class Char(val char: AsmToken) : Operand(char) {

                val value: Value.Dec
                override var textRange: TextRange = char.textRange

                override fun getFormatted(): String = char.value

                init {
                    val hexString = Value.Tools.asciiToHex(char.getContentAsString())
                    value = Value.Hex(hexString, Size.Bit32).toDec()
                }

                override fun evaluate(throwErrors: Boolean): Value.Dec = value

                override fun isDefined(): Boolean = true

                override fun getCodeStyle(position: TextPosition): CodeStyle? = char.type.style
            }
        }
    }


}







