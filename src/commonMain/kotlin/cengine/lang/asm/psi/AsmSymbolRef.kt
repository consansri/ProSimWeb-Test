package cengine.lang.asm.psi

import cengine.lang.asm.psi.dir.AsmSymbol
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiReference
import cengine.psi.core.TextRange

data class AsmSymbolRef(override val parent: PsiElement?, val name: String, override val textRange: TextRange) : PsiElement, PsiReference {
    override val children: List<PsiElement> = listOf()

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }

    override val element: PsiElement = this
    override var referencedElement: PsiElement? = null

    override fun resolve(): PsiElement? {
        return referencedElement
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (element !is AsmSymbol) return false
        if (element.name == name) return true
        return false
    }


}
