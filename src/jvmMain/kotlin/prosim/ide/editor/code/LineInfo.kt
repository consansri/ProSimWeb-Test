package prosim.ide.editor.code

import cengine.editor.folding.LineIndicator
import cengine.editor.widgets.Widget

data class LineInfo(
    val lineIndicator: LineIndicator,
    val startIndex: Int,
    val endIndex: Int,
    val firstNonWhitespaceCol: Int,
    val containsOnlySpaces: Boolean,
    val foldingPlaceholder: String?
){
    val lineNumber: Int get() = lineIndicator.lineNumber
}
