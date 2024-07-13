package prosim.uilib.styled.editor3

import cengine.editor.widgets.Widget

data class LineInfo(
    val lineNumber: Int,
    val startIndex: Int,
    val endIndex: Int,
    val text: String,
    val interlineWidgets: List<Widget>,
    val inlayWidgets: List<Widget>,
    val foldingPlaceholder: String?
)
