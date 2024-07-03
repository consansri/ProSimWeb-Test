package cengine

import cengine.annotation.Annotater
import cengine.completion.Completer
import cengine.editing.Editor
import cengine.highlighting.Highlighter
import cengine.selection.Selector
import cengine.text.TextModel

/**
 * This model is representing the state of the rendered code.
 */
interface CodeModel {

    val textModel: TextModel
    val selector: Selector

    var editor: Editor?
    var highlighter: Highlighter?
    var annotater: Annotater?
    var completer: Completer?

}