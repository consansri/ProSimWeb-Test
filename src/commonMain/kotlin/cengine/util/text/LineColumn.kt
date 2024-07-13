package cengine.util.text

data class LineColumn(
    val line: Int,
    val column: Int
) {

    companion object {
        fun of(line: Int, column: Int): LineColumn = LineColumn(line, column)
    }

    override fun toString(): String = "LineColumn{line=$line,column=$column}"
}