package cengine.lang.asm.psi.stmnt

import cengine.lang.asm.psi.instr.AsmInstr
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.TextRange

data class AsmInstrStmnt(val label: AsmLabel?, val instr: AsmInstr, override val textRange: TextRange): AsmStatement(){

    override val children: List<PsiElement> = listOfNotNull(label, instr)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
        children.forEach {
            it.accept(visitor)
        }
    }
}
