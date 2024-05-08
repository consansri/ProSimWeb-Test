package me.c3.ui.styled.editor

interface FileInterface {
    fun getRawContent() : String
    suspend fun contentChanged(text: String)
}