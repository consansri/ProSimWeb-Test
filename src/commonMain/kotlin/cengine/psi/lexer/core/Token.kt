package cengine.psi.lexer.core

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor

/**
 * Interface representing a token in the source code.
 */
abstract class Token : PsiElement {
    /**
     * The type of the token.
     */
    abstract val type: TokenType

    /**
     * The content of the token.
     */
    abstract val value: String

    val start: Int
        get() = range.first

    val end: Int
        get() = range.last + 1

    final override val children: List<PsiElement>
        get() = emptyList()

    final override val annotations: List<Annotation>
        get() = emptyList()

    final override val additionalInfo: String
        get() = ""

    final override var parent: PsiElement? = null
    final override val pathName: String
        get() = value

    final override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
    }

    override fun print(prefix: String): String = "$prefix${type}: $value\n"

    final override fun equals(other: Any?): Boolean {
        if (other !is Token) return false

        if (other.type != type) return false
        if (other.value != value) return false
        if (other.range != range) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + range.hashCode()
        return result
    }


}
