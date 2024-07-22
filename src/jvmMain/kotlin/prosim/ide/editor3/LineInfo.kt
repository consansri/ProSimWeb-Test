package prosim.ide.editor3

import cengine.editor.folding.LineIndicator
import cengine.editor.widgets.Widget

data class LineInfo(
    val lineIndicator: LineIndicator,
    val startIndex: Int,
    val endIndex: Int,
    val firstNonWhitespaceCol: Int,
    val containsOnlySpaces: Boolean,
    val interlineWidgets: List<Widget>,
    val inlayWidgets: List<Widget>,
    val foldingPlaceholder: String?
){
    val lineNumber: Int get() = lineIndicator.lineNumber
}
