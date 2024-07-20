package cengine.lang.asm.psi.instr

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

class InstrReg(override val parent: AsmInstr, override val textRange: TextRange): InstrOperand() {

    override val children: List<PsiElement> = listOf()

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }
}