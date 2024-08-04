package cengine.psi.core

import cengine.editor.annotation.Notation

/**
 * Base Element for all PSI elements
 */
interface PsiElement : Locatable {

    val parent: PsiElement?
    val children: List<PsiElement>
    val notations: List<Notation>

    override var textRange: TextRange

    fun print(prefix: String): String = "$prefix${this::class.simpleName}: \n" + ArrayList(children).joinToString("\n") { it.print(prefix + "\t") }

    fun moveTextRange(offset: TextPosition) {
        textRange = textRange.move(offset)
    }

    fun accept(visitor: PsiElementVisitor)
}