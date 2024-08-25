package emulator.kit

import emulator.kit.optional.FileHandler

/**
 * Updates local Files to content of [FileHandler.files]
 */
actual fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int) {
}

/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
actual fun FileHandler.loadFiles(files: MutableList<FileHandler.File>) {
}

actual fun nativeWarn(message: String) {
}

actual fun nativeLog(message: String) {
}

actual fun nativeError(message: String) {
}

actual fun nativeInfo(message: String) {
}