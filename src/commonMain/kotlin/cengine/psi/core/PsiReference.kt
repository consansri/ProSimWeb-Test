package cengine.psi.core


/**
 * Represents a reference to another PSI element
 *
 * @property element references [referencedElement]
 * @property referencedElement is what [element] references too
 */
interface PsiReference {
    val element: PsiElement
    val referencedElement: PsiElement?
    fun resolve(): PsiElement? = referencedElement
    fun isReferenceTo(element: PsiElement): Boolean = referencedElement == element
}