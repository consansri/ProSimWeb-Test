package cengine.editor

import cengine.editor.annotation.Annotater
import cengine.editor.completion.Completer
import cengine.editor.editing.EditHandler
import cengine.editor.folding.CodeFolder
import cengine.editor.highlighting.Highlighter
import cengine.editor.selection.Selector
import cengine.editor.text.TextModel
import cengine.editor.widgets.WidgetManager

/**
 * This model is representing the state of the rendered code.
 */
interface CodeEditor {

    val textModel: TextModel
    val selector: Selector

    val codeFolder: CodeFolder
    val widgetManager: WidgetManager

    var completer: Completer?
    var editHandler: EditHandler?
    var highlighter: Highlighter?
    var annotater: Annotater?

}