package cengine.editor

import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.vfs.VirtualFile

/**
 * This model is representing the state of the rendered code.
 */
interface CodeEditor : Editable {
    val psiManager: PsiManager<*>?
    val file: VirtualFile
    val textModel: TextModel
    val textStateModel: TextStateModel
    val selector: Selector
    val indentationProvider: IndentationProvider
    var currentElement: PsiElement?

    fun saveToFile() {
        file.setAsUTF8String(textModel.toString())
        psiManager?.updatePsi(file, textModel) {
            invalidateContent()
        }
    }

    fun loadFromFile() {
        textModel.replaceAll(file.getAsUTF8String())
        psiManager?.updatePsi(file, null) {
            invalidateContent()
        }
    }

    override fun insert(index: Int, new: String) {
        textModel.insert(index, new)
        psiManager?.updatePsi(file, textModel) {
            invalidateContent()
        }
    }

    override fun replaceAll(new: String) {
        textModel.replaceAll(new)
        psiManager?.updatePsi(file, textModel) {
            invalidateContent()
        }
    }

    override fun delete(start: Int, end: Int) {
        textModel.delete(start, end)
        psiManager?.updatePsi(file, textModel) {
            invalidateContent()
        }
    }

    fun invalidateContent()
}