package cengine.psi.core


/**
 * Represents a reference to another PSI element
 */
interface PsiReference {
    val element: PsiElement
    val referencedElement: PsiElement?
    fun resolve(): PsiElement?
    fun isReferenceTo(element: PsiElement): Boolean
}