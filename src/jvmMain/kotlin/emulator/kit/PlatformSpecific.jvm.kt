package emulator.kit

import emulator.kit.assembler.AsmFile
import emulator.kit.optional.FileHandler
import java.io.File


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

fun File.toAsmFile(main: File,wsRoot: File): AsmFile {
    val mainRelativeName = this.toRelativeString(main.parentFile).replace('\\', '/')
    val wsRelativeName = this.toRelativeString(wsRoot).replace('\\', '/')
    return AsmFile(mainRelativeName, wsRelativeName, this.readText())
}

fun File.toAsmFile(): AsmFile = AsmFile(this.name, this.name, this.readText())

