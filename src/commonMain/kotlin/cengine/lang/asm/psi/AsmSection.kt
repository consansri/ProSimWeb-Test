package cengine.lang.asm.psi

import cengine.lang.asm.psi.stmnt.AsmStatement
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

class AsmSection(override val parent: AsmProgram?, override val children: List<AsmStatement>, override val textRange: TextRange) : PsiElement {
    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }
}