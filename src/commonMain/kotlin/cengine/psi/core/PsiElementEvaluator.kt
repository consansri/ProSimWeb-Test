package cengine.psi.core

interface PsiElementEvaluator<T> {
    fun evaluate(element: PsiElement): T

}