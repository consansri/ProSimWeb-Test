package cengine.lang.asm.psi

import cengine.editor.annotation.Notation
import cengine.editor.text.TextModel
import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.gas.GASNode
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.TextRange
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AsmFile(override val file: VirtualFile, override val lang: AsmLang, private var program: GASNode.Program) : PsiFile {
    override val quickeditScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    override var textModel: TextModel? = null
    override val notations: List<Notation>
        get() = program.notations
    override val additionalInfo: String
        get() = program.additionalInfo

    override val parent: PsiElement?
        get() = program.parent
    override val children: List<GASNode> get() = program.children

    override var textRange: TextRange = program.textRange

    fun getFormattedString(identSize: Int): String = program.getFormatted(identSize)

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
        val newFile = lang.psiParser.parseFile(file, textModel)
        program = newFile.program
        textRange = newFile.textRange
    }

}