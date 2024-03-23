package emulator.kit

import emulator.kit.common.FileHandler



/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
actual fun FileHandler.loadFiles(files: MutableList<FileHandler.File>) {

}

/**
 * Updates local Files to content of [FileHandler.files]
 */
actual fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int) {

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