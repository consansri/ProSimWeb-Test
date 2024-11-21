package cengine.lang.mif.ast

import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor

sealed class MifNode(override var range: IntRange, vararg children: PsiElement) : PsiElement {

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
        visitor.visitElement(this)
    }

    class Program(
        children: List<PsiElement>,
    ) : MifNode(children.minOf { it.range.first }..children.maxOf { it.range.last }, *children.toTypedArray()) {

        val headers: List<Header> = children.filterIsInstance<Header>()
        val content: Content? = children.firstOrNull { it is Content } as? Content?
        val ignored: List<MifToken> = children.filterIsInstance<MifToken>()

        override val pathName: String = "Program"

        companion object {
            fun parse(lexer: MifLexer): Program {
                val headers = mutableListOf<Header>()
                val contents = mutableListOf<Content>()
                val error = mutableListOf<Unrecognized>()

                while (true) {

                    val nextHeader = Header.parse(lexer)
                    if (nextHeader != null) {
                        headers.add(nextHeader)
                        continue
                    }

                    val content = Content.parse(lexer)
                    if (content != null) {
                        contents.add(content)
                        continue
                    }

                    val eof = lexer.consume(true)
                    if (eof.type != MifToken.Type.EOF && eof.value.isNotEmpty()) {
                        error.add(Unrecognized(eof))
                    } else {
                        contents.forEachIndexed { index, node ->
                            if (index > 0) {
                                node.addError("Content was already defined!")
                            }
                        }
                        return Program(headers + contents + error + lexer.ignored)
                    }
                }
            }
        }

        fun init() {

            val addr_radix = headers.lastOrNull { it.identifier.value.uppercase() == "ADDRESS_RADIX" }?.value?.value ?: "HEX"
            val data_radix = headers.lastOrNull { it.identifier.value.uppercase() == "DATA_RADIX" }?.value?.value ?: "HEX"




        }

    }

    class Header(val identifier: MifToken, val assign: MifToken, val value: MifToken, val semicolon: MifToken) : MifNode(identifier.range.first..semicolon.range.last, identifier, assign, value, semicolon) {
        override val pathName: String = "Header"

        init {
            when (identifier.value.uppercase()) {
                "DEPTH", "WIDTH" -> {
                    if (value.type != MifToken.Type.NUMBER) {
                        addError("expected a decimal number but received ${value.value}")
                    }
                }

                "ADDRESS_RADIX", "DATA_RADIX" -> {
                    if (value.type != MifToken.Type.RADIX) {
                        addError("expected a radix (BIN, DEC, HEX, OCT, UNS) but received ${value.value}")
                    }
                }

                else -> {
                    addError("Unknown Identifier ${identifier.value}")
                }
            }
        }

        companion object {
            fun parse(lexer: MifLexer): Header? {
                val start = lexer.position

                val identifier = lexer.consume(true)
                if (identifier.type != MifToken.Type.IDENTIFIER) {
                    lexer.position = start
                    return null
                }

                val assign = lexer.consume(true)
                if (assign.value != "=") {
                    lexer.position = start
                    return null
                }

                val value = lexer.consume(true)

                val semicolon = lexer.consume(true)
                if (semicolon.value != ";") {
                    lexer.position = start
                    return null
                }

                return Header(identifier, assign, value, semicolon)
            }
        }
    }

    class Content(val content: MifToken, val begin: MifToken, val assignments: Array<Assignment>, val end: MifToken, val semicolon: MifToken) : MifNode(content.range.first..semicolon.range.last, content, begin, *assignments, end, semicolon) {
        override val pathName: String = "Content"

        companion object {
            fun parse(lexer: MifLexer): Content? {
                val start = lexer.position
                val content = lexer.consume(true)

                if (content.value.lowercase() != "content") {
                    lexer.position = start
                    return null
                }

                val begin = lexer.consume(true)
                if (begin.value.lowercase() != "begin") {
                    lexer.position = start
                    return null
                }

                val assignments = mutableListOf<Assignment>()
                var nextAssignment = Assignment.parse(lexer)
                while (nextAssignment != null) {
                    assignments.add(nextAssignment)
                    nextAssignment = Assignment.parse(lexer)
                }

                val end = lexer.consume(true)
                if (end.value.lowercase() != "end") {
                    lexer.position = start
                    return null
                }

                val semicolon = lexer.consume(true)
                if (semicolon.value != ";") {
                    lexer.position = start
                    return null
                }

                return Content(content, begin, assignments.toTypedArray(), end, semicolon)
            }
        }
    }

    sealed class Assignment(range: IntRange, vararg others: PsiElement) : MifNode(range, *others) {
        companion object {
            fun parse(lexer: MifLexer): Assignment? {
                val start = lexer.position

                val addr = lexer.consume(true)

                when {
                    addr.type == MifToken.Type.NUMBER -> {

                        val colon = lexer.consume(true)
                        if (colon.value != ":") {
                            lexer.position = start
                            return null
                        }

                        val values = mutableListOf<MifToken>()
                        var next = lexer.consume(true)
                        while (next.type == MifToken.Type.NUMBER) {
                            values.add(next)
                            next = lexer.consume(true)
                        }

                        if (next.value != ";") {
                            lexer.position = start
                            return null
                        }

                        if (values.size == 1) {
                            return Direct(addr, colon, values.first(), next)
                        }

                        return ListOfValues(addr, colon, values.toTypedArray(), next)
                    }

                    addr.value == "[" -> {
                        lexer.position = start
                        val range = ValueRange.parse(lexer) ?: return null

                        val colon = lexer.consume(true)
                        if (colon.value != ":") {
                            lexer.position = start
                            return null
                        }

                        val data = lexer.consume(true)
                        if (data.type != MifToken.Type.NUMBER) {
                            lexer.position = start
                            return null
                        }

                        val semicolon = lexer.consume(true)
                        if (semicolon.value != ";") {
                            lexer.position = start
                            return null
                        }

                        return SingleValueRange(range, colon, data, semicolon)
                    }

                    else -> {
                        lexer.position = start
                        return null
                    }
                }
            }
        }

        class Direct(val addr: MifToken, val colon: MifToken, val data: MifToken, val semicolon: MifToken) : Assignment(addr.range.first..semicolon.range.last, addr, colon, data, semicolon) {
            override val pathName: String = "DirectAssignment"
        }

        class SingleValueRange(val valueRange: ValueRange, val colon: MifToken, val data: MifToken, val semicolon: MifToken) : Assignment(valueRange.range.first..semicolon.range.last, valueRange, colon, data, semicolon) {
            override val pathName: String = "RangeAssignment"
        }

        class ListOfValues(val addr: MifToken, val colon: MifToken, val data: Array<MifToken>, val semicolon: MifToken) : Assignment(addr.range.first..semicolon.range.last, addr, colon, *data, semicolon) {
            override val pathName: String = "ListAssignment"
        }
    }

    class ValueRange(val bracketOpen: MifToken, val first: MifToken, val rangeTo: MifToken, val last: MifToken, val bracketClose: MifToken) : MifNode(
        bracketOpen.range.first..bracketClose.range.last,
        bracketOpen, first, rangeTo, last, bracketClose
    ) {
        companion object {
            fun parse(lexer: MifLexer): ValueRange? {
                val startPosition = lexer.position

                val bracketOpen = lexer.consume(true)
                if (bracketOpen.value != "[") {
                    lexer.position = startPosition
                    return null
                }

                val firstAddr = lexer.consume(true)
                if (firstAddr.type != MifToken.Type.NUMBER) {
                    lexer.position = startPosition
                    return null
                }

                val rangeTo = lexer.consume(true)
                if (rangeTo.type != MifToken.Type.RANGE_TO) {
                    lexer.position = startPosition
                    return null
                }

                val lastAddr = lexer.consume(true)
                if (lastAddr.type != MifToken.Type.NUMBER) {
                    lexer.position = startPosition
                    return null
                }

                val bracketClose = lexer.consume(true)
                if (bracketClose.value != "]") {
                    lexer.position = startPosition
                    return null
                }

                return ValueRange(bracketOpen, firstAddr, rangeTo, lastAddr, bracketClose)
            }
        }

        override val pathName: String = "Range"
    }

    class Unrecognized(val token: MifToken) : MifNode(token.range) {
        override val pathName: String = "ERROR"

        init {
            addError("${token.value} was not expected!")
        }
    }


}