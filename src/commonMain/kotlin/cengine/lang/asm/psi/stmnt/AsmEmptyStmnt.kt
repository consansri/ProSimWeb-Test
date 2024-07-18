package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.AsmSection
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmEmptyStmnt(override val parent: AsmSection?, val label: AsmLabel?, override val textRange: TextRange) : AsmStatement() {
    override val children: List<PsiElement> = listOfNotNull(label)

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}