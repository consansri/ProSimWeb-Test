package cengine.editor.completion

interface Completer {
    fun getCompletions(prefix: String, line: Int, column: Int): List<String>
}
