package cengine.editor.text

class StringModel(private var text: String) : cengine.editor.text.TextModel {
    override val length: Int get() = text.length
    override val lines: Int get() = text.count { it == '\n' } + 1

    override fun insert(index: Int, new: String) {
        text = text.substring(0, index) + new + text.substring(index)
    }

    override fun delete(start: Int, end: Int) {
        text = text.substring(0, start) + text.substring(end)
    }

    override fun replaceAll(new: String) {
        text = new
    }

    override fun substring(start: Int, end: Int): String {
        return text.substring(start, end)
    }

    override fun charAt(index: Int): Char {
        return text[index]
    }

    override fun getLineAndColumn(index: Int): Pair<Int, Int> {
        val relString = text.substring(0, index)
        if (relString.isEmpty()) {
            return 0 to 0
        }
        val lastLineCountIndex = relString.indexOfLast { it == '\n' }
        return relString.count { it == '\n' } to index - lastLineCountIndex - 1
    }

    override fun getIndexFromLineAndColumn(line: Int, column: Int): Int {
        var currLineIndex = 0
        var currLine = 0
        var currColumn = 0
        text.forEachIndexed { index, c ->
            if (line == currLine && column == currColumn) {
                return currLineIndex + currColumn
            }
            if (c == '\n') {
                if (line < currLine + 1) {
                    return currLineIndex + currColumn
                }
                currLineIndex = index
                currLine++
                currColumn = 0
            } else {
                currColumn++
            }
        }

        return currLineIndex + currColumn
    }

    override fun toString(): String = text
}