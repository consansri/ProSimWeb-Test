package emulator.kit.compiler.gas.nodes

import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.lexer.Severity
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import emulator.kit.types.Variable
import emulator.kit.compiler.lexer.TokenSeq.Component.InSpecific.*
import emulator.kit.compiler.parser.NodeSeq.Component.*
import emulator.kit.nativeError

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
    companion object {
        /**
         * Severities will be set by the Lowest Node which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, definedAssembly: DefinedAssembly): Node? {
            val remainingTokens = source.toMutableList()
            val node = when (gasNodeType) {
                GASNodeType.ROOT -> {
                    val statements = mutableListOf<Statement>()
                    while (remainingTokens.isNotEmpty()) {
                        val node = buildNode(GASNodeType.STATEMENT, remainingTokens, definedAssembly)

                        if (node == null) {
                            val lineContent = remainingTokens.takeWhile { it !is Token.LINEBREAK }
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

                    Root(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val first = remainingTokens.first()
                    val label = first as? Token.LABEL
                    label?.let {
                        remainingTokens.remove(it)
                    }

                    val directive = buildNode(GASNodeType.DIRECTIVE, remainingTokens, definedAssembly)
                    if (directive != null && directive is Directive) {
                        remainingTokens.removeAll(directive.getAllTokens().toSet())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        return Statement.DirType(label, directive, lineBreak)
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, remainingTokens, definedAssembly)
                    if (instruction != null && instruction is Instr) {
                        remainingTokens.removeAll(instruction.getAllTokens().toSet())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        return Statement.InstrType(label, instruction, lineBreak)
                    }

                    val lineBreak = remainingTokens.checkLineBreak() ?: return null
                    Statement.Empty(label, lineBreak)
                }

                GASNodeType.DIRECTIVE -> {
                    val first = remainingTokens.removeFirstOrNull() ?: return null
                    val directiveToken = first as? Token.KEYWORD.Directive
                    if (directiveToken == null) {
                        return null
                    }
                    remainingTokens.remove(directiveToken)
                    val node = directiveToken.dirType.buildDirectiveContent(directiveToken, remainingTokens, definedAssembly)
                    node
                }

                GASNodeType.INSTRUCTION -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first !is Token.KEYWORD.InstrName) {
                        return null
                    }
                    remainingTokens.removeFirst()
                    definedAssembly.parseInstrParams(first, remainingTokens)
                }

                GASNodeType.IDENTIFIER -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first !is Token.SYMBOL) {
                        return null
                    }
                    remainingTokens.removeFirst()
                    val assignmentOperator = remainingTokens.firstOrNull()
                    if (assignmentOperator == null || assignmentOperator.content != "=") {
                        Identifier(first)
                    } else {
                        remainingTokens.removeFirst()
                        val literal = buildNode(GASNodeType.EXPRESSION_ANY, source, definedAssembly)
                        if (literal == null || literal !is Expression) {
                            literal?.getAllTokens()?.firstOrNull()?.addSeverity(Severity(Severity.Type.ERROR, "Expected a expression for the symbol assignment!"))
                            Identifier(first)
                        } else {
                            Identifier(first, assignmentOperator, literal)
                        }
                    }
                }

                GASNodeType.EXPRESSION_ABS -> {
                    val expression = Expression.parse(Expression.Type.ABSOLUTE, remainingTokens)
                    expression?.let {
                        remainingTokens.removeAll(it.getAllTokens().toSet())
                    }
                    expression
                }

                GASNodeType.EXPRESSION_ANY -> {
                    val expression = Expression.parse(Expression.Type.ANY, remainingTokens)
                    expression?.let {
                        remainingTokens.removeAll(it.getAllTokens().toSet())
                    }
                    expression
                }

                GASNodeType.EXPRESSION_STRING -> {
                    val expression = Expression.parse(Expression.Type.STRING, remainingTokens)
                    expression?.let {
                        remainingTokens.removeAll(it.getAllTokens().toSet())
                    }
                    expression
                }
            }
            return node
        }

        fun MutableList<Token>.checkLineBreak(): Token.LINEBREAK? {
            val first = this.firstOrNull() ?: return null
            if (first !is Token.LINEBREAK) {
                first.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_MISSING_LINEBREAK))
                return null
            }
            return first
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
    sealed class Statement(val label: Token.LABEL?, lineBreak: Token.LINEBREAK, vararg childs: Node) : GASNode(), Provider {
        init {
            label?.let {
                addChild(BaseNode(it))
            }
            addChilds(*childs)
            addChild(BaseNode(lineBreak))
        }

        override val identifier: String? = when(label){
            is Token.LABEL.Basic -> label.identifier
            is Token.LABEL.Local -> label.identifier.toString()
            null -> null
        }

        override fun getType(): Expression.Type = Expression.Type.ABSOLUTE

        override fun getValue(size: Variable.Size?): Variable.Value {
            TODO("Not yet implemented")
        }

        class DirType(label: Token.LABEL?, val directive: Directive, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak, directive)

        class InstrType(label: Token.LABEL?, val instruction: Instr, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak, instruction)

        class Empty(label: Token.LABEL?, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak)
    }

    /**
     * Directive
     */
    class Directive(val dirName: Token.KEYWORD.Directive, val allTokens: List<Token> = listOf(), val additionalNodes: List<Node> = listOf()) : GASNode(*additionalNodes.toTypedArray()) {
        val type = dirName.dirType

        init {
            addChild(BaseNode(dirName))
            addChilds(*allTokens.map { BaseNode(it) }.toTypedArray())
        }
    }

    abstract class Instr(val instrName: Token.KEYWORD.InstrName, allTokens: List<Token>, allNodes: List<Node>) : GASNode() {
        abstract fun getWidth(): Variable.Size
        var addr: Variable.Value? = null

        init {
            addChild(BaseNode(instrName))
            addChilds(*allNodes.toTypedArray())
            val notYetAdded = allTokens.filter { !this.getAllTokens().contains(it) }
            addChilds(*notYetAdded.map { BaseNode(it) }.toTypedArray())
        }

    }

    /**
     * Symbol
     */
    class Identifier(val symbolName: Token.SYMBOL, val equalOperator: Token? = null, val assignement: Expression? = null) : GASNode(), Provider {
        var value: Expression.Value? = null
        override fun getType(): Expression.Type = value?.type ?: assignement?.getType() ?: Expression.Type.ANY

        override val identifier: String = symbolName.content
        override fun getValue(size: Variable.Size?): Variable.Value {
            TODO("Not yet implemented")
        }
    }

    /**
     * [Expression]
     *
     *
     *
     *
     */
    sealed class Expression(val type: Type, brackets: List<Token>, vararg operands: Expression) : GASNode(*operands) {

        init {
            for (bracket in brackets) {
                this.addChild(BaseNode(bracket))
            }
        }

        abstract fun getType(): Type

        abstract fun getValue(size: Variable.Size? = null): Variable.Value

        companion object {
            fun parse(type: Type, tokens: List<Token>): Expression? {
                val relevantTokens = takeRelevantTokens(tokens)
                markPrefixes(relevantTokens)
                if (relevantTokens.isEmpty()) return null

                // Convert tokens to postfix notation
                val postFixTokens = convertToPostfix(relevantTokens)
                val expression = buildExpressionFromPostfixNotation(type, postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet())
                return expression
            }

            private fun markPrefixes(tokens: List<Token>) {
                var i = 0
                while (i < tokens.size) {
                    val currentToken = tokens[i]
                    if (currentToken is Token.OPERATOR && currentToken.operatorType.couldBePrefix) {
                        // Check if '-' is a negation prefix
                        val previousToken = tokens.getOrNull(i - 1)
                        val nextToken = tokens.getOrNull(i + 1)
                        val previousMatchesPrefix = when (previousToken) {
                            is Token.LITERAL -> false
                            is Token.PUNCTUATION -> previousToken.isOpening()
                            else -> true
                        }
                        val nextMatchesPrefix = when (nextToken) {
                            is Token.LITERAL -> true
                            is Token.PUNCTUATION -> nextToken.isOpening()
                            else -> false
                        }
                        val isPrefix = previousMatchesPrefix && nextMatchesPrefix
                        if (isPrefix) currentToken.markAsPrefix()
                    }
                    i++
                }
            }

            private fun buildExpressionFromPostfixNotation(type: Type, tokens: MutableList<Token>, brackets: List<Token>): Expression? {
                val operator = when (val last = tokens.removeLast()) {
                    is Token.OPERATOR -> {
                        last
                    }

                    is Token.SYMBOL -> {
                        return Operand.Receiver(type, last)
                    }

                    is Token.LITERAL.NUMBER -> {
                        return Operand.Number(last)
                    }

                    is Token.LITERAL.CHARACTER -> {
                        return Operand.Char(last)
                    }

                    else -> {
                        last.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERATOR))
                        return null
                    }
                }
                val operandA = when (tokens.lastOrNull()) {
                    is Token.OPERATOR -> {
                        buildExpressionFromPostfixNotation(type, tokens, brackets)
                    }

                    is Token.SYMBOL -> Operand.Receiver(type, tokens.removeLast() as Token.SYMBOL)
                    is Token.LITERAL.NUMBER -> Operand.Number(tokens.removeLast() as Token.LITERAL.NUMBER)
                    else -> {
                        tokens.lastOrNull()?.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERAND))
                        return null
                    }
                } ?: return null

                if (operator.isPrefix()) {
                    return Prefix(operator, operandA, brackets)
                }

                val operandB = when (tokens.lastOrNull()) {
                    is Token.OPERATOR -> {
                        buildExpressionFromPostfixNotation(type, tokens, brackets)
                    }

                    is Token.SYMBOL -> Operand.Receiver(type, tokens.removeLast() as Token.SYMBOL)
                    is Token.LITERAL.NUMBER -> Operand.Number(tokens.removeLast() as Token.LITERAL.NUMBER)
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets)
                } else null
            }

            private fun takeRelevantTokens(tokens: List<Token>): List<Token> {
                return tokens.takeWhile { it is Token.OPERATOR || it is Token.SYMBOL || it is Token.LITERAL || (it is Token.PUNCTUATION && it.isBasicBracket()) }
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
                    when (token) {
                        is Token.LITERAL -> output.add(token)
                        is Token.OPERATOR -> {
                            val higherOrEqualPrecedence = mutableListOf<Token.OPERATOR>()
                            for (op in operatorStack) {
                                if (op is Token.OPERATOR && token.lowerOrEqualPrecedenceAs(op)) {
                                    output.add(op)
                                    higherOrEqualPrecedence.add(op)
                                }
                                if (op is Token.PUNCTUATION) break
                            }
                            operatorStack.removeAll(higherOrEqualPrecedence)
                            operatorStack.add(token)
                        }

                        is Token.PUNCTUATION -> {
                            if (token.isOpening()) operatorStack.add(token)
                            if (token.isClosing()) {
                                var peekedOpToken = operatorStack.lastOrNull()
                                while (peekedOpToken != null) {
                                    if (peekedOpToken is Token.PUNCTUATION && peekedOpToken.isOpening()) {
                                        operatorStack.removeLast()
                                        break
                                    }
                                    output.add(operatorStack.removeLast())
                                    peekedOpToken = operatorStack.lastOrNull()
                                }
                            }
                        }

                        else -> {
                            nativeError("Token (${token::class.simpleName}: ${token.content}) is not valid in a expression!")
                        }
                    }
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
        class Prefix(val operator: Token.OPERATOR, val operand: Expression, brackets: List<Token>) : Expression(Type.ABSOLUTE, brackets, operand) {
            init {
                addChild(BaseNode(operator))
            }

            override fun getType(): Type = operand.getType()

            override fun getValue(size: Variable.Size?): Variable.Value {
                return when (operator.operatorType) {
                    Token.OPERATOR.OperatorType.COMPLEMENT -> operand.getValue(size).toBin().inv()
                    Token.OPERATOR.OperatorType.MINUS -> -operand.getValue(size)
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
        class Classic(val operandA: Expression, val operator: Token.OPERATOR, val operandB: Expression, brackets: List<Token>) : Expression(type = Type.ABSOLUTE, brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

            override fun getType(): Type = operandA.getType()

            override fun getValue(size: Variable.Size?): Variable.Value {
                return when (operator.operatorType) {
                    Token.OPERATOR.OperatorType.MULT -> operandA.getValue(size) * operandB.getValue(size)
                    Token.OPERATOR.OperatorType.DIV -> operandA.getValue(size) * operandB.getValue(size)
                    Token.OPERATOR.OperatorType.REM -> operandA.getValue(size) % operandB.getValue(size)
                    Token.OPERATOR.OperatorType.SHL -> operandA.getValue(size).toBin() shl (operandB.getValue(size).toDec().toIntOrNull() ?: return operandA.getValue(size))
                    Token.OPERATOR.OperatorType.SHR -> operandA.getValue(size).toBin() shl (operandB.getValue(size).toDec().toIntOrNull() ?: return operandA.getValue(size))
                    Token.OPERATOR.OperatorType.BITWISE_OR -> operandA.getValue(size).toBin() or operandB.getValue(size).toBin()
                    Token.OPERATOR.OperatorType.BITWISE_AND -> operandA.getValue(size).toBin() and operandB.getValue(size).toBin()
                    Token.OPERATOR.OperatorType.BITWISE_XOR -> operandA.getValue(size).toBin() xor operandB.getValue(size).toBin()
                    Token.OPERATOR.OperatorType.BITWISE_ORNOT -> operandA.getValue(size).toBin() or operandB.getValue(size).toBin().inv()
                    Token.OPERATOR.OperatorType.PLUS -> operandA.getValue(size) + operandB.getValue(size)
                    Token.OPERATOR.OperatorType.MINUS -> operandA.getValue(size) - operandB.getValue(size)
                    else -> {
                        nativeError("$operator is not a Classic Expression operator!")
                        operandA.getValue(size)
                    }
                }
            }
        }

        /**
         * [Empty]
         * - [lbracket] [operand] [rbracket]
         */
        class Empty(type: Type, val lbracket: Token.PUNCTUATION, val operand: Expression, val rbracket: Token.PUNCTUATION) : Expression(type, listOf(lbracket, rbracket), operand) {
            override fun getType(): Type = operand.getType()

            override fun getValue(size: Variable.Size?): Variable.Value {
                return operand.getValue(size)
            }
        }

        sealed class Operand(type: Type, token: Token) : Expression(type, brackets = listOf()) {
            init {
                addChild(BaseNode(token))
            }

            class Receiver(type: Type, val symbol: Token.SYMBOL) : Operand(type, symbol), GASNode.Receiver {
                override var linkedProvider: Provider? = null
                override fun getType(): Type = linkedProvider?.getType() ?: Type.ANY
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return linkedProvider?.getValue(size) ?: throw ProviderNotLinkedYetException(this)
                }
            }

            class Number(val number: Token.LITERAL.NUMBER) : Operand(Type.ABSOLUTE, number) {
                override fun getType(): Type = Type.ABSOLUTE
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return number.getValue(size)
                }
            }

            class Char(val char: Token.LITERAL.CHARACTER) : Operand(Type.STRING, char) {
                override fun getType(): Type = Type.STRING
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return char.getValue(size)
                }
            }
        }

        enum class Type {
            ANY,
            STRING,
            ABSOLUTE
        }

        data class Value(val type: Type, val content: String)
    }

    interface Provider {
        val identifier: String?
        fun getValue(size: Variable.Size? = null): Variable.Value
        fun getType(): Expression.Type
    }

    interface Receiver {
        var linkedProvider: Provider?
    }

    class ValueNotLinkedYetException(provider: Provider) : Exception() {
        override val message: String = "${provider::class.simpleName}: Address wasn't linked yet!"
    }

    class ProviderNotLinkedYetException(expression: Expression) : Exception() {
        override val message: String = "${expression::class.simpleName}: Value provider wasn't linked yet!"
    }

}







