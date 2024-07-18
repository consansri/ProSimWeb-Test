package cengine.psi.core

interface PsiElementVisitor {
    fun visitFile(file: PsiFile)
    fun visitElement(element: PsiElement)

}