package cengine.project

import cengine.vfs.FPath
import cengine.vfs.VFileSystem
import cengine.vfs.VirtualFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProjectStateManager {

    private const val projectsFileName = "projects.json"

    private val vfs = VFileSystem("Projects")

    var projects: List<ProjectState> = loadProjectState()
        set(value) {
            field = value
            saveProjectState(value)
        }

    // Function to save the project state to a file
    fun saveProjectState(list: List<ProjectState>): VirtualFile {
        // nativeLog("SaveProjectState")
        val file = vfs.findFile(FPath.of(vfs, projectsFileName)) ?: vfs.createFile(FPath.of(vfs, projectsFileName))
        // Serialize the ProjectState object to a JSON string
        val jsonString = Json.encodeToString(list)
        // Write the JSON string to the file
        file.setAsUTF8String(jsonString)
        return file
    }

    // Function to load the project state from a file
    fun loadProjectState(): List<ProjectState> {
        // nativeLog("LoadProjectState")
        val file = vfs.findFile(FPath.of(vfs, projectsFileName))

        if (file == null) {
            val createdFile = saveProjectState(listOf())

            // Read the JSON string from the file
            val jsonString = createdFile.getAsUTF8String()
            // Deserialize the JSON string to a ProjectState object
            return Json.decodeFromString<List<ProjectState>>(jsonString)
        }

        // Read the JSON string from the file
        val jsonString = file.getAsUTF8String()
        // Deserialize the JSON string to a ProjectState object
        return Json.decodeFromString<List<ProjectState>>(jsonString)
    }

}