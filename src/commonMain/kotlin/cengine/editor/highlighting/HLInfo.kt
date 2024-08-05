package cengine.editor.highlighting

import cengine.psi.core.TextRange

interface HLInfo {
    val range: TextRange
    val color: Int
}