package cengine.lang.asm.psi

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog

class AsmFile(override val file: VirtualFile, override val lang: AsmLang, private var program: GASNode.Program) : PsiFile {

    override val parent: PsiElement? = null
    override val children: MutableList<GASNode> get() = program.children

    override var textRange: TextRange = program.textRange

    init {
        nativeLog(print(""))
    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
        ArrayList(children).forEach {
            it.accept(visitor)
        }
    }

    override fun update() {
        // Reparse the file and update children
        val newFile = lang.psiParser.parseFile(file)
        program = newFile.program
        textRange = newFile.textRange
        nativeLog("Updated: " + print(""))
    }

}