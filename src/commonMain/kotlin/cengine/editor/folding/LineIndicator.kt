package cengine.editor.folding

data class LineIndicator(val lineNumber: Int, val isFoldedBeginning: Boolean = false, val placeHolder: String = "")