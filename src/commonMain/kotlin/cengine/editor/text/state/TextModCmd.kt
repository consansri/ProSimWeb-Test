package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.TextModel

interface TextModCmd {
    fun execute(model: TextModel, selector: Selector)
    fun undo(model: TextModel, selector: Selector)
}