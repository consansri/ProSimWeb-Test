package cengine.vfs

import Keys
import web.storage.localStorage

/**
 * JavaScript-specific implementation of [ActualFileSystem].
 *
 * This implementation uses [localStorage] to simulate a file system in a web browser environment.
 */
actual class ActualFileSystem actual constructor(rootPath: String) {
    actual val rootPath = rootPath

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
}