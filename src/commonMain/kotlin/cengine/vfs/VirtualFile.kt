package cengine.vfs

/**
 * Represents a file or directory in the virtual file system.
 */
interface VirtualFile {
    /**
     * The name of the file or directory.
     */
    val name: String

    /**
     * The full path of the file or directory within the file system.
     */
    val path: String

    /**
     * Indicates whether this is a directory
     */
    val isDirectory: Boolean

    /**
     * The parent directory of this file or directory, or null if this is the root.
     */
    val parent: VirtualFile?

    /**
     * Retrieves the child files and directories, or null if this is the root.
     *
     * @return A list of child [VirtualFile] objects, or an empty list if this is not a directory.
     */
    fun getChildren(): List<VirtualFile>

    /**
     * Retrieves the content of the file.
     *
     * @return The file content as a ByteArray, or an empty ByteArray if this is a directory.
     */
    fun getContent(): ByteArray

    /**
     * Sets the content of the file.
     *
     * @param content The new content of the file as a ByteArray
     * @throws UnsupportedOperationException if this is a directory.
     */
    fun setContent(content: ByteArray)
}