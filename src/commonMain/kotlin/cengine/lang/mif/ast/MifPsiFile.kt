package cengine.lang.mif.ast

import cengine.editor.annotation.Annotation
import cengine.lang.mif.MifLang
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class MifPsiFile(
    override val file: VirtualFile, var program: MifNode.Program
) : PsiFile {

    override val lang: MifLang get() = MifLang
    override val children: List<PsiElement>
        get() = program.children

    override val annotations: List<Annotation>
        get() = program.annotations

    override var parent: PsiElement? = null
    override val additionalInfo: String = "MifFile"
    override var range: IntRange = (children.minOf { it.range.first })..(children.maxOf { it.range.last })

    override fun update() {
        // Reparse the file and update children
        val newFile = lang.psiParser.parse(file)
        program = newFile.program
        range = newFile.range
    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }
}