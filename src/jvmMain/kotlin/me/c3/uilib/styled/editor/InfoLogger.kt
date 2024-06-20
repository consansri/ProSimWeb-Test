package me.c3.uilib.styled.editor

interface InfoLogger {

    fun printCaretInfo(caret: CodePosition, lowSelPosition: CodePosition, highSelPosition: CodePosition)

    fun printError(text: String)
    fun clearError()

    data class CodePosition(
        val index: Int,
        val line: Int,
        val column: Int
    )

}