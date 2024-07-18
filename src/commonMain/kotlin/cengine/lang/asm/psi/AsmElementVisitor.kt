package cengine.lang.asm.psi

import cengine.lang.asm.psi.instr.AsmInstr
import cengine.lang.asm.psi.stmnt.AsmLabel
import cengine.psi.core.PsiElementVisitor

/**
 * Assembly specific [PsiElementVisitor]
 */
interface AsmElementVisitor: PsiElementVisitor {
    fun visitFile(file: AsmFile)
    fun visitProgram(program: AsmProgram)
    fun visitInstruction(instr: AsmInstr)
    fun visitLabel(label: AsmLabel)
}