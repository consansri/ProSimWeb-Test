package cengine.lang.asm.psi

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmProgram(
    override val children: List<PsiElement>,
    override val textRange: TextRange
): PsiElement {
    override val parent: PsiElement? = null

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}