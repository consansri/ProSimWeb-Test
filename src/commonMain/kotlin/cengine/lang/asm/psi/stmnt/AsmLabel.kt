package cengine.lang.asm.psi.stmnt

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmLabel(override val parent: AsmStatement, val name: String, override val textRange: TextRange) : PsiElement {
    override val children: List<PsiElement> = listOf()
    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}
