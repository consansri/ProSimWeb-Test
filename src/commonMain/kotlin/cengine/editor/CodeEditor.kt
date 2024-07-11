package cengine.editor

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

    fun saveToFile() {
        file.setAsUTF8String(textModel.toString())
    }

    fun loadFromFile() {
        textModel.replaceAll(file.getAsUTF8String())
    }
}