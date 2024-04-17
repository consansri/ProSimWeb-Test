package me.c3.ui.styled.editor

interface Highlighter {
    suspend fun highlight(text: String): List<CEditorArea.StyledChar>
}