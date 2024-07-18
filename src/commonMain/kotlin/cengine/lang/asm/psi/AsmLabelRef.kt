package cengine.lang.asm.psi

import cengine.lang.asm.psi.stmnt.AsmLabel
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiReference
import cengine.psi.core.TextRange

data class AsmLabelRef(override val parent: PsiElement, val name: String, override val textRange: TextRange) : PsiElement, PsiReference {
    override var referencedElement: PsiElement? = null
    override fun resolve(): PsiElement? {
        return referencedElement
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element !is AsmLabel) return false
        if (element.name.removeSuffix(":") == name) return true
        return false
    }

    override val element: PsiElement = this

    override val children: List<PsiElement> = listOf()

    override fun accept(visitor: PsiElementVisitor) {

    }

}