package cengine.lang.asm.psi

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.editor.widgets.Widget
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.vfs.VirtualFile

class AsmFile(override val file: VirtualFile, override val lang: AsmLang, private var program: GASNode.Program) : PsiFile {
    override var textModel: TextModel? = null
    override val notations: List<Notation>
        get() = program.notations
    override val additionalInfo: String
        get() = program.additionalInfo

    override val interlineWidgets: List<Widget>
        get() = program.interlineWidgets

    override val inlayWidgets: List<Widget>
        get() = program.inlayWidgets

    override val parent: PsiElement?
        get() = program.parent
    override val children: List<GASNode> get() = program.children

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
        val newFile = lang.psiParser.parseFile(file, textModel)
        program = newFile.program
        range = newFile.range
    }
}