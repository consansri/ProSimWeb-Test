package me.c3.uilib.styled.editor

interface Highlighter {
    suspend fun highlight(text: String): List<CEditorArea.StyledChar>
}