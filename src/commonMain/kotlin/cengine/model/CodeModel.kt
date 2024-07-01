package cengine.model

import cengine.annotation.Annotater
import cengine.completion.Completer
import cengine.highlighting.Highlighter
import cengine.structures.Rope

/**
 * This model is representing the state of the rendered code.
 */
class CodeModel {

    var state: Rope = Rope()

    var annotater: Annotater? = null
    var completer: Completer? = null
    var highlighter: Highlighter? = null

}