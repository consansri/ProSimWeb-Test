package cengine.psi.core

/**
 * Base class for visitors of the PSI tree
 */
abstract class PsiElementVisitor {
    open fun visitElement(visitedElement: PsiElement){
        visitedElement.children.forEach { it.accept(this) }
    }
    open fun visitFile(file: PsiFile) = visitElement(file)
}