package emulator.kit.assembler.gas.nodes

import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Node
import emulator.kit.types.Variable
import emulator.kit.assembler.lexer.TokenSeq.Component.InSpecific.*
import emulator.kit.assembler.parser.NodeSeq.Component.*
import emulator.kit.nativeError
import emulator.kit.nativeLog
import emulator.kit.nativeWarn

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

        enum class Type {
            LOCAL,
            GLOBAL
        }
    }

    companion object {
        /**
         * Severities will be set by the Lowest Node which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, allDirs: List<DirTypeInterface>, definedAssembly: DefinedAssembly): GASNode? {
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
                    if (instruction != null && instruction is Instruction) {
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
                            nativeWarn("Found dir ${it.getDetectionString()}")
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
                    return definedAssembly.parseInstrParams(first, params)
                }

                GASNodeType.EXPRESSION_INTEGER -> {
                    return NumericExpr.parse(remainingTokens)
                }

                GASNodeType.EXPRESSION_ANY -> {
                    val stringExpr = StringExpr.parse(remainingTokens)
                    if (stringExpr != null) return stringExpr

                    return NumericExpr.parse(remainingTokens)
                }

                GASNodeType.EXPRESSION_STRING -> {
                    return StringExpr.parse(remainingTokens)
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
                    if (third == null) {
                        remainingTokens.firstOrNull()?.addSeverity(Severity.Type.ERROR, "Expected an expression!")
                        return Argument.Basic(first)
                    }

                    return Argument.DefaultValue(first, second, third, spaces)
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
    class Root(vararg val statements: Statement) : GASNode(*statements) {

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

        class Dir(label: Label?, val directive: Directive, lineBreak: Token) : Statement(label, lineBreak, directive)

        class Instr(label: Label?, val instruction: Instruction, lineBreak: Token) : Statement(label, lineBreak, instruction)
        class Unresolved(label: Label?, val lineTokens: List<Token>, lineBreak: Token) : Statement(label, lineBreak, *lineTokens.map { BaseNode(it) }.toTypedArray())

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

    sealed class ArgAssignment(val node: Node) : GASNode() {
        init {
            addChild(node)
        }

        class Direct(val token: Token) : ArgAssignment(BaseNode(token))

        class Expr(val expression: GASNode) : ArgAssignment(expression)

        class WithIdentifier(val argName: Token, val assignment: Token, exprOrBaseNode: Node) : ArgAssignment(exprOrBaseNode) {
            init {
                addChild(BaseNode(argName))
                addChild(BaseNode(assignment))
            }
        }

    }

    sealed class Argument(val argName: Token) : GASNode() {

        init {
            addChild(BaseNode(argName))
            argName.hl(CodeStyle.argument)
        }

        class Basic(argName: Token) : Argument(argName)

        class DefaultValue(argName: Token, val assignment: Token, val expression: GASNode, val spaces: List<Token>) : Argument(argName) {
            init {
                addChild(expression)
                addChild(BaseNode(assignment))
                addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
                assignment.hl(CodeStyle.argument)
            }
        }
    }

    abstract class Instruction(val instrName: Token, allTokens: List<Token>, allNodes: List<Node>) : GASNode() {
        abstract fun getWidth(): Variable.Size
        var addr: Variable.Value? = null

        init {
            addChild(BaseNode(instrName))
            addChilds(*allNodes.toTypedArray())
            val notYetAdded = allTokens.filter { !this.getAllTokens().contains(it) }
            addChilds(*notYetAdded.map { BaseNode(it) }.toTypedArray())
        }
    }


    sealed class StringExpr(spaces: List<Token> = listOf(), vararg operands: StringExpr) : GASNode(*operands) {

        abstract fun getValue(): String

        init {
            addChilds(*spaces.map { BaseNode(it) }.toTypedArray())
        }

        class Concatination(spaces: List<Token>, vararg val exprs: StringExpr) : StringExpr(spaces, *exprs) {
            override fun getValue(): String = exprs.joinToString("") { it.getValue() }
        }

        sealed class Operand(val token: Token) : StringExpr() {

            init {
                addChild(BaseNode(token))
            }

            class Identifier(val symbol: Token) : Operand(symbol) {
                override fun getValue(): String = TODO()
            }

            class StringLiteral(val string: Token) : Operand(string) {
                override fun getValue(): String = when (string.type) {
                    Token.Type.STRING_ML -> token.content.substring(3, token.content.length - 3)
                    Token.Type.STRING_SL -> token.content.substring(1, token.content.length - 1)
                    else -> ""
                }
            }
        }

        companion object {
            fun parse(tokens: List<Token>, allowSymbolsAsOperands: Boolean = true): StringExpr? {
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
                return if (operands.size == 1) {
                    operands.first()
                } else {
                    Concatination(spaces, *operands.toTypedArray())
                }
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

        abstract fun getValue(size: Variable.Size? = null): Variable.Value

        companion object {
            fun parse(tokens: List<Token>, allowSymbolsAsOperands: Boolean = true): NumericExpr? {
                val relevantTokens = takeRelevantTokens(tokens, allowSymbolsAsOperands).toMutableList()
                val spaces = relevantTokens.filter { it.type == Token.Type.WHITESPACE }
                relevantTokens.removeAll(spaces)
                markPrefixes(relevantTokens)
                if (relevantTokens.isEmpty()) return null

                // Convert tokens to postfix notation
                val postFixTokens = convertToPostfix(relevantTokens)
                val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet(), spaces)
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
                val uncheckedLast1 = tokens.removeLast()
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

            override fun getValue(size: Variable.Size?): Variable.Value {
                return when (operator.type) {
                    Token.Type.COMPLEMENT -> operand.getValue(size).toBin().inv()
                    Token.Type.MINUS -> -operand.getValue(size)
                    else -> {
                        nativeError("${operator} is not a Prefix operator!")
                        operand.getValue(size)
                    }
                }
            }
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(val operandA: NumericExpr, val operator: Token, val operandB: NumericExpr, brackets: List<Token>, spaces: List<Token>) : NumericExpr(spaces, brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

            override fun getValue(size: Variable.Size?): Variable.Value {
                return when (operator.type) {
                    Token.Type.MULT -> operandA.getValue(size) * operandB.getValue(size)
                    Token.Type.DIV -> operandA.getValue(size) * operandB.getValue(size)
                    Token.Type.REM -> operandA.getValue(size) % operandB.getValue(size)
                    Token.Type.SHL -> operandA.getValue(size).toBin() shl (operandB.getValue(size).toDec().toIntOrNull() ?: return operandA.getValue(size))
                    Token.Type.SHR -> operandA.getValue(size).toBin() shl (operandB.getValue(size).toDec().toIntOrNull() ?: return operandA.getValue(size))
                    Token.Type.BITWISE_OR -> operandA.getValue(size).toBin() or operandB.getValue(size).toBin()
                    Token.Type.BITWISE_AND -> operandA.getValue(size).toBin() and operandB.getValue(size).toBin()
                    Token.Type.BITWISE_XOR -> operandA.getValue(size).toBin() xor operandB.getValue(size).toBin()
                    Token.Type.BITWISE_ORNOT -> operandA.getValue(size).toBin() or operandB.getValue(size).toBin().inv()
                    Token.Type.PLUS -> operandA.getValue(size) + operandB.getValue(size)
                    Token.Type.MINUS -> operandA.getValue(size) - operandB.getValue(size)
                    else -> {
                        nativeError("$operator is not a Classic Expression operator!")
                        operandA.getValue(size)
                    }
                }
            }
        }

        sealed class Operand(token: Token) : NumericExpr(brackets = listOf(), spaces = listOf()) {
            init {
                addChild(BaseNode(token))
            }

            class Identifier(val symbol: Token) : Operand(symbol) {
                var provider: Node? = null

                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO("Not yet implemented")
                }

            }

            class Number(val number: Token) : Operand(number) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO()
                }
            }

            class Char(val char: Token) : Operand(char) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO()
                }
            }
        }
    }


    class ProviderNotLinkedYetException(numericExpr: NumericExpr) : Exception() {
        override val message: String = "${numericExpr::class.simpleName}: Value provider wasn't linked yet!"
    }

}







