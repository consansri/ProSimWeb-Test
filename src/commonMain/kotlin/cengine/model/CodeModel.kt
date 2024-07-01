package cengine.model

import cengine.annotation.Annotater
import cengine.completion.Completer
import cengine.highlighting.Highlighter
import cengine.structures.RopeModel

/**
 * This model is representing the state of the rendered code.
 */
class CodeModel {

    var state: RopeModel = RopeModel()

    var annotater: Annotater? = null
    var completer: Completer? = null
    var highlighter: Highlighter? = null

}