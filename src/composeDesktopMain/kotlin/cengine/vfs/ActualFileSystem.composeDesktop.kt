package cengine.vfs

/**
 * Platform-specific implementation of the actual file system operations.
 *
 * @property rootPath The root path of this file system.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ActualFileSystem actual constructor(rootPath: String) {
    actual val rootPath: String
        get() = TODO("Not yet implemented")

    /**
     * Reads the content of a file.
     *
     * @param path The relative path of the file to read.
     * @return The content of the file as ByteArray.
     */
    actual fun readFile(path: String): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    actual fun writeFile(path: String, content: ByteArray) {
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: String) {
    }

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: String, isDirectory: Boolean) {
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: String): List<String> {
        TODO("Not yet implemented")
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: String): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: String): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Converts a relative path to an absolute path using [rootPath].
     *
     * @param path The relative path to convert.
     * @return The absolute path.
     */
    actual fun getAbsolutePath(path: String): String {
        TODO("Not yet implemented")
    }

}