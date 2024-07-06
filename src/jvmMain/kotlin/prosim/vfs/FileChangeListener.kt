package prosim.vfs

interface FileChangeListener {
    fun onFileChanged(file: VFile)
    fun onFileCreated(file: VFile)
    fun onFileDeleted(file: VFile)

}