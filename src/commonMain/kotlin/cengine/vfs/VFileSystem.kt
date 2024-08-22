package cengine.vfs

import emulator.kit.nativeLog

/**
 * Virtual File System (VFS)
 *
 * A flexible, platform-independent file system abstraction layer.
 * It provides a unified interface for file operations across different platforms,
 * allowing to work with files and directories in a consistent manner regardless of the
 * underlying storage mechanism.
 *
 * The main class that manages the virtual file system.
 *
 * @property actualFileSystem The platform-specific file system implementation.
 */
class VFileSystem(absRootPath: String) {
    private val absRootPath = absRootPath.replace("\\", DELIMITER)
    private val actualFileSystem: ActualFileSystem = ActualFileSystem(absRootPath)
    val root: VirtualFile = RootDirectory(absRootPath.split(DELIMITER).last())
    private val fileCache = mutableMapOf<String, VirtualFile>()
    private val changeListeners = mutableListOf<FileChangeListener>()
    private val fileWatcher: FileWatcher = FileWatcher(this)

    companion object {
        const val DELIMITER = "/"
    }

    init {
        initializeFileWatcher()
    }

    /**
     * FileWatcher Initialization
     */

    private fun initializeFileWatcher() {
        watchRecursively("")
        fileWatcher.startWatching()
    }

    private fun watchRecursively(relativePath: String) {
        if (!actualFileSystem.isDirectory(relativePath)) return
        fileWatcher.watchDirectory(actualFileSystem.getAbsolutePath(relativePath))

        actualFileSystem.listDirectory(relativePath).forEach { childName ->
            val childPath = "$relativePath$DELIMITER$childName"
            watchRecursively(childPath)
        }
    }

    /**
     * File System Modification
     */

    /**
     * Finds a file or directory in the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param absolutePath The absolute path of the file or directory to find.
     * @return The [VirtualFile] object if found, or null if not found.
     */
    fun findFileByAbsolute(absolutePath: String): VirtualFile? {
        val relativePath = absolutePath.removePrefix(absRootPath)
        //nativeLog("AbsRoot: $absRootPath, $relativePath -> {${fileCache.keys.joinToString(",")}}")
        return fileCache[relativePath] ?: findFileInternal(relativePath)?.also { fileCache[relativePath] = it }
    }

    /**
     * Finds a file or directory in the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path of the file or directory to find.
     * @return The [VirtualFile] object if found, or null if not found.
     */
    fun findFile(relativePath: String): VirtualFile? {
        return fileCache[relativePath] ?: findFileInternal(relativePath)?.also { fileCache[relativePath] = it }
    }

    /**
     * Creates a new file or directory in the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path where the new file or directory should be created.
     * @param isDirectory Whether to create a directory (true) or a file (false).
     * @return The newly created [VirtualFile] object.
     */
    fun createFile(relativePath: String, isDirectory: Boolean = false): VirtualFile {

        // Create Parent Directory if non-existent

        val lastDeliIndex = relativePath.indexOfLast { it == DELIMITER.first() }
        if (lastDeliIndex != -1) {
            val remainingPath = relativePath.substring(0, relativePath.indexOfLast { it == DELIMITER.first() })
            if (!actualFileSystem.exists(remainingPath)) {
                actualFileSystem.createFile(remainingPath, true)
            }
        }

        // Create File

        actualFileSystem.createFile(relativePath, isDirectory)
        val parts = relativePath.split(DELIMITER).filter { it.isNotEmpty() }
        var current: VirtualFile = root
        for (i in 0 until parts.size - 1) {
            val part = parts[i]
            current = current.getChildren().find { it.name == part } ?: createFile("/${parts.slice(0..i).joinToString(DELIMITER)}", true)
        }
        val newFile = getOrCreateFile(relativePath, isDirectory)
        fileCache[relativePath] = newFile
        notifyFileCreated(newFile)
        return newFile
    }

    /**
     * Deletes a file or directory from the virtual file system.
     *
     * Use [DELIMITER] inside the path!
     *
     * @param relativePath The relative path of the file or directory to delete.
     */
    fun deleteFile(relativePath: String) {
        val deletedFile = findFile(relativePath)
        actualFileSystem.deleteFile(relativePath)
        fileCache.remove(relativePath)
        deletedFile?.let {
            notifyFileDeleted(it)
        }
    }

    private fun findFileInternal(relativePath: String): VirtualFile? {
        val parts = relativePath.split(DELIMITER).filter { it.isNotEmpty() }
        var current: VirtualFile = root
        for (part in parts) {
            current = current.getChildren().find { it.name == part } ?: return null
        }
        return current
    }

    private fun getOrCreateFile(relativePath: String, isDirectory: Boolean = actualFileSystem.isDirectory(relativePath)): VirtualFile {
        return fileCache.getOrPut(relativePath) {
            val name = relativePath.split(DELIMITER).last()
            val parent = findFile(relativePath.substringBeforeLast(DELIMITER, DELIMITER))
            val newFile = VirtualFileImpl(name, relativePath, isDirectory, parent)
            newFile
        }
    }

    /**
     * Change Listeners
     */

    /**
     * Adds a listener for file system change events.
     *
     * @param listener The [FileChangeListener] to add.
     */
    fun addChangeListener(listener: FileChangeListener) {
        changeListeners.add(listener)
    }

    /**
     * Removes a previously added file system change listener.
     *
     * @param listener The [FileChangeListener] to remove.
     */
    fun removeChangeListener(listener: FileChangeListener) {
        changeListeners.remove(listener)
    }

    fun notifyFileChanged(file: VirtualFile) {
        file.hasChangedOnDisk()
        nativeLog("Notify File Changed")
        changeListeners.forEach { it.onFileChanged(file) }
    }

    fun notifyFileCreated(file: VirtualFile) {
        changeListeners.forEach { it.onFileCreated(file) }
    }

    fun notifyFileDeleted(file: VirtualFile) {
        changeListeners.forEach { it.onFileDeleted(file) }
    }

    fun close() {
        changeListeners.clear()
    }

    inner class RootDirectory(override val name: String) : VirtualFile {
        override val path: String = DELIMITER
        override val isDirectory: Boolean = true
        override val parent: VirtualFile? = null
        override var onDiskChange: () -> Unit = {}

        override fun getChildren(): List<VirtualFile> {
            return actualFileSystem.listDirectory(DELIMITER).map { getOrCreateFile("$DELIMITER$it") }
        }

        override fun getContent(): ByteArray = ByteArray(0)

        override fun setContent(content: ByteArray) {
            throw UnsupportedOperationException()
        }

        override fun toString(): String = name
    }

    inner class VirtualFileImpl(
        override val name: String,
        override val path: String,
        override val isDirectory: Boolean,
        override val parent: VirtualFile?
    ) : VirtualFile {
        override var onDiskChange: () -> Unit = {}

        override fun getChildren(): List<VirtualFile> {
            return if (isDirectory) {
                actualFileSystem.listDirectory(path).map { getOrCreateFile("$path$DELIMITER$it") }
            } else {
                emptyList()
            }
        }

        override fun getContent(): ByteArray {
            return if (isDirectory) {
                ByteArray(0)
            } else {
                actualFileSystem.readFile(path)
            }
        }

        override fun setContent(content: ByteArray) {
            if (!isDirectory) {
                actualFileSystem.writeFile(path, content)
            }
        }

        override fun toString(): String = name
    }

    override fun toString(): String {
        return root.toString()
    }


}