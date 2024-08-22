package cengine.util.string


fun String.lineAndColumn(index: Int): Pair<Int, Int>{
    val relString = substring(0, index)
    if (relString.isEmpty()) {
        return 0 to 0
    }
    val lastLineCountIndex = relString.indexOfLast { it == '\n' }
    return relString.count { it == '\n' } to index - lastLineCountIndex - 1
}
