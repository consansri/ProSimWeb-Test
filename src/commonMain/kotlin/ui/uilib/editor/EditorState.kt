package ui.uilib.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import cengine.editor.CodeEditor
import cengine.editor.annotation.Annotation
import cengine.editor.indentation.BasicIndenation
import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.RopeModel
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.vfs.VirtualFile

class EditorState(
    override val file: VirtualFile,
    override val psiManager: PsiManager<*>? = null,
    val hasChanged: (EditorState) -> Unit
) : CodeEditor {

    // Code States

    override val textModel: TextModel = RopeModel(file.getAsUTF8String())
    override val selector: Selector = Selector(textModel)
    override val textStateModel: TextStateModel = TextStateModel(this, textModel, selector)
    override val indentationProvider: IndentationProvider = BasicIndenation(textStateModel, textModel)

    override var currentElement: PsiElement? by mutableStateOf<PsiElement?>(null)
    override var annotations: Set<Annotation> by mutableStateOf<Set<Annotation>>(emptySet())

    // Rendering States
    var viewBounds by mutableStateOf(Rect.Zero)

    override fun invalidateContent(editor: CodeEditor) {
        hasChanged(this)
    }

    override fun invalidateAnalytics(editor: CodeEditor) {
        hasChanged(this)
    }

    fun elementAt(offset: Int): PsiElement? {
        val psiFile = psiFile ?: return null
        return lang?.psiService?.findElementAt(psiFile, offset)
    }
}