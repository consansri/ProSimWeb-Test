package cengine.lang.asm.psi.instr

import cengine.lang.asm.psi.stmnt.AsmInstrStmnt
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmInstr(override val parent: AsmInstrStmnt,val operands: List<InstrOperand>, override val textRange: TextRange): PsiElement {
    override val children: List<PsiElement>
        get() = TODO("Not yet implemented")

    override fun accept(visitor: PsiElementVisitor) {
        TODO("Not yet implemented")
    }


}