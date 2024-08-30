package cengine.vfs

import Keys
import emulator.kit.nativeLog
import web.storage.localStorage

/**
 * JavaScript-specific implementation of [ActualFileSystem].
 *
 * This implementation uses [localStorage] to simulate a file system in a web browser environment.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ActualFileSystem actual constructor(actual val rootPath: String) {

    /**
     * Reads the content of a file.
     *
     * @param path The relative path of the file to read.
     * @return The content of the file as ByteArray.
     */
    actual fun readFile(path: FPath): ByteArray {
        val content = kotlinx.browser.localStorage.getItem(getFileKey(path))?.encodeToByteArray() ?: ByteArray(0)
        nativeLog("ReadFile: $path ${content.size}")
        return content
    }

    /**
     * Writes content to a file.
     *
     * @param path The relative path of the file to write.
     * @param content The content to write to the file.
     */
    actual fun writeFile(path: FPath, content: ByteArray) {
        nativeLog("WriteFile: $path ${content.size}")
        kotlinx.browser.localStorage.setItem(getFileKey(path), content.decodeToString())
    }

    /**
     * Deletes a file or directory.
     *
     * @param path The relative path of the file or directory to delete.
     */
    actual fun deleteFile(path: FPath) {
        nativeLog("DeleteFile: $path")
        kotlinx.browser.localStorage.removeItem(getFileKey(path))
    }

    /**
     * Creates a file or directory.
     *
     * @param path The relative path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: FPath, isDirectory: Boolean) {
        nativeLog("CreateFile: $path isDirectory=$isDirectory")
        if (isDirectory) {
            // don't save!
        } else {
            kotlinx.browser.localStorage.setItem(getFileKey(path), "")
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param path The relative path of the directory to list.
     * @return A list of names of files and directories in the given directory.
     */
    actual fun listDirectory(path: FPath): List<String> {
        val prefix = getDirPrefix(path)

        val paths = getLocalStorageKeys()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix).split(FPath.DELIMITER).firstOrNull() ?: "" }
            .filter { it.isNotEmpty() }
            .distinct()

        nativeLog("ListDirectories: path=$path prefix=$prefix ->\n ${getLocalStorageKeys().filter { it.startsWith(prefix) }}")
        return paths
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The relative path to check.
     * @return True if the path is a directory, false otherwise.
     */
    actual fun isDirectory(path: FPath): Boolean {
        val prefix = getDirPrefix(path)
        return getLocalStorageKeys().any { it.startsWith(prefix) && it != getFileKey(path) }
    }

    /**
     * Checks if a file or directory exists.
     *
     * @param path The relative path to check.
     * @return True if the file or directory exists, false otherwise.
     */
    actual fun exists(path: FPath): Boolean {
        if (kotlinx.browser.localStorage.getItem(getFileKey(path)) != null) {
            // Exists as File
            return true
        }

        if (getLocalStorageKeys().any { it.startsWith(getDirPrefix(path)) }) {
            // Exists as Directory
            return true
        }

        return false
    }

    /**
     * Converts a relative path to an absolute path using [rootPath].
     *
     * @param path The relative path to convert.
     * @return The absolute path.
     */
    actual fun getAbsolutePath(path: FPath): String = path.toAbsolute(rootPath)

    private fun getFileKey(path: FPath): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}"
    private fun getDirPrefix(path: FPath): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}${FPath.DELIMITER}"
    private fun getLocalStorageKeys(): List<String> {
        val keys = mutableListOf<String>()
        for (i in 0 until kotlinx.browser.localStorage.length) {
            kotlinx.browser.localStorage.key(i)?.let { keys.add(it) }
        }
        nativeLog("Keys: ${keys}")
        return keys
    }



}