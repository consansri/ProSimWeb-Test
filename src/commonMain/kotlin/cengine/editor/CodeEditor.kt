package cengine.editor

import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.psi.PsiManager
import cengine.vfs.VirtualFile

/**
 * This model is representing the state of the rendered code.
 */
interface CodeEditor {
    val psiManager: PsiManager<*>?
    val file: VirtualFile
    val textModel: TextModel
    val textStateModel: TextStateModel
    val selector: Selector
    val indentationProvider: IndentationProvider

    fun saveToFile() {
        file.setAsUTF8String(textModel.toString())
        psiManager?.updatePsi(file, textModel)
    }

    fun loadFromFile() {
        psiManager?.updatePsi(file, textModel)
        textModel.replaceAll(file.getAsUTF8String())
    }

    fun invalidateContent()

}