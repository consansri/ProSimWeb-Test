package cengine.lang.asm.ast.impl

import cengine.editor.annotation.Annotation
import cengine.lang.asm.AsmLang
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class AsmFile(override val file: VirtualFile, override val lang: AsmLang, private var program: ASNode.Program) : PsiFile {
    override val annotations: List<Annotation>
        get() = program.annotations
    override val additionalInfo: String
        get() = program.additionalInfo

    override val parent: PsiElement?
        get() = program.parent
    override val children: List<ASNode> get() = program.children

    override var range: IntRange = program.range
        set(value) {
            field = program.range
        }
        get() = program.range

    fun getFormattedString(identSize: Int): String = program.getFormatted(identSize)

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
        ArrayList(children).forEach {
            it.accept(visitor)
        }
    }

    override fun update() {
        // Reparse the file and update children
        val newFile = lang.psiParser.parse(file)
        program = newFile.program
        range = newFile.range
    }
}