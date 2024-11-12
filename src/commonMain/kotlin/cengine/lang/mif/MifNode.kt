package cengine.lang.mif

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor

sealed class MifNode(override var range: IntRange, vararg children: PsiElement) : PsiElement {

    override var parent: PsiElement? = null

    final override val children: MutableList<PsiElement> = mutableListOf(*children)
    final override val annotations: MutableList<Annotation> = mutableListOf()
    override val additionalInfo: String
        get() = ""

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
    }

    companion object {
        fun parse(lexer: MifLexer): Program {

            TODO()


        }
    }


    class Program(
        children: Array<PsiElement>
    ): MifNode(children.minOf { it.range.first }..children.maxOf { it.range.last }, *children) {

        val headers: List<Header> = children.filterIsInstance<Header>()
        val content: Content? = children.firstOrNull{it is Content} as? Content?
        val ignored: List<MifToken> = children.filterIsInstance<MifToken>()

        override val pathName: String = "Program"
    }

    class Header(val identifier: MifToken, val assign: MifToken, val value: MifToken) : MifNode(identifier.range.first..value.range.last, identifier, assign, value) {
        override val pathName: String = "header"
    }

    class Content(val content: MifToken, val begin: MifToken, val assignments: Array<Assignment>, val end: MifToken, val semicolon: MifToken) : Assignment(content.range.first..semicolon.range.last, content, begin, *assignments, end, semicolon) {
        override val pathName: String = "content"
    }

    sealed class Assignment(range: IntRange, vararg others: PsiElement) : MifNode(range, *others) {
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

    class ValueRange(val bracketOpen: MifToken, val start: MifToken, val dot1: MifToken, val dot2: MifToken, val endInclusive: MifToken, val bracketClose: MifToken) : MifNode(
        bracketOpen.range.first..bracketClose.range.last,
        bracketOpen, start, dot1, dot2, endInclusive, bracketClose
    ) {
        override val pathName: String = "Range"
    }


}