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
    actual fun readFile(path: String): ByteArray = Files.readAllBytes(Paths.get(getAbsolutePath(path)))

    actual fun writeFile(path: String, content: ByteArray) {
        Files.write(Paths.get(getAbsolutePath(path)), content)
    }

    actual fun deleteFile(path: String) {
        Files.delete(Paths.get(getAbsolutePath(path)))
    }

    actual fun listDirectory(path: String): List<String> {
        return Files.list(Paths.get(getAbsolutePath(path))).use {stream ->
            stream.map { it.fileName.toString() }.toList()
        }
    }

    actual fun isDirectory(path: String): Boolean = Files.isDirectory(Paths.get(getAbsolutePath(path)))
    actual fun exists(path: String): Boolean = Files.exists(Paths.get(getAbsolutePath(path)))
    actual fun getAbsolutePath(path: String): String = Paths.get(rootPath, path).normalize().toString()

}