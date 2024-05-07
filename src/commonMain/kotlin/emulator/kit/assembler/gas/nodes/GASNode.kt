package emulator.kit.assembler.gas.nodes

import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.Rule
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.types.Variable
import emulator.kit.assembler.parser.NodeSeq.Component.*
import emulator.kit.assembler.parser.Parser
import emulator.kit.nativeError
import emulator.kit.nativeLog

/**
 * --------------------
 * 1. [Root]
 *   - [Section]
 *
 * --------------------
 * 2. [Section]
 *   - [Statement]
 * --------------------
 * 3.
 *
 */
sealed class GASNode(vararg childs: Node) : Node.HNode(*childs) {

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
         * Severities will be set by the Lowest Node which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly, assignedSymbols: List<GASParser.Symbol> = emptyList()): GASNode? {
            val remainingTokens = source.toMutableList()
            when (gasNodeType) {
                GASNodeType.ROOT -> {
                    val statements = mutableListOf<Statement>()
                    while (remainingTokens.isNotEmpty() && remainingTokens.firstOrNull { it.type == Token.Type.LINEBREAK } != null) {
                        val node = buildNode(GASNodeType.STATEMENT, remainingTokens, allDirs, definedAssembly)

                        if (node == null) {
                            val lineContent = remainingTokens.takeWhile { it.type != Token.Type.LINEBREAK }
                            lineContent.forEach { it.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_NOT_A_STATEMENT)) }
                            remainingTokens.removeAll(lineContent)
                            continue
                        }

                        if (node !is Statement) {
                            remainingTokens.removeAll(node.getAllTokens().toSet())
                            nativeError("Didn't get a statement node for GASNode.buildNode(${GASNodeType.STATEMENT})!")
                            continue
                        }

                        statements.add(node)
                        remainingTokens.removeAll(node.getAllTokens().toSet())
                    }

                    return Root(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val spaces = mutableListOf<Token>()
                    spaces.addAll(remainingTokens.dropSpaces())

                    val label = buildNode(GASNodeType.LABEL, remainingTokens, allDirs, definedAssembly) as? Label
                    if (label != null) {
                        remainingTokens.removeAll(label.getAllTokens().toSet())
                    }
                    spaces.addAll(remainingTokens.dropSpaces())

                    val directive = buildNode(GASNodeType.DIRECTIVE, remainingTokens, allDirs, definedAssembly)
                    if (directive != null && directive is Directive) {
                        remainingTokens.removeAll(directive.getAllTokens().toSet())
                        spaces.addAll(remainingTokens.dropSpaces())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        val node = Statement.Dir(label, directive, lineBreak)
                        node.addSpaces(spaces)
                        return node
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, remainingTokens, allDirs, definedAssembly)
                    if (instruction != null && instruction is RawInstr) {
                        remainingTokens.removeAll(instruction.getAllTokens().toSet())
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
                        val node = it.buildDirectiveContent(remainingTokens, allDirs, definedAssembly)
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

                GASNodeType.EXPRESSION_INTEGER -> {
                    return NumericExpr.parse(remainingTokens, assignedSymbols)
                }

                GASNodeType.EXPRESSION_ANY -> {
                    val stringExpr = StringExpr.parse(remainingTokens)
                    if (stringExpr != null) return stringExpr

                    val numericExpr = NumericExpr.parse(remainingTokens, assignedSymbols)
                    return numericExpr
                }

                GASNodeType.EXPRESSION_STRING -> {
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

                    val third = buildNode(GASNodeType.EXPRESSION_ANY, remainingTokens, allDirs, definedAssembly)
                    if (third != null) {
                        return Argument.DefaultValue(first, second, third, spaces)
                    }

                    val thirdNotExpr = remainingTokens.firstOrNull()
                    return Argument.DefaultValue(first, second, if (thirdNotExpr != null) BaseNode(thirdNotExpr) else null, spaces)
                }

                GASNodeType.ARG_DEF -> {
                    val named = ArgDef.Named.rule.matchStart(remainingTokens, allDirs, definedAssembly, assignedSymbols)
                    if (named.matches) {
                        return ArgDef.Named(named.matchingTokens[0], named.matchingTokens[1], named.matchingTokens.drop(2), named.ignoredSpaces)
                    }

                    val pos = ArgDef.Positional.rule.matchStart(remainingTokens, allDirs, definedAssembly, assignedSymbols)
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
     * - [Section]
     *
     *
     * [Root] has minimum 3 Sections
     *  - TEXT
     *  - DATA
     *  - BSS
     *
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
     *
     */
    sealed class Statement(val label: Label?, lineBreak: Token, vararg childs: Node) : GASNode() {
        init {
            label?.let {
                addChild(it)
            }
            addChilds(*childs)
            addChild(BaseNode(lineBreak))
        }

        fun contentBackToString(): String = getAllTokens().sortedBy { it.id }.joinToString("") { it.content }

        class Dir(label: Label?, val dir: Directive, lineBreak: Token) : Statement(label, lineBreak, dir) {
            fun resolve(macros: MutableList<GASParser.Macro>, sections: MutableList<GASParser.Section>) {

            }
        }

        class Instr(label: Label?, val rawInstr: RawInstr, lineBreak: Token) : Statement(label, lineBreak, rawInstr) {

        }

        class Unresolved(label: Label?, val lineTokens: List<Token>, lineBreak: Token) : Statement(label, lineBreak, *lineTokens.map { BaseNode(it) }.toTypedArray()) {
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

        class DefaultValue(argName: Token, val assignment: Token, val expression: Node? = null, val spaces: List<Token>) : Argument(argName) {
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

    sealed class ArgDef(val content: List<Token>, val spaces: List<Token>) : GASNode() {
        init {
            addChilds(*content.map { BaseNode(it) }.toTypedArray())
            addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
        }

        class Positional(content: List<Token>, spaces: List<Token>) : ArgDef(content, spaces) {
            companion object {
                val rule = Rule {
                    Rule.Component.Seq(
                        Rule.Component.Repeatable {
                            Rule.Component.Except(Rule.Component.XOR(Rule.Component.Specific(","), Rule.Component.InSpecific(Token.Type.LINEBREAK)))
                        }
                    )
                }
            }
        }

        class Named(val name: Token, val assignment: Token, content: List<Token>, spaces: List<Token>) : ArgDef(content, spaces) {
            init {
                addChild(BaseNode(assignment))
                addChild(BaseNode(name))
            }

            companion object {
                val rule = Rule {
                    Rule.Component.Seq(
                        Rule.Component.InSpecific(Token.Type.SYMBOL),
                        Rule.Component.Specific("="),
                        Rule.Component.Repeatable {
                            Rule.Component.Except(Rule.Component.XOR(Rule.Component.Specific(","), Rule.Component.InSpecific(Token.Type.LINEBREAK)))
                        }
                    )
                }
            }
        }
    }

    class RawInstr(val instrName: Token, val remainingTokens: List<Token>) : GASNode() {
        var addr: Variable.Value? = null

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

        class Concatenation(spaces: List<Token>, exprs: Array<StringExpr>) : StringExpr(spaces, *exprs) {
            val exprs: Array<StringExpr>

            init {
                this.exprs = exprs
            }

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
                var expr: StringExpr? = null
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

        abstract fun evaluate(throwErrors: Boolean): Variable.Value.Dec

        abstract fun assignSymbols(assignedSymbols: List<GASParser.Symbol>)
        abstract fun isDefined(): Boolean
        abstract fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>)

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

                if(expression != null){
                    postFixTokens.forEach {
                        if(!expression.getAllTokens().contains(it)){
                            throw Parser.ParserError(it, "Invalid Token ${it.type} for the Numeric Expression!")
                        }
                    }
                }

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
                        val prevMatchesPrefix: Boolean = previousToken == null || !(previousToken.type.isLiteral() || previousToken.type == Token.Type.SYMBOL)

                        /**
                         * Is Prefix if next is:
                         * - not null
                         * - a literal or a symbol or an opening bracket
                         */
                        val nextMatchesPrefix = nextToken != null && (nextToken.type.isLiteral() || nextToken.type == Token.Type.SYMBOL || nextToken.content == "(")

                        val isPrefix = prevMatchesPrefix && nextMatchesPrefix
                        if (isPrefix) currentToken.markAsPrefix()
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

                    uncheckedLast1.type == Token.Type.SYMBOL -> {
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
                    uncheckedLast2.type == Token.Type.SYMBOL -> Operand.Identifier(tokens.removeLast())
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
                    uncheckedLast3.type == Token.Type.SYMBOL -> Operand.Identifier(tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets, spaces)
                } else null
            }

            private fun takeRelevantTokens(tokens: List<Token>, allowSymbolsAsOperands: Boolean): List<Token> {
                return if (allowSymbolsAsOperands) {
                    tokens.takeWhile { it.type.isOperator || it.type == Token.Type.SYMBOL || it.type.isNumberLiteral || it.type.isCharLiteral || it.type.isBasicBracket() || it.type == Token.Type.WHITESPACE }
                } else {
                    tokens.takeWhile { it.type.isOperator || it.type.isNumberLiteral || it.type.isCharLiteral || it.type.isBasicBracket() || it.type == Token.Type.WHITESPACE }
                }
            }

            /**
             * [convertToPostfix] converts infix notated expression to postfix notated expression
             *
             * Operators: [Token.OPERATOR] -> Precedence through [Token.OPERATOR.operatorType] which contains [Token.OPERATOR.Precedence]
             * Operands: [Token.LITERAL]
             * Brackets: [Token.PUNCTUATION]
             *
             */
            private fun convertToPostfix(infix: List<Token>): List<Token> {
                val output = mutableListOf<Token>()
                val operatorStack = mutableListOf<Token>()

                for (token in infix) {
                    if (token.type.isLiteral() || token.type == Token.Type.SYMBOL) {
                        output.add(token)
                        continue
                    }

                    if (token.type.isOperator) {
                        val higherOrEqualPrecedence = mutableListOf<Token>()
                        for (op in operatorStack) {
                            if (op.type.isOperator && token.lowerOrEqualPrecedenceAs(op)) {
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

                    nativeError("Token (${token::class.simpleName}: ${token.content}) is not valid in a expression!")
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
        class Prefix(val operator: Token, val operand: NumericExpr, brackets: List<Token>, spaces: List<Token>) : NumericExpr(spaces, brackets, operand) {

            init {
                addChild(BaseNode(operator))
            }

            override fun evaluate(throwErrors: Boolean): Variable.Value.Dec {
                return when (operator.type) {
                    Token.Type.COMPLEMENT -> operand.evaluate(throwErrors).toBin().inv().toDec()
                    Token.Type.MINUS -> (-operand.evaluate(throwErrors)).toDec()
                    Token.Type.PLUS -> operand.evaluate(throwErrors)
                    else -> {
                        throw Parser.ParserError(operator, "$operator is not defined for this type of expression!")
                    }
                }
            }

            override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>) {
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
        class Classic(val operandA: NumericExpr, val operator: Token, val operandB: NumericExpr, brackets: List<Token>, spaces: List<Token>) : NumericExpr(spaces, brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

            override fun evaluate(throwErrors: Boolean): Variable.Value.Dec {
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

            override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>) {
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
                private var labelAddr: Variable.Value? = null
                fun isLinked(): Boolean = expr != null
                override fun evaluate(throwErrors: Boolean): Variable.Value.Dec {
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
                    return Variable.Value.Dec("0", Variable.Size.Bit32())
                }

                override fun print(prefix: String): String = "$prefix<$symbol>"

                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    val assignement = assignedSymbols.firstOrNull { it.name == symbol.content }

                    if (assignement == null) {
                        return
                    }

                    when (assignement) {
                        is GASParser.Symbol.IntegerExpr -> {
                            symbol.removeSeverityIfError()
                            expr = assignement.expr
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

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>) {
                    val assignment = assigendLabels.firstOrNull { it.first.label.identifier == symbol.content }

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
                val value: Variable.Value.Dec

                init {
                    val intVal: Variable.Value?
                    val bigNum64: Variable.Value?
                    val bigNum128: Variable.Value?
                    when (number.type) {
                        Token.Type.INT_BIN -> {
                            val possibleInt = Variable.Value.Bin(number.onlyNumber, Variable.Size.Bit32())
                            intVal = if (possibleInt.getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum64 = Variable.Value.Bin(number.onlyNumber, Variable.Size.Bit64())
                            bigNum64 = if (possibleBigNum64.getBit(0)?.toRawString() == "1") null else possibleBigNum64

                            val possibleBigNum128 = Variable.Value.Bin(number.onlyNumber, Variable.Size.Bit128())
                            bigNum128 = if (possibleBigNum128.getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_HEX -> {
                            val possibleInt = Variable.Value.Hex(number.onlyNumber, Variable.Size.Bit32())
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Variable.Value.Hex(number.onlyNumber, Variable.Size.Bit64())
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Variable.Value.Hex(number.onlyNumber, Variable.Size.Bit128())
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_OCT -> {
                            val possibleInt = Variable.Value.Oct(number.onlyNumber, Variable.Size.Bit32())
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Variable.Value.Oct(number.onlyNumber, Variable.Size.Bit64())
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Variable.Value.Oct(number.onlyNumber, Variable.Size.Bit128())
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        Token.Type.INT_DEC -> {
                            val possibleInt = Variable.Value.UDec(number.onlyNumber, Variable.Size.Bit32())
                            intVal = if (possibleInt.toBin().getBit(0)?.toRawString() == "1") null else possibleInt

                            val possibleBigNum = Variable.Value.UDec(number.onlyNumber, Variable.Size.Bit64())
                            bigNum64 = if (possibleBigNum.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum

                            val possibleBigNum128 = Variable.Value.UDec(number.onlyNumber, Variable.Size.Bit128())
                            bigNum128 = if (possibleBigNum128.toBin().getBit(0)?.toRawString() == "1") null else possibleBigNum128
                        }

                        else -> {
                            throw Parser.ParserError(number, "$number is not an Integer, BigNum64 or BigNum128.")
                        }
                    }

                    if (intVal?.checkResult?.valid == true) {
                        value = intVal.toDec()
                        type = Type.Integer
                    } else if (bigNum64?.checkResult?.valid == true) {
                        value = bigNum64.toDec()
                        type = Type.BigNum64
                    } else if (bigNum128?.checkResult?.valid == true) {
                        value = bigNum128.toDec()
                        type = Type.BigNum128
                    } else {
                        throw Parser.ParserError(number, "$number is not supported. Supported Types: Integer, BigNum64, BigNum128.")
                    }
                }

                override fun print(prefix: String): String = "$prefix$number"

                override fun evaluate(throwErrors: Boolean): Variable.Value.Dec = value

                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    // NOTHING
                }

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>) {
                    // NOTHING
                }

                override fun isDefined(): Boolean = true

                enum class Type {
                    Integer,
                    BigNum64,
                    BigNum128
                }
            }

            class Char(val char: Token) : Operand(char) {

                val value: Variable.Value.Dec

                init {
                    val hexString = Variable.Tools.asciiToHex(char.getContentAsString())
                    value = Variable.Value.Hex(hexString, Variable.Size.Bit32()).toDec()
                }

                override fun evaluate(throwErrors: Boolean): Variable.Value.Dec = value
                override fun assignSymbols(assignedSymbols: List<GASParser.Symbol>) {
                    // NOTHING
                }

                override fun assignLabels(assigendLabels: List<Pair<GASParser.Label, Variable.Value.Hex>>) {
                    // NOTHING
                }

                override fun print(prefix: String): String = "$prefix$value"
                override fun isDefined(): Boolean = true
            }
        }
    }


}







