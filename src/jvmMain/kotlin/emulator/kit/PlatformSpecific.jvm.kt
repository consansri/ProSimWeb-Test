package emulator.kit

import emulator.kit.assembly.Compiler
import emulator.kit.optional.FileHandler
import kotlinx.coroutines.Delay
import java.io.File
import javax.swing.SwingUtilities
import javax.swing.Timer


var localFiles = listOf(
    FileHandler.File("testfile.s", "new file"),
    FileHandler.File("second.s", "anotherone")
)

/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
actual fun FileHandler.loadFiles(files: MutableList<FileHandler.File>) {
    files.clear()
    files.addAll(localFiles)
}

/**
 * Updates local Files to content of [FileHandler.files]
 */
actual fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int) {
    localFiles = files
}

actual fun nativeLog(message: String) {
    println("Log: $message")
}

actual fun nativeError(message: String) {
    println("Error: $message")
}

actual fun nativeWarn(message: String) {
    println("Warn: $message")
}

actual fun nativeInfo(message: String) {
    println("Info: $message")
}

fun File.toCompilerFile(): Compiler.CompilerFile = Compiler.CompilerFile(this.name, this.readText())

