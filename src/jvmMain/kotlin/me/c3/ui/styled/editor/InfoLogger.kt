package me.c3.ui.styled.editor

interface InfoLogger {

    fun printCaretInfo(caret: CodePosition, lowSelPosition: CodePosition, highSelPosition: CodePosition)

    data class CodePosition(
        val index: Int,
        val line: Int,
        val column: Int
    )

}