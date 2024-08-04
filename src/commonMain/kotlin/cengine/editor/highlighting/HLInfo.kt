package cengine.editor.highlighting

import cengine.psi.core.TextRange

interface HLInfo {
    var range: TextRange
    val color: Int
}