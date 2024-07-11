package cengine.editor

import cengine.editor.selection.Selector
import cengine.editor.text.TextModel
import cengine.lang.Language
import cengine.vfs.VirtualFile

/**
 * This model is representing the state of the rendered code.
 */
interface CodeEditor {

    val file: VirtualFile
    val textModel: TextModel
    val selector: Selector
    val language: Language?
        get() = file.getLanguage()


}