package emulator.kit.assembler.gas

import debug.DebugTools
import emulator.core.Size
import emulator.core.Value
import emulator.kit.assembler.AsmHeader
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.GASNode.*
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.syntax.Component.*
import emulator.kit.assembler.syntax.Rule
import emulator.kit.nativeError
import emulator.kit.nativeLog

/**
 * Node Relatives
 * --------------------
 * 1. [Root]
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
sealed class GASNode(vararg children: Node) : Node.HNode(*children) {

    fun addSpaces(spaces: List<Token>) {
        addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
    }

    class Label(name: Token, colon: Token) : GASNode(BaseNode(name), BaseNode(colon)) {

        val type = if (name.type == Token.Type.INT_DEC) Type.LOCAL else Type.GLOBAL
        val identifier = name.content

        init {
            name.hl(CodeStyle.label)
            colon.hl(CodeStyle.label)
        }

        enum class Type {
            LOCAL,
            GLOBAL
        }
    }

    companion object {
        /**
         * Severities will be set by the Lowest Node, which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, allDirs: List<DirTypeInterface>, asmHeader: AsmHeader, assignedSymbols: List<GASParser.Symbol> = emptyList()): GASNode? {
            val remainingTokens = source.toMutableList()
            when (gasNodeType) {
                GASNodeType.ROOT -> {
                    val statements = mutableListOf<Statement>()
                    while (remainingTokens.isNotEmpty() && remainingTokens.firstOrNull { it.type == Token.Type.LINEBREAK } != null) {
                        val node = buildNode(GASNodeType.STATEMENT, remainingTokens, allDirs, asmHeader)

                        if (node == null) {
                            val lineContent = remainingTokens.takeWhile { it.type != Token.Type.LINEBREAK }
                            lineContent.forEach { it.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_NOT_A_STATEMENT)) }
                            remainingTokens.removeAll(lineContent)
                            continue
                        }

                        if (node !is Statement) {
                            remainingTokens.removeAll(node.tokens().toSet())
                            nativeError("Didn't get a statement node for GASNode.buildNode(${GASNodeType.STATEMENT})!")
                            continue
                        }

                        statements.add(node)
                        remainingTokens.removeAll(node.tokens().toSet())
                    }

                    return Root(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val spaces = mutableListOf<Token>()
                    spaces.addAll(remainingTokens.dropSpaces())

                    val label = buildNode(GASNodeType.LABEL, remainingTokens, allDirs, asmHeader) as? Label
                    if (label != null) {
                        remainingTokens.removeAll(label.tokens().toSet())
                    }
                    spaces.addAll(remainingTokens.dropSpaces())

                    val directive = buildNode(GASNodeType.DIRECTIVE, remainingTokens, allDirs, asmHeader)
                    if (directive != null && directive is Directive) {
                        remainingTokens.removeAll(directive.tokens().toSet())
                        spaces.addAll(remainingTokens.dropSpaces())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        val node = Statement.Dir(label, directive, lineBreak)
                        node.addSpaces(spaces)
                        return node
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, remainingTokens, allDirs, asmHeader)
                    if (instruction != null && instruction is RawInstr) {
                        remainingTokens.removeAll(instruction.tokens().toSet())
                        spaces.addAll(remainingTokens.dropSpaces())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        val node = Statement.Instr(label, instruction, lineBreak)
                        node.addSpaces(spaces)
                        return node
                    }

                    val lineBreak = remainingTokens.checkLineBreak()
                    if (lineBreak == null) {
                        val remainingLineContent = remainingTokens.takeWhile { it.type != Token.Type.LINEBREAK }
                        remainingTokens.removeAll(remainingLineContent)
                        spaces.addAll(remainingTokens.dropSpaces())
                        val newLineBreak = remainingTokens.checkLineBreak() ?: return null
                        val node = Statement.Unresolved(label, remainingLineContent, newLineBreak)
                        node.addSpaces(spaces)
                        return node
                    }

                    val node = Statement.Empty(label, lineBreak)
                    node.addSpaces(spaces)
                    return node
                }

                GASNodeType.DIRECTIVE -> {
                    allDirs.forEach {
                        val node = it.buildDirectiveContent(remainingTokens, allDirs, asmHeader)
                        if (node != null) {
                            return node
                        }
                    }
                    return null
                }

                GASNodeType.INSTRUCTION -> {
                    remainingTokens.dropSpaces()

                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.INSTRNAME) {
                        return null
                    }
                    remainingTokens.removeFirst()

                    val params = remainingTokens.takeWhile { it.type != Token.Type.LINEBREAK }
                    return RawInstr(first, params)
                }

                GASNodeType.INT_EXPR -> {
                    return NumericExpr.parse(remainingTokens, assignedSymbols)
                }

                GASNodeType.ANY_EXPR -> {
                    val stringExpr = StringExpr.parse(remainingTokens)
                    if (stringExpr != null) return stringExpr

                    val numericExpr = NumericExpr.parse(remainingTokens, assignedSymbols)
                    return numericExpr
                }

                GASNodeType.STRING_EXPR -> {
                    return StringExpr.parse(remainingTokens, assignedSymbols)
                }

                GASNodeType.LABEL -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.SYMBOL && first.type != Token.Type.INT_DEC) return null
                    remainingTokens.removeFirst()
                    val second = remainingTokens.firstOrNull() ?: return null
                    if (second.content != ":") return null
                    return Label(first, second)
                }

                GASNodeType.ARG -> {
                    val spaces = mutableListOf<Token>()
                    spaces.addAll(remainingTokens.dropSpaces())

                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.SYMBOL) return null
                    remainingTokens.removeFirst()
                    spaces.addAll(remainingTokens.dropSpaces())


                    val second = remainingTokens.firstOrNull()
                    if (second == null || second.content != "=") return Argument.Basic(first)
                    remainingTokens.removeFirst()
                    spaces.addAll(remainingTokens.dropSpaces())

                    val third = buildNode(GASNodeType.ANY_EXPR, remainingTokens, allDirs, asmHeader)
                    if (third != null) {
                        return Argument.DefaultValue(first, second, third, spaces)
                    }

                    val thirdNotExpr = remainingTokens.firstOrNull()
                    return Argument.DefaultValue(first, second, if (thirdNotExpr != null) BaseNode(thirdNotExpr) else null, spaces)
                }

                GASNodeType.ARG_DEF -> {
                    val named = ArgDef.Named.rule.matchStart(remainingTokens, allDirs, asmHeader, assignedSymbols)
                    if (named.matches) {
                        return ArgDef.Named(named.matchingTokens[0], named.matchingTokens[1], named.matchingTokens.drop(2), named.ignoredSpaces)
                    }

                    val pos = ArgDef.Positional.rule.matchStart(remainingTokens, allDirs, asmHeader, assignedSymbols)
                    if (pos.matches) {
                        return ArgDef.Positional(pos.matchingTokens, pos.ignoredSpaces)
                    }

                    return null
                }
            }
        }

        private fun MutableList<Token>.checkLineBreak(): Token? {
            val first = this.firstOrNull() ?: return null
            if (first.type != Token.Type.LINEBREAK) {
                first.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_MISSING_LINEBREAK))
                return null
            }
            return first
        }

        fun MutableList<Token>.dropSpaces(): List<Token> {
            val dropped = mutableListOf<Token>()
            while (this.isNotEmpty()) {
                if (this.first().type != Token.Type.WHITESPACE) break
                dropped.add(this.removeFirst())
            }
            return dropped
        }
    }

    /**
     * [Root]
     * A RootNode only contains several [Statement]s.
     */
    class Root(vararg statements: Statement) : GASNode(*statements) {
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
    sealed class Statement(val label: Label?, lineBreak: Token, vararg childs: Node) : GASNode() {
        init {
            label?.let {
                addChild(it)
            }
            addChilds(*childs)
            addChild(BaseNode(lineBreak))
        }

        fun contentBackToString(): String = tokens().sortedBy { it.id }.joinToString("") { it.content }

        class Dir(label: Label?, val dir: Directive, lineBreak: Token) : Statement(label, lineBreak, dir)

        class Instr(label: Label?, val rawInstr: RawInstr, lineBreak: Token) : Statement(label, lineBreak, rawInstr)

        class Unresolved(label: Label?, lineTokens: List<Token>, lineBreak: Token) : Statement(label, lineBreak, *lineTokens.map { BaseNode(it) }.toTypedArray()) {
            init {
                lineTokens.firstOrNull()?.addSeverity(Severity.Type.ERROR, "Found unresolved statement!")
            }
        }

        class Empty(label: Label?, lineBreak: Token) : Statement(label, lineBreak)
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, val allTokens: List<Token> = listOf(), val additionalNodes: List<Node> = listOf()) : GASNode(*additionalNodes.toTypedArray()) {
        init {
            addChilds(*allTokens.map { BaseNode(it) }.toTypedArray())
        }
    }

    sealed class Argument(val argName: Token) : GASNode() {

        init {
            addChild(BaseNode(argName))
            argName.hl(CodeStyle.argument)
        }

        abstract fun getDefaultValue(): String

        class Basic(argName: Token) : Argument(argName) {
            override fun getDefaultValue(): String = ""
        }

        class DefaultValue(argName: Token, assignment: Token, private val expression: Node? = null, spaces: List<Token>) : Argument(argName) {
            init {
                expression?.let {
                    addChild(expression)
                }
                addChild(BaseNode(assignment))
                addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
                assignment.hl(CodeStyle.argument)
            }

            override fun getDefaultValue(): String = expression?.getContentAsString() ?: ""
        }
    }

    sealed class ArgDef(val content: List<Token>, spaces: List<Token>) : GASNode() {
        init {
            addChilds(*content.map { BaseNode(it) }.toTypedArray())
            addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
        }

        class Positional(content: List<Token>, spaces: List<Token>) : ArgDef(content, spaces) {
            companion object {
                val rule = Rule {
                    Seq(
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(Token.Type.LINEBREAK)))
                        }
                    )
                }
            }
        }

        class Named(val name: Token, assignment: Token, content: List<Token>, spaces: List<Token>) : ArgDef(content, spaces) {
            init {
                addChild(BaseNode(assignment))
                addChild(BaseNode(name))
            }

            companion object {
                val rule = Rule {
                    Seq(
                        InSpecific(Token.Type.SYMBOL),
                        Specific("="),
                        Repeatable {
                            Except(XOR(Specific(","), InSpecific(Token.Type.LINEBREAK)))
                        }
                    )
                }
            }
        }
    }

    class RawInstr(val instrName: Token, val remainingTokens: List<Token>) : GASNode() {
        var addr: Value? = null

        init {
            addChild(BaseNode(instrName))
            addChilds(*remainingTokens.map { BaseNode(it) }.toTypedArray())
        }
    }


    sealed class StringExpr(spaces: List<Token> = listOf(), vararg operands: StringExpr) : GASNode(*operands) {

        abstract fun evaluate(printErrors: Boolean): String

        init {
            addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
        }

        abstract fun replaceIdentifierWithExpr(assignedSymbols: List<GASParser.Symbol>)

        class Concatenation(spaces: List<Token>, private val exprs: Array<StringExpr>) : StringExpr(spaces, *exprs) {

            override fun evaluate(printErrors: Boolean): String = exprs.joinToString("") { it.evaluate(printErrors) }
            override fun replaceIdentifierWithExpr(assignedSymbols: List<GASParser.Symbol>) {
                exprs.forEach { expr ->
                    expr.replaceIdentifierWithExpr(assignedSymbols)
                }
            }
        }

        sealed class Operand(val token: Token) : StringExpr() {

            init {
                addChild(BaseNode(token))
            }

            class Identifier(val symbol: Token) : Operand(symbol) {
                private var expr: StringExpr? = null
                override fun evaluate(printErrors: Boolean): String {
                    val currExpr = expr

                    if (currExpr != null) {
                        return currExpr.evaluate(printErrors)
                    }

                    if (printErrors) throw Parser.ParserError(token, "Unknown String Identifier!")

                    return ""
                }

                override fun replaceIdentifierWithExpr(assignedSymbols: List<GASParser.Symbol>) {
                    val assignement = assignedSymbols.firstOrNull { it.name == symbol.content }

                    if (assignement == null) {
                        // symbol.addSeverity(Severity.Type.WARNING, "Symbol is undefined!")
                        return
                    }

                    when (assignement) {
                        is GASParser.Symbol.IntegerExpr -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a Integer to a String Identifier!")
                        }

                        is GASParser.Symbol.StringExpr -> {
                            symbol.removeSeverityIfError()
                            expr = assignement.expr
                            symbol.hl(CodeStyle.symbol)
                        }

                        is GASParser.Symbol.TokenRef -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a Token to a String Identifier!")
                        }

                        is GASParser.Symbol.Undefined -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a Undefined Symbol to a String Identifier!")
                        }
                    }
                }
            }

            class StringLiteral(val string: Token) : Operand(string) {
                private val stringContent: String = token.getContentAsString()
                override fun evaluate(printErrors: Boolean): String = stringContent
                override fun replaceIdentifierWithExpr(assignedSymbols: List<GASParser.Symbol>) {
                    /*Nothing*/
                }
            }
        }

        companion object {
            fun parse(tokens: List<Token>, assignedSymbols: List<GASParser.Symbol> = listOf(), allowSymbolsAsOperands: Boolean = true): StringExpr? {
                val relevantTokens = takeRelevantTokens(tokens, allowSymbolsAsOperands).toMutableList()
                val spaces = relevantTokens.filter { it.type == Token.Type.WHITESPACE }
                relevantTokens.removeAll(spaces)
                if (relevantTokens.isEmpty()) return null
                val operands: List<Operand> = relevantTokens.map {
                    when {
                        it.type == Token.Type.SYMBOL -> Operand.Identifier(it)
                        it.type.isStringLiteral -> Operand.StringLiteral(it)
                        else -> {
                            return null
                        }
                    }
                }
                if (operands.isEmpty()) return null

                val expr = if (operands.size == 1) {
                    operands.first()
                } else {
                    Concatenation(spaces, operands.toTypedArray())
                }
                expr.replaceIdentifierWithExpr(assignedSymbols)
                return expr
            }

            private fun takeRelevantTokens(tokens: List<Token>, allowSymbolsAsOperands: Boolean): List<Token> {
                return if (allowSymbolsAsOperands) {
                    tokens.takeWhile { it.type == Token.Type.SYMBOL || it.type.isStringLiteral || it.type == Token.Type.WHITESPACE }
                } else {
                    tokens.takeWhile { it.type.isStringLiteral || it.type == Token.Type.WHITESPACE }
                }
            }
        }
    }

    /**
     * [NumericExpr]
     *
     */
    sealed class NumericExpr(spaces: List<Token>, brackets: List<Token>, vararg operands: NumericExpr) : GASNode(*operands) {

        init {
            this.addChilds(*brackets.map { BaseNode(it) }.toTypedArray())
            this.addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
        }

        abstract fun evaluate(throwErrors: Boolean): Value.Dec

        abstract fun assignSymbols(assignedSymbols: List<GASParser.Symbol>)
        abstract fun isDefined(): Boolean
        abstract fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>)

        companion object {
            fun parse(tokens: List<Token>, assignedSymbols: List<GASParser.Symbol>, allowSymbolsAsOperands: Boolean = true): NumericExpr? {
                val relevantTokens = takeRelevantTokens(tokens, allowSymbolsAsOperands).toMutableList()
                val spaces = relevantTokens.filter { it.type == Token.Type.WHITESPACE }
                if (relevantTokens.lastOrNull()?.type?.isOpeningBracket == true) relevantTokens.removeLast()
                relevantTokens.removeAll(spaces)
                markPrefixes(relevantTokens)
                if (relevantTokens.isEmpty()) return null

                // Convert tokens to postfix notation
                val postFixTokens = convertToPostfix(relevantTokens)
                val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet(), spaces)
                expression?.assignSymbols(assignedSymbols)

                if (expression != null) {
                    postFixTokens.forEach {
                        if (!expression.tokens().contains(it)) {
                            throw Parser.ParserError(it, "Invalid Token ${it.type} for the Numeric Expression!")
                        }
                    }
                }

                if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                    "NumericExpr:" +
                            "\n\tRelevantTokens: ${relevantTokens.joinToString(" ") { it.content }}" +
                            "\n\tPostFixNotation: ${postFixTokens.joinToString(" ") { it.content }}" +
                            "\n\tExpression: ${expression?.print("")}"
                )

                return expression
            }

            private fun markPrefixes(tokens: List<Token>) {
                var i = 0
                while (i < tokens.size) {
                    val currentToken = tokens[i]
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
                        val nextMatchesPrefix = nextToken != null && (nextToken.type.isLiteral() || nextToken.type.isLinkableSymbol() || nextToken.content == "(")

                        val isPrefix = prevMatchesPrefix && nextMatchesPrefix
                        if (isPrefix) {
                            currentToken.markAsPrefix()
                            if (DebugTools.KIT_showPostFixExpressions) {
                                nativeLog("NumericExpr: Marked ${currentToken.content} as prefix!")
                            }
                        }
                    }
                    i++
                }
            }

            private fun buildExpressionFromPostfixNotation(tokens: MutableList<Token>, brackets: List<Token>, spaces: List<Token>): NumericExpr? {
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
                        uncheckedLast1.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERATOR))
                        return null
                    }
                }
                val uncheckedLast2 = tokens.lastOrNull() ?: return null
                val operandA = when {
                    uncheckedLast2.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, spaces)
                    uncheckedLast2.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())

                    else -> {
                        tokens.lastOrNull()?.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERAND))
                        return null
                    }
                } ?: return null
                if (operator.isPrefix()) {
                    return Prefix(operator, operandA, brackets, spaces)
                }

                val uncheckedLast3 = tokens.lastOrNull()
                val operandB = when {
                    uncheckedLast3 == null -> null
                    uncheckedLast3.type.isOperator -> buildExpressionFromPostfixNotation(tokens, brackets, spaces)
                    uncheckedLast3.type.isLinkableSymbol() -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast3.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast3.type.isCharLiteral -> Operand.Char(tokens.removeLast())
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets, spaces)
                } else null
            }

            private fun takeRelevantTokens(tokens: List<Token>, allowSymbolsAsOperands: Boolean): List<Token> {
                return if (allowSymbolsAsOperands) {
                    tokens.takeWhile { it.type.isOperator || it.type.isLinkableSymbol() || it.type.isNumberLiteral || it.type.isCharLiteral || it.type.isBasicBracket() || it.type == Token.Type.WHITESPACE }
                } else {
                    tokens.takeWhile { it.type.isOperator || it.type.isNumberLiteral || it.type.isCharLiteral || it.type.isBasicBracket() || it.type == Token.Type.WHITESPACE }
                }
            }

            /**
             * [convertToPostfix] converts infix notated expression to postfix notated expression
             *
             * Operators: [Token.Type.isOperator] -> Precedence through [Token.getPrecedence] which contains [Token.Precedence]
             * Operands: [Token.Type.isLiteral]
             * Brackets: [Token.Type.isBasicBracket]
             *
             */
            private fun convertToPostfix(infix: List<Token>): List<Token> {
                val output = mutableListOf<Token>()
                val operatorStack = mutableListOf<Token>()

                for (token in infix) {
                    if (DebugTools.KIT_showPostFixExpressions) nativeLog(
                        "PostFixIteration: for ${token.type}:${token.getPrecedence()} -> ${token.content}" +
                                "\n\tOutput: ${output.joinToString(" ") { it.content }}" +
                                "\n\tOperatorStack: ${operatorStack.joinToString("") { it.content }}"
                    )
                    if (token.type.isLiteral() || token.type.isLinkableSymbol()) {
                        output.add(token)
                        continue
                    }

                    if (token.type.isOperator) {
                        val higherOrEqualPrecedence = mutableListOf<Token>()
                        for (op in operatorStack) {
                            if (op.type.isOperator && op.higherOrEqualPrecedenceAs(token)) {
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

                    throw Parser.ParserError(token, "Token (${token::class.simpleName}: ${token.content}) is not valid in a numeric expression!")
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
        class Prefix(private val operator: Token, private val operand: NumericExpr, brackets: List<Token>, spaces: List<Token>) : NumericExpr(spaces, brackets, operand) {

            init {
                addChild(BaseNode(operator))
            }

            override fun evaluate(throwErrors: Boolean): Value.Dec {
                return when (operator.type) {
                    Token.Type.COMPLEMENT -> operand.evaluate(throwErrors).toBin().inv().toDec()
                    Token.Type.MINUS -> (-operand.evaluate(throwErrors)).toDec()
                    Token.Type.PLUS -> operand.evaluate(throwErrors)
                    else -> {
                        throw Parser.ParserError(operator, "$operator is not defined for this type of expression!")
                    }
                }
            }

            override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>) {
                operand.assignLabels(assigendLabels)
            }

            override fun isDefined(): Boolean = operand.isDefined()

            override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                operand.assignSymbols(assignedSymbols)
            }

            override fun print(prefix: String): String = "$prefix($operator${operand.print("")})"
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(private val operandA: NumericExpr, private val operator: Token, private val operandB: NumericExpr, brackets: List<Token>, spaces: List<Token>) : NumericExpr(spaces, brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

            override fun evaluate(throwErrors: Boolean): Value.Dec {
                return (when (operator.type) {
                    Token.Type.MULT -> operandA.evaluate(throwErrors) * operandB.evaluate(throwErrors)
                    Token.Type.DIV -> operandA.evaluate(throwErrors) * operandB.evaluate(throwErrors)
                    Token.Type.REM -> operandA.evaluate(throwErrors) % operandB.evaluate(throwErrors)
                    Token.Type.SHL -> operandA.evaluate(throwErrors).toBin() shl (operandB.evaluate(throwErrors).toUDec().toIntOrNull() ?: return operandA.evaluate(throwErrors))
                    Token.Type.SHR -> operandA.evaluate(throwErrors).toBin() shl (operandB.evaluate(throwErrors).toUDec().toIntOrNull() ?: return operandA.evaluate(throwErrors))
                    Token.Type.BITWISE_OR -> operandA.evaluate(throwErrors).toBin() or operandB.evaluate(throwErrors).toBin()
                    Token.Type.BITWISE_AND -> operandA.evaluate(throwErrors).toBin() and operandB.evaluate(throwErrors).toBin()
                    Token.Type.BITWISE_XOR -> operandA.evaluate(throwErrors).toBin() xor operandB.evaluate(throwErrors).toBin()
                    Token.Type.BITWISE_ORNOT -> operandA.evaluate(throwErrors).toBin() or operandB.evaluate(throwErrors).toBin().inv()
                    Token.Type.PLUS -> operandA.evaluate(throwErrors) + operandB.evaluate(throwErrors)
                    Token.Type.MINUS -> operandA.evaluate(throwErrors) - operandB.evaluate(throwErrors)
                    else -> {
                        throw Parser.ParserError(operator, "$operator is not defined for this type of expression!")
                    }
                }).toDec()
            }

            override fun print(prefix: String): String = "$prefix(${operandA.print("")} $operator ${operandB.print("")})"

            override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                operandA.assignSymbols(assignedSymbols)
                operandB.assignSymbols(assignedSymbols)
            }

            override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>) {
                operandA.assignLabels(assigendLabels)
                operandB.assignLabels(assigendLabels)
            }

            override fun isDefined(): Boolean = operandA.isDefined() && operandB.isDefined()
        }

        sealed class Operand(token: Token) : NumericExpr(brackets = listOf(), spaces = listOf()) {
            init {
                addChild(BaseNode(token))
            }

            class Identifier(val symbol: Token) : Operand(symbol) {
                private var expr: NumericExpr? = null
                private var labelAddr: Value? = null
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

                    if (throwErrors) throw Parser.ParserError(symbol, "Unknown Numeric Identifier!")
                    //symbol.addSeverity(Severity.Type.ERROR, "Can't evaluate from a undefined symbol. Returning 0.")
                    return Value.Dec("0", Size.Bit32)
                }

                override fun print(prefix: String): String = "$prefix<$symbol>"

                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    val assignment = assignedSymbols.firstOrNull { it.name == symbol.content }

                    if (assignment == null) {
                        return
                    }

                    when (assignment) {
                        is GASParser.Symbol.IntegerExpr -> {
                            symbol.removeSeverityIfError()
                            expr = assignment.expr
                            symbol.hl(CodeStyle.symbol)
                        }

                        is GASParser.Symbol.StringExpr -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a String to an Integer Identifier!")
                        }

                        is GASParser.Symbol.TokenRef -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a Token to an Integer Identifier!")
                        }

                        is GASParser.Symbol.Undefined -> {
                            symbol.addSeverity(Severity.Type.ERROR, "Unable to assign a Undefined Symbol to an Integer Identifier!")
                        }
                    }
                }

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>) {
                    val assignment = when (symbol.type) {
                        Token.Type.L_LABEL_REF -> {
                            val backwards = symbol.content.endsWith("b")
                            if (backwards) {
                                val lineLoc = symbol.lineLoc
                                val before = assigendLabels.filter {
                                    it.first.getFirstToken().lineLoc.file == lineLoc.file &&
                                            it.first.getFirstToken().lineLoc.lineID <= lineLoc.lineID
                                }.sortedBy { it.first.getFirstToken().lineLoc.lineID }
                                val id = symbol.content.removeSuffix("b")
                                before.lastOrNull { it.first.getID() == id }
                            } else {
                                val lineLoc = symbol.lineLoc
                                val after = assigendLabels.filter {
                                    it.first.getFirstToken().lineLoc.file == lineLoc.file &&
                                            it.first.getFirstToken().lineLoc.lineID >= lineLoc.lineID
                                }.sortedBy { it.first.getFirstToken().lineLoc.lineID }
                                val id = symbol.content.removeSuffix("f")
                                after.firstOrNull { it.first.getID() == id }
                            }
                        }

                        else -> assigendLabels.firstOrNull { it.first.getID() == symbol.content }
                    }

                    if (assignment == null) {
                        return
                    }

                    expr = null
                    labelAddr = assignment.second
                    symbol.hl(CodeStyle.label)
                }

                override fun isDefined(): Boolean = expr != null || labelAddr != null

            }

            class Number(val number: Token) : Operand(number) {
                val type: Type
                val value: Value.Dec

                init {
                    val intVal: Value?
                    val bigNum64: Value?
                    val bigNum128: Value?
                    when (number.type) {
                        Token.Type.INT_BIN -> {
                            val possibleInt = Value.Bin(number.onlyNumber, Size.Bit32)
                            intVal = if (possibleInt.getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum64 = Value.Bin(number.onlyNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum64.getBit(0)?.toRawString() == "1") null else possibleBigNum64

                            val possibleBigNum128 = Value.Bin(number.onlyNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_HEX -> {
                            val possibleInt = Value.Hex(number.onlyNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.Hex(number.onlyNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.Hex(number.onlyNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_OCT -> {
                            val possibleInt = Value.Oct(number.onlyNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.Oct(number.onlyNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.Oct(number.onlyNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_DEC -> {
                            val possibleInt = Value.UDec(number.onlyNumber, Size.Bit32)
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Value.UDec(number.onlyNumber, Size.Bit64)
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Value.UDec(number.onlyNumber, Size.Bit128)
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        else -> {
                            throw Parser.ParserError(number, "$number is not an Integer, BigNum64 or BigNum128.")
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
                        throw Parser.ParserError(number, "$number is not supported. Supported Types: Integer, BigNum64, BigNum128.")
                    }
                }

                override fun print(prefix: String): String = "$prefix$number"

                override fun evaluate(throwErrors: Boolean): Value.Dec = value

                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    // NOTHING
                }

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>) {
                    // NOTHING
                }

                override fun isDefined(): Boolean = true

                enum class Type {
                    Integer,
                    BigNum64,
                    BigNum128
                }
            }

            class Char(char: Token) : Operand(char) {

                val value: Value.Dec

                init {
                    val hexString = Value.Tools.asciiToHex(char.getContentAsString())
                    value = Value.Hex(hexString, Size.Bit32).toDec()
                }

                override fun evaluate(throwErrors: Boolean): Value.Dec = value
                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    // NOTHING
                }

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Value.Hex>>) {
                    // NOTHING
                }

                override fun print(prefix: String): String = "$prefix$value"
                override fun isDefined(): Boolean = true
            }
        }
    }


}







