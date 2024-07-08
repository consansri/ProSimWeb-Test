package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.TextModel

class ReplaceAllModCmd(private val newText: String): TextModCmd {
    private lateinit var oldText: String
    private lateinit var oldSelection: Pair<Int?,Int?>
    private var oldCaretIndex: Int = 0

    override fun execute(model: TextModel, selector: Selector) {
        oldText = model.toString()
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
        model.replaceAll(newText)
    }

    override fun undo(model: TextModel, selector: Selector) {
        model.replaceAll(oldText)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }


}