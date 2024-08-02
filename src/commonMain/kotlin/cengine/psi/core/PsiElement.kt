package cengine.psi.core

/**
 * Base Element for all PSI elements
 */
interface PsiElement : Locatable {
    val parent: PsiElement?
    val children: List<PsiElement>

    override var textRange: TextRange

    fun print(prefix: String): String = "$prefix${this::class.simpleName}: \n" + ArrayList(children).joinToString("\n") { it.print(prefix + "\t") }

    fun moveTextRange(offset: TextPosition) {
        textRange = textRange.move(offset)
    }

    fun accept(visitor: PsiElementVisitor)
}