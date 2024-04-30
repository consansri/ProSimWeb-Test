package emulator.kit.compiler.gas.nodes

import emulator.kit.compiler.DirTypeInterface
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
        fun buildNode(gasNodeType: GASNodeType, source: List<Token>, definedAssembly: DefinedAssembly): Node? {
            val remainingTokens = source.toMutableList()
            val node = when (gasNodeType) {
                GASNodeType.ROOT -> {
                    val statements = mutableListOf<Statement>()
                    while (remainingTokens.isNotEmpty()) {
                        val node = buildNode(GASNodeType.STATEMENT, remainingTokens, definedAssembly)

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

                    Root(*statements.toTypedArray())
                }

                GASNodeType.STATEMENT -> {
                    val label = buildNode(GASNodeType.LABEL, remainingTokens, definedAssembly) as? Label
                    if (label != null) {
                        remainingTokens.removeAll(label.getAllTokens().toSet())
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
                    if (first.type != Token.Type.DIRECTIVE) return null
                    remainingTokens.remove(first)
                    definedAssembly.getAdditionalDirectives().forEach {
                        if (".${it.getDetectionString().uppercase()}" == first.content.uppercase()) {
                            val node = it.buildDirectiveContent(first, remainingTokens, definedAssembly)
                            return node
                        }
                    }
                    return null
                }

                GASNodeType.INSTRUCTION -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.INSTRNAME) {
                        return null
                    }
                    remainingTokens.removeFirst()
                    definedAssembly.parseInstrParams(first, remainingTokens)
                }

                GASNodeType.IDENTIFIER -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.SYMBOL) {
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

                GASNodeType.LABEL -> {
                    val first = remainingTokens.firstOrNull() ?: return null
                    if (first.type != Token.Type.SYMBOL && first.type != Token.Type.INT_DEC) return null
                    remainingTokens.removeFirst()
                    val second = remainingTokens.firstOrNull() ?: return null
                    if (second.content != ":") return null
                    Label(first, second)
                }
            }
            return node
        }

        fun MutableList<Token>.checkLineBreak(): Token? {
            val first = this.firstOrNull() ?: return null
            if (first.type != Token.Type.LINEBREAK) {
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
    sealed class Statement(val label: Label?, lineBreak: Token, vararg childs: Node) : GASNode() {
        init {
            label?.let {
                addChild(it)
            }
            addChilds(*childs)
            addChild(BaseNode(lineBreak))
        }

        class DirType(label: Label?, val directive: Directive, lineBreak: Token) : Statement(label, lineBreak, directive)

        class InstrType(label: Label?, val instruction: Instr, lineBreak: Token) : Statement(label, lineBreak, instruction)

        class Empty(label: Label?, lineBreak: Token) : Statement(label, lineBreak)
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, val dirName: Token, val allTokens: List<Token> = listOf(), val additionalNodes: List<Node> = listOf()) : GASNode(*additionalNodes.toTypedArray()) {

        init {
            addChild(BaseNode(dirName))
            addChilds(*allTokens.map { BaseNode(it) }.toTypedArray())
        }
    }

    abstract class Instr(val instrName: Token, allTokens: List<Token>, allNodes: List<Node>) : GASNode() {
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
    class Identifier(val symbolName: Token, val equalOperator: Token? = null, val assignement: Expression? = null) : GASNode() {

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

            private fun buildExpressionFromPostfixNotation(type: Type, tokens: MutableList<Token>, brackets: List<Token>): Expression? {
                val uncheckedLast1 = tokens.removeLast()
                val operator = when {
                    uncheckedLast1.type.isOperator -> {
                        uncheckedLast1
                    }

                    uncheckedLast1.type == Token.Type.SYMBOL -> {
                        return Operand.Identifier(type, uncheckedLast1)
                    }

                    uncheckedLast1.type.isNumberLiteral -> {
                        return Operand.Number(uncheckedLast1)
                    }

                    uncheckedLast1.type.isStringLiteral -> {
                        return Operand.String(uncheckedLast1)
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
                    uncheckedLast2.type.isOperator -> buildExpressionFromPostfixNotation(type, tokens, brackets)
                    uncheckedLast2.type == Token.Type.SYMBOL -> Operand.Identifier(type, tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isStringLiteral -> Operand.String(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())

                    else -> {
                        tokens.lastOrNull()?.addSeverity(Severity(Severity.Type.ERROR, Severity.MSG_EXPRESSION_TOKEN_IS_NOT_AN_OPERAND))
                        return null
                    }
                } ?: return null

                if (operator.isPrefix()) {
                    return Prefix(operator, operandA, brackets)
                }

                val uncheckedLast3 = tokens.lastOrNull()
                val operandB = when {
                    uncheckedLast3 == null -> null
                    uncheckedLast3.type.isOperator -> buildExpressionFromPostfixNotation(type, tokens, brackets)
                    uncheckedLast3.type == Token.Type.SYMBOL -> Operand.Identifier(type, tokens.removeLast())
                    uncheckedLast2.type.isNumberLiteral -> Operand.Number(tokens.removeLast())
                    uncheckedLast2.type.isStringLiteral -> Operand.String(tokens.removeLast())
                    uncheckedLast2.type.isCharLiteral -> Operand.Char(tokens.removeLast())
                    else -> null
                }

                return if (operandB != null) {
                    Classic(operandB, operator, operandA, brackets)
                } else null
            }

            private fun takeRelevantTokens(tokens: List<Token>): List<Token> {
                return tokens.takeWhile { it.type.isOperator || it.type == Token.Type.SYMBOL || it.type.isLiteral() || it.type.isBasicBracket() }
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

                    if(token.type.isPunctuation){
                        if(token.type.isOpeningBracket){
                            operatorStack.add(token)
                            continue
                        }
                        if(token.type.isClosingBracket){
                            var peekedOpToken = operatorStack.lastOrNull()
                            while(peekedOpToken != null){
                                if(peekedOpToken.type.isOpeningBracket){
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
        class Prefix(val operator: Token, val operand: Expression, brackets: List<Token>) : Expression(Type.ABSOLUTE, brackets, operand) {
            init {
                addChild(BaseNode(operator))
            }

            override fun getType(): Type = operand.getType()

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
        class Classic(val operandA: Expression, val operator: Token, val operandB: Expression, brackets: List<Token>) : Expression(type = Type.ABSOLUTE, brackets, operandA, operandB) {

            init {
                addChild(BaseNode(operator))
            }

            override fun getType(): Type = operandA.getType()

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

        /**
         * [Empty]
         * - [lbracket] [operand] [rbracket]
         */
        class Empty(type: Type, val lbracket: Token, val operand: Expression, val rbracket: Token) : Expression(type, listOf(lbracket, rbracket), operand) {
            override fun getType(): Type = operand.getType()

            override fun getValue(size: Variable.Size?): Variable.Value {
                return operand.getValue(size)
            }
        }

        sealed class Operand(type: Type, token: Token) : Expression(type, brackets = listOf()) {
            init {
                addChild(BaseNode(token))
            }

            class Identifier(type: Type, val symbol: Token) : Operand(type, symbol) {
                var provider: Node? = null

                override fun getType(): Type {
                    TODO("Not yet implemented")
                }

                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO("Not yet implemented")
                }

            }

            class Number(val number: Token) : Operand(Type.ABSOLUTE, number) {
                override fun getType(): Type = Type.ABSOLUTE
                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO()
                }
            }

            class String(val string: Token) : Operand(Type.ABSOLUTE, string) {
                override fun getType(): Type {
                    TODO("Not yet implemented")
                }

                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO("Not yet implemented")
                }

            }

            class Char(val char: Token) : Operand(Type.STRING, char) {
                override fun getType(): Type = Type.STRING
                override fun getValue(size: Variable.Size?): Variable.Value {
                    TODO()
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


    class ProviderNotLinkedYetException(expression: Expression) : Exception() {
        override val message: String = "${expression::class.simpleName}: Value provider wasn't linked yet!"
    }

}







