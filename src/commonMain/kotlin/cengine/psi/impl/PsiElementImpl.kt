package cengine.psi.impl

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor

/**
 * Basic implementation of PsiElement
 */
abstract class PsiElementImpl: PsiElement {
    override val children: MutableList<PsiElement> = mutableListOf()

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
    }
}