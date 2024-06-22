package prosim.uilib.workspace

interface WSFileAction {

    val name: String

    fun shouldAppend(selectedFiles: List<Workspace.TreeFile>): Boolean

    suspend fun execute(ws: Workspace, files: List<Workspace.TreeFile>)

}