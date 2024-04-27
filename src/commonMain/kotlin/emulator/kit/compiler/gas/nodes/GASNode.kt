package emulator.kit.compiler.gas.nodes

import emulator.kit.compiler.gas.GASDirType
import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.lexer.Token
import emulator.kit.compiler.parser.Node
import emulator.kit.types.Variable
import emulator.kit.compiler.lexer.TokenSeq.Component.InSpecific.*
import emulator.kit.compiler.parser.NodeSeq.Component.*
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
    companion object {

        /**
         * Severities will be set by the Lowest Node which is actually checking the token.
         */
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, definedAssembly: DefinedAssembly): Node? {
            val remainingTokens = source.toMutableList()
            return when (gasNodeType) {
                GASNodeType.ROOT -> {
                    val statements = mutableListOf<Statement>()
                    while (remainingTokens.isNotEmpty()) {
                        val node = buildNode(GASNodeType.STATEMENT, remainingTokens, definedAssembly)

                        if (node == null) {
                            remainingTokens.dropWhile { it !is Token.LINEBREAK }
                            remainingTokens.removeFirst()
                            continue
                        }

                        if (node !is Statement) {
                            remainingTokens.removeAll(node.getAllTokens().toSet())
                            nativeError("Didn't get a statement node for GASNode.buildNode(${GASNodeType.STATEMENT})!")
                            continue
                        }

                        statements.add(node)
                        remainingTokens.removeAll(node.getAllTokens().toSet())
                        nativeLog("GASNode: Build Statement: ${node::class.simpleName}: ${node.getAllTokens().joinToString(" ") { it.content }}")
                    }

                    Root(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val first = remainingTokens.first()
                    val label = first as? Token.LABEL
                    label?.let {
                        nativeLog("Label is ${label::class.simpleName} -> ${label.content}")
                        remainingTokens.removeFirst()
                    }

                    val directive = buildNode(GASNodeType.DIRECTIVE, remainingTokens, definedAssembly)
                    if (directive != null && directive is Directive) {
                        nativeLog("Found directive: $directive")
                        remainingTokens.removeAll(directive.getAllTokens().toSet())
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        return Statement.DirType(label, directive, lineBreak)
                    }

                    val instruction = buildNode(GASNodeType.INSTRUCTION, remainingTokens, definedAssembly)
                    if (instruction != null && instruction is Instr) {
                        nativeLog("Found instruction: ${instruction.print("Instr: ")}\n\tTokens: ${instruction.getAllTokens().joinToString { it::class.simpleName.toString() }}")
                        remainingTokens.removeAll(instruction.getAllTokens().toSet())
                        nativeLog("Remaining: ${remainingTokens.joinToString { it::class.simpleName.toString() }}")
                        val lineBreak = remainingTokens.checkLineBreak() ?: return null
                        return Statement.InstrType(label, instruction, lineBreak)
                    }

                    val lineBreak = remainingTokens.checkLineBreak() ?: return null
                    return Statement.Empty(label, lineBreak)
                }

                GASNodeType.DIRECTIVE -> {
                    if (remainingTokens.isEmpty()) return null
                    val directiveToken = remainingTokens.first() as? Token.KEYWORD.Directive ?: return null
                    val type = GASDirType.entries.firstOrNull { ".${it.name.uppercase()}" == directiveToken.content.uppercase() } ?: return null

                    return null
                }

                GASNodeType.INSTRUCTION -> {
                    if (remainingTokens.isEmpty()) return null
                    val first = remainingTokens.first()
                    if (first !is Token.KEYWORD.InstrName) return null
                    remainingTokens.removeFirst()
                    return definedAssembly.parseInstrParams(first, remainingTokens)
                }

                GASNodeType.EXPRESSION -> {
                    val expression = Expression.parse(remainingTokens)
                    expression?.let {
                        remainingTokens.removeAll(it.getAllTokens().toSet())
                    }
                    expression
                }
            }
        }

        private fun MutableList<Token>.checkLineBreak(): Token.LINEBREAK? {
            val lineBreak = this.firstOrNull() as? Token.LINEBREAK
            if (lineBreak == null) {
                return null
            }
            this.removeFirst()
            return lineBreak
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
                nativeLog("Instantiating Statement with label: $label")
                addChild(BaseNode(it))
            }
            addChilds(*childs)
            addChild(BaseNode(lineBreak))
        }

        override fun getValue(size: Variable.Size?): Variable.Value {
            TODO("Not yet implemented")
        }

        class DirType(label: Token.LABEL?, val directive: Directive, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak, directive) {

        }

        class InstrType(label: Token.LABEL?, val instruction: Instr, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak, instruction) {

        }

        class Empty(label: Token.LABEL?, lineBreak: Token.LINEBREAK) : Statement(label, lineBreak) {

        }
    }

    /**
     * Directive
     */
    class Directive(val type: GASDirType) : GASNode() {

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
     * [Expression]
     *
     *
     *
     *
     */
    sealed class Expression(brackets: List<Token>, vararg operands: Expression) : GASNode(*operands) {
        init {
            for (bracket in brackets) {
                this.addChild(BaseNode(bracket))
            }
        }

        abstract fun getValue(size: Variable.Size? = null): Variable.Value

        companion object {
            fun parse(tokens: List<Token>): Expression? {
                nativeLog("Expression: for ${tokens.joinToString { it.content }}")
                val relevantTokens = takeRelevantTokens(tokens)
                markPrefixes(relevantTokens)
                nativeLog("Expression: Relevant Tokens: ${relevantTokens.joinToString { it.content }}")
                if (relevantTokens.isEmpty()) return null

                // Convert tokens to postfix notation
                val postFixTokens = convertToPostfix(relevantTokens)
                nativeLog("Expression: PostFix Tokens: ${postFixTokens.joinToString { it.content }}")
                val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet())
                nativeLog("Expression: ${expression?.print("")}")
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

            private fun buildExpressionFromPostfixNotation(tokens: MutableList<Token>, brackets: List<Token>): Expression? {
                val operator = when (val last = tokens.removeLast()) {
                    is Token.OPERATOR -> {
                        last
                    }

                    is Token.SYMBOL -> {
                        return Operand.Symbol(last)
                    }

                    is Token.LITERAL.NUMBER -> {
                        return Operand.Number(last)
                    }

                    is Token.LITERAL.CHARACTER -> {
                        return Operand.Char(last)
                    }

                    else -> return null
                }
                val operandA = when (tokens.last()) {
                    is Token.OPERATOR -> {
                        buildExpressionFromPostfixNotation(tokens, brackets)
                    }

                    is Token.SYMBOL -> Operand.Symbol(tokens.removeLast() as Token.SYMBOL)
                    is Token.LITERAL.NUMBER -> Operand.Number(tokens.removeLast() as Token.LITERAL.NUMBER)
                    else -> return null
                } ?: return null

                if (operator.isPrefix()) {
                    return Prefix(operator, operandA, brackets)
                }

                val operandB = when (tokens.lastOrNull()) {
                    is Token.OPERATOR -> {
                        buildExpressionFromPostfixNotation(tokens, brackets)
                    }

                    is Token.SYMBOL -> Operand.Symbol(tokens.removeLast() as Token.SYMBOL)
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

                nativeLog("Postfix Notation: ${output.joinToString(" ") { it.content }}")

                return output
            }
        }

        /**
         * [Prefix]
         * - [operator] [Operand]
         *
         */
        class Prefix(val operator: Token.OPERATOR, val operand: Expression, brackets: List<Token>) : Expression(brackets, operand) {
            init {
                addChild(BaseNode(operator))
            }

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
        class Classic(val operandA: Expression, val operator: Token.OPERATOR, val operandB: Expression, brackets: List<Token>) : Expression(brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

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
        class Empty(val lbracket: Token.PUNCTUATION, val operand: Expression, val rbracket: Token.PUNCTUATION) : Expression(listOf(lbracket, rbracket), operand) {
            override fun getValue(size: Variable.Size?): Variable.Value {
                return operand.getValue(size)
            }
        }

        sealed class Operand(token: Token) : Expression(brackets = listOf()) {
            init {
                addChild(BaseNode(token))
            }

            class Symbol(val symbol: Token.SYMBOL) : Operand(symbol) {
                var linkedProvider: Provider? = null
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return linkedProvider?.getValue(size) ?: throw ProviderNotLinkedYetException(this)
                }
            }

            class Number(val number: Token.LITERAL.NUMBER) : Operand(number) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return number.getValue(size)
                }
            }

            class Char(val char: Token.LITERAL.CHARACTER) : Operand(char) {
                override fun getValue(size: Variable.Size?): Variable.Value {
                    return char.getValue(size)
                }
            }
        }
    }

    interface Provider {
        fun getValue(size: Variable.Size? = null): Variable.Value
    }

    class ValueNotLinkedYetException(provider: Provider) : Exception() {
        override val message: String = "${provider::class.simpleName}: Address wasn't linked yet!"
    }

    class ProviderNotLinkedYetException(expression: Expression) : Exception() {
        override val message: String = "${expression::class.simpleName}: Value provider wasn't linked yet!"
    }

}







