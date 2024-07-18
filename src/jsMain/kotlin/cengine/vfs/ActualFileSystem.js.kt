package cengine.vfs

import Keys
import web.storage.localStorage

/**
 * JavaScript-specific implementation of [ActualFileSystem].
 *
 * This implementation uses [localStorage] to simulate a file system in a web browser environment.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ActualFileSystem actual constructor(actual val rootPath: String) {

    actual fun readFile(path: String): ByteArray {
        return localStorage.getItem(getFileKey(path))?.encodeToByteArray() ?: ByteArray(0)
    }

    actual fun writeFile(path: String, content: ByteArray) {
        localStorage.setItem(getFileKey(path), content.decodeToString())
    }

    actual fun deleteFile(path: String) {
        localStorage.removeItem(getFileKey(path))
    }

    actual fun listDirectory(path: String): List<String> {
        val prefix = getDirPrefix(path)
        return getLocalStorageKeys()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix).split("/").firstOrNull() ?: "" }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    actual fun isDirectory(path: String): Boolean {
        val prefix = getDirPrefix(path)
        return getLocalStorageKeys().any { it.startsWith(prefix) && it != getFileKey(path) }
    }

    actual fun exists(path: String): Boolean = localStorage.getItem(getFileKey(path)) != null

    actual fun getAbsolutePath(path: String): String = "$rootPath/$path".replace("//", "/")

    private fun getFileKey(path: String): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}"
    private fun getDirPrefix(path: String): String = "${Keys.FILE_PREFIX}${getAbsolutePath(path)}/"
    private fun getLocalStorageKeys(): List<String> {
        val keys = mutableListOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { keys.add(it) }
        }
        return keys
    }

    /**
     * Creates a file or directory.
     *
     * @param path The path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: String, isDirectory: Boolean) {
        if (isDirectory) {
            // don't save!
        } else {
            localStorage.setItem(getFileKey(path), "")
        }
    }
}