package cengine.vfs

/**
 * Platform-specific implementation of the actual file system operations.
 *
 * @property rootPath The root path of this file system.
 */
expect class ActualFileSystem(rootPath: String) {
    val rootPath: String

    /**
     * Reads the content of a file.
     *
     * @param path The path of the file to read.
     * @return The content of the file as ByteArray.
     */
    fun readFile(path: String): ByteArray

    /**
     * Writes content to a file.
     *
     * @param path The path of the file to write.
     * @param content The content to write to the file.
     */
    fun writeFile(path: String, content: ByteArray)

    /**
     * Deletes a file or directory.
     *
     * @param path The path of the file or directory to delete.
     */
    fun deleteFile(path: String)

    /**
     * Lists the contents of a directory.
     *
     * @param path The path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    fun listDirectory(path: String): List<String>

    /**
     * Checks if a path represents a directory.
     *
     * @param path The path to check.
     * @return True if the path is a directory, false otherwise.
     */
    fun isDirectory(path: String): Boolean

    /**
     * Checks if a file or directory exists.
     *
     * @param path The path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    fun exists(path: String): Boolean

    /**
     * Converts a relative path to an absolute path using [rootPath].
     *
     * @param path The relative path to convert.
     * @return The absolute path.
     */
    fun getAbsolutePath(path: String): String
}