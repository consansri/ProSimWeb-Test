package cengine.editor

import cengine.editor.indentation.IndentationProvider
import cengine.editor.selection.Selector
import cengine.editor.text.Editable
import cengine.editor.text.TextModel
import cengine.editor.text.state.TextStateModel
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        psiManager?.queueUpdate(file, textModel) {
            withContext(Dispatchers.Main){
                invalidateContent()
            }
        }
    }

    fun loadFromFile() {
        textModel.replaceAll(file.getAsUTF8String())
        psiManager?.queueUpdate(file, null) {
            withContext(Dispatchers.Main) {
                invalidateContent()
            }
        }
    }

    override fun insert(index: Int, new: String) {
        textModel.insert(index, new)
        psiManager?.inserted(file, textModel, index, new) {
            withContext(Dispatchers.Main) {
                invalidateContent()
            }
        }
    }

    override fun delete(start: Int, end: Int) {
        textModel.delete(start, end)
        psiManager?.deleted(file, textModel, start, end) {
            withContext(Dispatchers.Main) {
                invalidateContent()
            }
        }
    }

    override fun replaceAll(new: String) {
        textModel.replaceAll(new)
        psiManager?.queueUpdate(file, textModel) {
            withContext(Dispatchers.Main) {
                invalidateContent()
            }
        }
    }



    fun invalidateContent()
}