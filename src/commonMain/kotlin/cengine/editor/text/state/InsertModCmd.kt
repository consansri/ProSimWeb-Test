package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.TextModel

class InsertModCmd(private val index: Int, private val text: String) : TextModCmd {
    private lateinit var oldSelection: Pair<Int?,Int?>
    private var oldCaretIndex: Int = 0
    override fun execute(model: TextModel, selector: Selector) {
        model.insert(index, text)
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
    }

    override fun undo(model: TextModel, selector: Selector) {
        model.delete(index, index + text.length)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }
}