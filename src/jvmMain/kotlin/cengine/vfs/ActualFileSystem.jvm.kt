package cengine.vfs

import java.nio.file.Files
import java.nio.file.Paths

/**
 * JVM-specific implementation of [ActualFileSystem].
 *
 * This implementation uses [java.nio.file] for file operations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ActualFileSystem actual constructor(actual val rootPath: String) {
    actual fun readFile(path: String): ByteArray {
        return try {
            Files.readAllBytes(Paths.get(getAbsolutePath(path)))
        } catch (e: FileAlreadyExistsException) {
            ByteArray(0)
        }
    }

    actual fun writeFile(path: String, content: ByteArray) {
        Files.write(Paths.get(getAbsolutePath(path)), content)
    }

    actual fun deleteFile(path: String) {
        try {
            Files.delete(Paths.get(getAbsolutePath(path)))
        } catch (e: Exception) {
            // if not existent, then it shouldn't need to be deleted.
        }
    }

    actual fun listDirectory(path: String): List<String> {
        return Files.list(Paths.get(getAbsolutePath(path))).use { stream ->
            stream.map { it.fileName.toString() }.toList()
        }
    }

    actual fun isDirectory(path: String): Boolean = Files.isDirectory(Paths.get(getAbsolutePath(path)))
    actual fun exists(path: String): Boolean = Files.exists(Paths.get(getAbsolutePath(path)))
    actual fun getAbsolutePath(path: String): String {
        val pathString = Paths.get(rootPath, path).normalize().toString()
        return pathString
    }

    /**
     * Creates a file or directory.
     *
     * @param path The path of the file or directory to create.
     * @param isDirectory If the file is a directory.
     */
    actual fun createFile(path: String, isDirectory: Boolean) {
        if (!Files.exists(Paths.get(getAbsolutePath(path)))) {
            if (isDirectory) {
                Files.createDirectory(Paths.get(getAbsolutePath(path)))
            } else {
                Files.createFile(Paths.get(getAbsolutePath(path)))
            }
        }
    }
}