package cengine.editor.text.state

import cengine.editor.selection.Selector
import cengine.editor.text.TextModel

class DeleteModCmd(private val start: Int, private val end: Int) : TextModCmd {
    private lateinit var deletedText: String
    private lateinit var oldSelection: Pair<Int?,Int?>
    private var oldCaretIndex: Int = 0
    override fun execute(model: TextModel, selector: Selector) {
        deletedText = model.substring(start, end)
        oldSelection = selector.selection.start to selector.selection.end
        oldCaretIndex = selector.caret.index
        model.delete(start, end)
    }

    override fun undo(model: TextModel, selector: Selector) {
        model.insert(start, deletedText)
        selector.caret.set(oldCaretIndex)
        selector.selection.select(oldSelection)
    }
}