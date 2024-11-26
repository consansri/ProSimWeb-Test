package cengine.lang.asm.ast.impl

import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
import cengine.lang.asm.CodeStyle
import cengine.lang.asm.ast.*
import cengine.lang.asm.ast.Component.*
import cengine.lang.asm.ast.impl.ASNode.*
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmToken
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.obj.elf.*
import cengine.psi.core.*
import cengine.psi.feature.Highlightable
import cengine.psi.lexer.core.Token
import cengine.util.integer.*
import cengine.util.integer.Value.Companion.asBin
import cengine.util.integer.Value.Companion.asDec
import cengine.util.integer.Value.Companion.asHex
import cengine.util.integer.Value.Companion.asOct
import cengine.util.integer.Value.Companion.toValue
import debug.DebugTools
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
sealed class ASNode(override var range: IntRange, vararg children: PsiElement) : PsiElement, PsiFormatter {
    override var parent: PsiElement? = null

    final override val children: MutableList<PsiElement> = mutableListOf(*children)
    final override val annotations: MutableList<Annotation> = mutableListOf()

    override val additionalInfo: String
        get() = ""

    fun addError(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.error(this, message, execute))
    }

    fun addWarn(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.warn(this, message, execute))
    }

    fun addInfo(message: String, execute: (CodeEditor) -> Unit = {}) {
        annotations.add(Annotation.info(this, message, execute))
    }

    override fun accept(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
    }

    class Comment(val token: AsmToken) : ASNode(token.range), Highlightable {
        override val style: CodeStyle
            get() = CodeStyle.comment
        override val pathName: String
            get() = PATHNAME

        companion object {
            const val PATHNAME = "COMMENT"
        }

        override fun getFormatted(identSize: Int): String = token.value
    }

    class Error(val message: String, vararg val tokens: AsmToken) : ASNode(tokens.first().range.first..tokens.last().range.last), Highlightable {
        override val pathName: String
            get() = PATHNAME

        override val style: CodeStyle
            get() = CodeStyle.error

        companion object {
            const val PATHNAME = "ERROR"
        }

        init {
            addError(message)
        }

        override fun getFormatted(identSize: Int): String = tokens.joinToString(" ") { it.value }
    }

    companion object {
        /**
         * Severities will be set by the Lowest Node, which is actually checking the token.
         */
        fun buildNode(gasNodeType: ASNodeType, lexer: AsmLexer, targetSpec: TargetSpec<*>): ASNode? {
            val initialPos = lexer.position

            when (gasNodeType) {
                ASNodeType.PROGRAM -> {
                    val statements = mutableListOf<Statement>()
                    val annotations = mutableListOf<Annotation>()

                    while (lexer.hasMoreTokens()) {
                        val node = buildNode(ASNodeType.STATEMENT, lexer, targetSpec)

                        if (node == null) {
                            val token = lexer.consume(true)
                            annotations.add(Annotation.error(token, "Expected a Statement!"))
                            continue
                        }

                        if (node is Statement.Unresolved && node.lineTokens.isEmpty() && node.label == null) {
                            // node is empty
                            continue
                        }

                        if (node !is Statement) {
                            throw PsiParser.NodeException(node, "Didn't get a statement node for GASNode.buildNode(${ASNodeType.STATEMENT})!")
                        }

                        statements.add(node)
                    }

                    val node = Program(statements, lexer.ignored.map { Comment(it as AsmToken) })
                    node.annotations.addAll(annotations)
                    return node
                }

                ASNodeType.STATEMENT -> {
                    val label = buildNode(ASNodeType.LABEL, lexer, targetSpec) as? Label

                    val directive = buildNode(ASNodeType.DIRECTIVE, lexer, targetSpec)
                    if (directive != null && directive is Directive) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Dir(label, directive, lineBreak)
                            node.annotations.add(Annotation.error(lineBreak, "Linebreak is missing!"))
                            return node
                        }

                        return Statement.Dir(label, directive, lineBreak)
                    }

                    val instruction = buildNode(ASNodeType.INSTRUCTION, lexer, targetSpec)
                    if (instruction != null && instruction is Instruction) {
                        val lineBreak = lexer.consume(true)
                        if (lineBreak.type != AsmTokenType.LINEBREAK && lineBreak.type != AsmTokenType.EOF) {
                            val node = Statement.Instr(label, instruction, lineBreak)
                            node.annotations.add(Annotation.error(lineBreak, "Linebreak is missing!"))
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
                                node.annotations.add(Annotation.error(lineBreak, "Linebreak is missing!"))
                                return node
                            }

                            unresolvedTokens.add(token)
                        }

                        return Statement.Unresolved(label, unresolvedTokens, token)
                    }

                    return Statement.Empty(label, lineBreak)
                }

                ASNodeType.DIRECTIVE -> {
                    targetSpec.allDirs.forEach {
                        val node = it.buildDirectiveContent(lexer, targetSpec)
                        if (node != null) {
                            //nativeLog("Found directive ${it.getDetectionString()} ${node::class.simpleName}")
                            return node
                        }
                        lexer.position = initialPos
                    }
                    return null
                }

                ASNodeType.INSTRUCTION -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.INSTRNAME) {
                        lexer.position = initialPos
                        return null
                    }

                    val validTypes = targetSpec.allInstrs.filter { it.detectionName.lowercase() == first.value.lowercase() }
                    if (validTypes.isEmpty()) {
                        val node = Error("No valid instruction type found for $first.", first)
                        return node
                    }

                    validTypes.forEach {
                        val rule = it.paramRule ?: return Instruction(it, first, emptyList(), emptyList())
                        val result = rule.matchStart(lexer, targetSpec)
                        if (result.matches) {
                            return Instruction(it, first, result.matchingTokens, result.matchingNodes)
                        }
                    }

                    val node = Error("Invalid Arguments for valid types: ${validTypes.joinToString { it.typeName }}!", first)
                    return node
                }

                ASNodeType.INT_EXPR -> {
                    return NumericExpr.parse(lexer)
                }

                ASNodeType.ANY_EXPR -> {
                    val stringExpr = StringExpr.parse(lexer)
                    if (stringExpr != null) return stringExpr

                    val numericExpr = NumericExpr.parse(lexer)
                    return numericExpr
                }

                ASNodeType.STRING_EXPR -> {
                    return StringExpr.parse(lexer)
                }

                ASNodeType.LABEL -> {
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

                ASNodeType.ARG -> {
                    val first = lexer.consume(true)
                    if (first.type != AsmTokenType.SYMBOL) {
                        lexer.position = initialPos
                        return null
                    }

                    val second = lexer.consume(true)
                    if (second.value != "=") {
                        lexer.position = second.range.first
                        return Argument.Basic(first)
                    }

                    val third = buildNode(ASNodeType.ANY_EXPR, lexer, targetSpec)
                    if (third != null) {
                        return Argument.DefaultValue(first, second, third)
                    }

                    val thirdNotExpr = lexer.consume(true)
                    return Argument.DefaultValue(first, second, TokenExpr(thirdNotExpr))
                }

                ASNodeType.ARG_DEF -> {
                    val named = ArgDef.Named.rule.matchStart(lexer, targetSpec)
                    if (named.matches) {
                        return ArgDef.Named(named.matchingTokens[0], named.matchingTokens[1], named.matchingTokens.drop(2))
                    }

                    val pos = ArgDef.Positional.rule.matchStart(lexer, targetSpec)
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
    class Program(statements: List<Statement>, comments: List<Comment>) : ASNode(
        (statements.minByOrNull { it.range.first }?.range?.start ?: 0)..(statements.maxByOrNull { it.range.last }?.range?.last ?: 0),
        *(statements + comments).sortedBy { it.range.first }.toTypedArray()
    ) {
        override val pathName: String = this::class.simpleName.toString()

        init {
            //removeEmptyStatements()
        }

        override fun getFormatted(identSize: Int): String = children.joinToString("") { (it as? ASNode)?.getFormatted(identSize) ?: it.pathName }

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
    sealed class Statement(val label: Label?, val lineBreak: AsmToken, range: IntRange? = null, vararg childs: ASNode) : ASNode(
        range ?: (label?.range?.first ?: childs.firstOrNull()?.range?.first ?: lineBreak.range.first)..<(lineBreak.range.last + 1)
    ) {
        override val pathName: String get() = PATHNAME

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
                addError("Statement is unresolved!")
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

    class Label(val nameToken: AsmToken, val colon: AsmToken) : ASNode(nameToken.start..<colon.end), Highlightable {
        override val pathName get() = nameToken.value + colon.value
        val type = if (nameToken.type == AsmTokenType.INT_DEC) Type.NUMERIC else Type.ALPHANUMERIC
        val identifier = nameToken.value
        override val style: CodeStyle
            get() = CodeStyle.label

        init {
            children.add(nameToken)
            children.add(colon)
        }

        override fun getFormatted(identSize: Int): String = "${nameToken.value}${colon.value}"

        enum class Type {
            NUMERIC,
            ALPHANUMERIC
        }
    }

    /**
     * Directive
     */
    class Directive(val type: DirTypeInterface, val optionalIdentificationToken: AsmToken?, val allTokens: List<AsmToken> = listOf(), val additionalNodes: List<ASNode> = listOf()) : ASNode(
        (optionalIdentificationToken?.range?.start ?: allTokens.first().range.first)..maxOf(allTokens.lastOrNull()?.range?.start ?: 0, additionalNodes.lastOrNull()?.range?.last ?: 0),
        *additionalNodes.toTypedArray()
    ) {
        override val pathName: String get() = type.typeName
        override val additionalInfo: String
            get() = type.typeName + " " + optionalIdentificationToken

        private val sortedContent = (allTokens + additionalNodes).sortedBy { it.range.start }

        init {
            optionalIdentificationToken?.let {
                children.add(it)
            }
            children.addAll(allTokens)
        }

        override fun getFormatted(identSize: Int): String = (optionalIdentificationToken?.value ?: "") + sortedContent.joinToString("", " ") {
            when (it) {
                is ASNode -> it.getFormatted(identSize)
                is Token -> it.value
                else -> ""
            }
        }
    }

    sealed class Argument(val argName: AsmToken, range: IntRange) : ASNode(range), Highlightable {
        override val pathName: String
            get() = argName.value

        override val style: CodeStyle
            get() = CodeStyle.argument

        abstract fun getDefaultValue(): String

        class Basic(argName: AsmToken) : Argument(argName, argName.range) {
            override fun getDefaultValue(): String = ""

            override fun getFormatted(identSize: Int): String = argName.value
        }

        class DefaultValue(argName: AsmToken, val assignment: AsmToken, private val expression: ASNode? = null) : Argument(argName, argName.range.first..(expression?.range?.last ?: assignment.range.last)) {
            init {
                expression?.let {
                    children.add(expression)
                }
            }

            override fun getDefaultValue(): String = expression?.getFormatted(identSize = 4) ?: ""

            override fun getFormatted(identSize: Int): String = "${argName.value} ${assignment.value}${if (expression != null) " ${expression.getFormatted(identSize)}" else ""}"
        }
    }

    sealed class ArgDef(val content: List<AsmToken>, range: IntRange) : ASNode(range), Highlightable {

        override val style: CodeStyle
            get() = CodeStyle.argument

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

            override fun getFormatted(identSize: Int): String = "${nameToken.value} ${assignment.value} ${content.joinToString("") { it.value }}"
        }
    }

    class Instruction(val type: InstrTypeInterface, val instrName: AsmToken, val tokens: List<AsmToken>, val nodes: List<ASNode>) : ASNode(instrName.range.first..maxOf(tokens.lastOrNull()?.range?.last ?: 0, instrName.range.last, nodes.lastOrNull()?.range?.last ?: 0), *nodes.toTypedArray()) {
        override val pathName: String
            get() = instrName.value

        init {
            children.add(instrName)
            children.addAll(tokens)
        }

        override fun getFormatted(identSize: Int): String = "${instrName.value} ${
            (tokens + nodes).sortedBy { it.range.first }.joinToString("") {
                when (it) {
                    is AsmToken -> it.value
                    is ASNode -> it.getFormatted(identSize)
                    else -> ""
                }
            }
        }"
    }

    class TokenExpr(val token: AsmToken) : ASNode(token.range) {
        companion object {
            const val PATHNAME = "Token"
        }

        override val pathName: String
            get() = PATHNAME

        override fun getFormatted(identSize: Int): String = token.value
    }

    sealed class StringExpr(range: IntRange, vararg operands: StringExpr) : ASNode(range, *operands), Highlightable {

        override val style: CodeStyle
            get() = CodeStyle.string

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

                override val style: CodeStyle
                    get() = CodeStyle.symbol

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
    sealed class NumericExpr(val brackets: List<AsmToken>, range: IntRange, vararg operands: NumericExpr) : ASNode(range, *operands) {
        abstract var evaluated: Dec?

        companion object {
            fun parse(lexer: AsmLexer, allowSymbolsAsOperands: Boolean = true): ASNode? {
                val initialPos = lexer.position
                val relevantTokens = mutableListOf<AsmToken>()

                var openingBracketCount = 0
                var closingBracketCount = 0

                while (true) {
                    val token = lexer.consume(true)
                    when {
                        token.type.isOperator -> relevantTokens.add(token)
                        token.type.isNumberLiteral -> relevantTokens.add(token)
                        token.type.isCharLiteral -> relevantTokens.add(token)
                        token.type.isBasicBracket() -> {
                            if (token.type.isOpeningBracket) {
                                openingBracketCount++
                                relevantTokens.add(token)
                            } else {
                                if (openingBracketCount <= closingBracketCount) {
                                    break
                                }
                                closingBracketCount++
                                relevantTokens.add(token)
                            }
                        }

                        allowSymbolsAsOperands && token.type.isLinkableSymbol() -> relevantTokens.add(token)
                        else -> {
                            lexer.position = token.start
                            break
                        }
                    }
                }

                if (relevantTokens.lastOrNull()?.type?.isOpeningBracket == true) {
                    lexer.position = relevantTokens.removeLast().range.first
                }

                if (relevantTokens.isEmpty()) {
                    lexer.position = initialPos
                    return null
                }

                try {
                    val markedAsPrefix = computePrefixList(relevantTokens)

                    // Convert tokens to postfix notation
                    val postFixTokens = convertToPostfix(relevantTokens, markedAsPrefix)

                    val expression = buildExpressionFromPostfixNotation(postFixTokens.toMutableList(), relevantTokens - postFixTokens.toSet(), markedAsPrefix)

                    if (expression != null) {
                        val unusedTokens = (relevantTokens - postFixTokens).filter { !it.type.isBasicBracket() }
                        if (unusedTokens.isNotEmpty()) {
                            unusedTokens.forEach {
                                expression.annotations.add(Annotation.error(it, "Invalid Token $it for Numeric Expression!"))
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
                } catch (e: PsiParser.TokenException) {
                    return Error("Invalid Numeric Expression.", *relevantTokens.toTypedArray())
                }
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
         * @param builder is for storing relocation information.
         *
         */
        abstract fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit = {}): Dec
        abstract fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt)

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

            override var evaluated: Dec? = null

            override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): Dec {
                return when (operator.type) {
                    AsmTokenType.COMPLEMENT -> operand.evaluate(builder, createRelocations).toBin().inv().toDec()
                    AsmTokenType.MINUS -> (-operand.evaluate(builder, createRelocations)).toDec()
                    AsmTokenType.PLUS -> operand.evaluate(builder, createRelocations)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }.also { evaluated = it }
            }

            override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                operand.assign(symbols, section, offset)
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operator.value}${operand.getFormatted(identSize)}" else "${operator.value}(${operand.getFormatted(identSize)})"
        }

        /**
         * [Classic]
         * - [operandA] [operator] [operandB]
         */
        class Classic(val operandA: NumericExpr, val operator: AsmToken, val operandB: NumericExpr, brackets: List<AsmToken>) : NumericExpr(
            brackets,
            (brackets.firstOrNull()?.range?.first ?: operandA.range.first)..(brackets.lastOrNull()?.range?.last ?: operandB.range.last), operandA,
            operandB
        ) {
            override var evaluated: Dec? = null
            override val pathName: String
                get() = operator.value

            override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): Dec {
                return (when (operator.type) {
                    AsmTokenType.MULT -> operandA.evaluate(builder, createRelocations) * operandB.evaluate(builder, createRelocations)
                    AsmTokenType.DIV -> operandA.evaluate(builder, createRelocations) / operandB.evaluate(builder, createRelocations)
                    AsmTokenType.REM -> operandA.evaluate(builder, createRelocations) % operandB.evaluate(builder, createRelocations)
                    AsmTokenType.SHL -> operandA.evaluate(builder, createRelocations).toBin() shl (operandB.evaluate(builder, createRelocations).toUDec().toIntOrNull() ?: return operandA.evaluate(
                        builder,
                        createRelocations
                    ))

                    AsmTokenType.SHR -> operandA.evaluate(builder, createRelocations).toBin() shl (operandB.evaluate(builder, createRelocations).toUDec().toIntOrNull() ?: return operandA.evaluate(
                        builder,
                        createRelocations
                    ))

                    AsmTokenType.BITWISE_OR -> operandA.evaluate(builder, createRelocations).toBin() or operandB.evaluate(builder, createRelocations).toBin()
                    AsmTokenType.BITWISE_AND -> operandA.evaluate(builder, createRelocations).toBin() and operandB.evaluate(builder, createRelocations).toBin()
                    AsmTokenType.BITWISE_XOR -> operandA.evaluate(builder, createRelocations).toBin() xor operandB.evaluate(builder, createRelocations).toBin()
                    AsmTokenType.BITWISE_ORNOT -> operandA.evaluate(builder, createRelocations).toBin() or operandB.evaluate(builder, createRelocations).toBin().inv()
                    AsmTokenType.PLUS -> operandA.evaluate(builder, createRelocations) + operandB.evaluate(builder, createRelocations)
                    AsmTokenType.MINUS -> operandA.evaluate(builder, createRelocations) - operandB.evaluate(builder, createRelocations)
                    else -> {
                        throw PsiParser.NodeException(this, "$operator is not defined for this type of expression!")
                    }
                }).toDec().also { evaluated = it }
            }

            override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                operandA.assign(symbols, section, offset)
                operandB.assign(symbols, section, offset)
            }

            override fun getFormatted(identSize: Int): String = if (brackets.isEmpty()) "${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)}" else "(${operandA.getFormatted(identSize)} ${operator.value} ${operandB.getFormatted(identSize)})"
        }

        sealed class Operand(val token: AsmToken, range: IntRange) : NumericExpr(listOf(), range) {
            override val pathName: String
                get() = additionalInfo

            class Identifier(val symToken: AsmToken) : Operand(symToken, symToken.range), PsiReference, Highlightable {
                private var symbol: AsmCodeGenerator.Symbol<*>? = null

                override val additionalInfo: String
                    get() = token.value
                override val element: PsiElement = this
                override var referencedElement: ASNode? = null
                override var evaluated: Dec? = null

                override fun getFormatted(identSize: Int): String = symToken.value

                override val style: CodeStyle
                    get() = when (symbol) {
                        is AsmCodeGenerator.Symbol.Abs -> CodeStyle.symbol
                        is AsmCodeGenerator.Symbol.Label -> CodeStyle.label
                        null -> CodeStyle.BASE0
                    }

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): Dec {
                    val currSymbol = symbol

                    return when (currSymbol) {
                        is AsmCodeGenerator.Symbol.Abs -> currSymbol.value.toValue().toDec().also { evaluated = it }
                        is AsmCodeGenerator.Symbol.Label -> currSymbol.address().toDec().also { evaluated = it }
                        null -> {
                            createRelocations(symToken.value)
                            0.toValue().also { evaluated = it }
                        }
                    }
                }

                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {
                    val nameAssociated = symbols.firstOrNull {
                        it.name == symToken.value
                    }
                    if (nameAssociated != null) {
                        symbol = nameAssociated
                        return
                    }

                    if (symToken.value.endsWith("f")) {
                        val nextLocal = symbols.filterIsInstance<AsmCodeGenerator.Symbol.Label<*>>().firstOrNull {
                            it.local && it.section == section && it.offset >= offset
                        }
                        if (nextLocal != null) {
                            symbol = nextLocal
                            return
                        }
                    }

                    if (symToken.value.endsWith("b")) {
                        val lastLocal = symbols.filterIsInstance<AsmCodeGenerator.Symbol.Label<*>>().firstOrNull {
                            it.local && it.section == section && it.offset <= offset
                        }
                        if (lastLocal != null) {
                            symbol = lastLocal
                            return
                        }
                    }
                }
            }

            class Number(val number: AsmToken) : Operand(number, number.range), Highlightable {
                override val additionalInfo: String
                    get() = number.value
                override var evaluated: Dec? = null
                    set(value) {
                        field = value
                        //nativeLog("Operand.evaluate(): $value with Size ${value?.size}") // For DEBUGGING
                    }

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): Dec {
                    return (when (number.type) {
                        AsmTokenType.INT_DEC -> {
                            val auto = number.asNumber.asDec()
                            when {
                                auto.size == Size.Bit32 -> auto
                                auto.size == Size.Bit64 -> auto
                                auto.size == Size.Bit128 -> auto
                                auto.size.bitWidth < 32 -> number.asNumber.asDec(Size.Bit32)
                                auto.size.bitWidth < 64 -> number.asNumber.asDec(Size.Bit64)
                                auto.size.bitWidth < 128 -> number.asNumber.asDec(Size.Bit128)
                                else -> throw PsiParser.NodeException(this, "Integer ($number) of size ${auto.size} is not supported!")
                            }
                        }

                        AsmTokenType.INT_HEX -> {
                            val auto = number.asNumber.asHex()
                            when {
                                auto.size.bitWidth < 32 -> number.asNumber.asHex(Size.Bit32).toDec()
                                auto.size.bitWidth < 64 -> number.asNumber.asHex(Size.Bit64).toDec()
                                auto.size.bitWidth < 128 -> number.asNumber.asHex(Size.Bit128).toDec()
                                else -> throw PsiParser.NodeException(this, "Integer ($number) of size ${auto.size} is not supported!")
                            }
                        }

                        AsmTokenType.INT_OCT -> {
                            val auto = number.asNumber.asOct()
                            when {
                                auto.size.bitWidth < 32 -> number.asNumber.asOct(Size.Bit32).toDec()
                                auto.size.bitWidth < 64 -> number.asNumber.asOct(Size.Bit64).toDec()
                                auto.size.bitWidth < 128 -> number.asNumber.asOct(Size.Bit128).toDec()
                                else -> throw PsiParser.NodeException(this, "Integer ($number) of size ${auto.size} is not supported!")
                            }
                        }

                        AsmTokenType.INT_BIN -> {
                            val auto = number.asNumber.asBin()
                            when {
                                auto.size.bitWidth < 32 -> number.asNumber.asBin(Size.Bit32).toDec()
                                auto.size.bitWidth < 64 -> number.asNumber.asBin(Size.Bit64).toDec()
                                auto.size.bitWidth < 128 -> number.asNumber.asBin(Size.Bit128).toDec()
                                else -> throw PsiParser.NodeException(this, "Integer ($number) of size ${auto.size} is not supported!")
                            }
                        }

                        else -> throw PsiParser.NodeException(this, "$number is not a valid Int Literal!")
                    }).also { evaluated = it }
                }

                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {}

                override fun getFormatted(identSize: Int): String = number.value

                override val style: CodeStyle
                    get() = number.type.style ?: CodeStyle.integer
            }

            class Char(val char: AsmToken) : Operand(char, char.range), Highlightable {

                val value: Byte = char.getContentAsString().encodeToByteArray().first()
                override val additionalInfo: String
                    get() = char.value

                override var evaluated: Dec? = null
                override fun assign(symbols: Set<AsmCodeGenerator.Symbol<*>>, section: AsmCodeGenerator.Section, offset: UInt) {}

                override fun evaluate(builder: AsmCodeGenerator<*>, createRelocations: (String) -> Unit): Dec {
                    return char.getContentAsString().first().code.toValue().also { evaluated = it }
                }

                override fun getFormatted(identSize: Int): String = char.value

                override val style: CodeStyle
                    get() = CodeStyle.char
            }
        }
    }


}







