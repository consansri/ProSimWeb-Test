package me.c3.uilib.styled.editor

interface FileInterface {
    fun getRawContent() : String
    suspend fun contentChanged(text: String)
}