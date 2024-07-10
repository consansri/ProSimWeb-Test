package cengine.psi.core

/**
 * Base Element for all PSI elements
 */
interface PsiElement {
    val parent: PsiElement?
    val children: List<PsiElement>
    val textRange: TextRange
    fun accept(visitor: PsiElementVisitor)
}