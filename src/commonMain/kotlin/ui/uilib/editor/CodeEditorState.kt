package ui.uilib.editor

import cengine.editor.CodeEditor
import cengine.editor.annotation.Notation
import cengine.editor.indentation.BasicIndenation
import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.vfs.VirtualFile

data class CodeEditorState(
    override val psiManager: PsiManager<*>? = null,
    override val file: VirtualFile,
): CodeEditor {
    override val textModel: TextModel = RopeModel(file.getAsUTF8String())
    override val selector: Selector = Selector(textModel)
    override val textStateModel: TextStateModel = TextStateModel(this, textModel, selector)
    override val indentationProvider: IndentationProvider = BasicIndenation(textStateModel, textModel)
    override var currentElement: PsiElement? = null
    override var notations: Set<Notation> = emptySet()
    override fun invalidateContent() {
        TODO("Not yet implemented")
    }

    override fun invalidateAnalytics() {
        TODO("Not yet implemented")
    }

}