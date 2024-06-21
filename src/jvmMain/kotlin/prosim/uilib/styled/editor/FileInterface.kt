package prosim.uilib.styled.editor

interface FileInterface {
    fun getRawContent() : String
    suspend fun contentChanged(text: String)
}