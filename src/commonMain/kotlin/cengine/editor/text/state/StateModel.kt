package cengine.editor.text.state

import cengine.editor.text.Editable
import cengine.editor.text.TextModel

interface StateModel: Editable {
    val textModel: TextModel
    val canUndo: Boolean
    val canRedo: Boolean

    fun undo()
    fun redo()
}