package prosim.vfs

class VFileSystem {

    private val root: VFile = RootDirectory()
    private val listeners = mutableListOf<FileChangeListener>()
    private val fileChache = mutableMapOf<String, VFile>()

    fun findFile(path: String): VFile?{
        return fileChache[path] ?: findFileInternal(path)?.also { fileChache[path] = it }
    }
    private fun findFileInternal(path: String): VFile? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        var current: VFile = root
        for (part in parts) {
            current = current.getChildren().find { it.name == part } ?: return null
        }
        return current
    }

    fun createFile(path: String, isDirectory: Boolean = false): VFile {
        val parts = path.split("/").filter { it.isNotEmpty() }
        var current: VFile = root
        for (i in 0..<parts.size - 1) {
            val part = parts[i]
            current = current.getChildren().find { it.name == part }
                ?: createFile("/${parts.slice(0..i).joinToString("/")}", true)
        }
        val newFile = VFileImpl(parts.last(), path, isDirectory, current)
        (current as? VFileImpl)?.children?.add(newFile)
        notifyFileCreated(newFile)
        return newFile
    }

    fun deleteFile(path: String) {
        val file = findFile(path) ?: return
        (file.parent as? VFileImpl)?.children?.remove(file)
        notifyFileDeleted(file)
    }

    fun addListener(listener: FileChangeListener){
        listeners.add(listener)
    }
    fun removeListener(listener: FileChangeListener){
        listeners.remove(listener)
    }
    fun notifyFileChanged(file: VFile){
        listeners.forEach {
            it.onFileChanged(file)
        }
    }
    private fun notifyFileCreated(file: VFile){
        listeners.forEach {
            it.onFileCreated(file)
        }
    }
    private fun notifyFileDeleted(file: VFile){
        listeners.forEach {
            it.onFileDeleted(file)
        }
    }
}