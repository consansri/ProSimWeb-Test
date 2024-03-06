package emulator.kit

import emulator.kit.common.FileHandler


/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
expect fun FileHandler.loadFiles(files: MutableList<FileHandler.File>)

/**
 * Updates local Files to content of [FileHandler.files]
 */
expect fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int)


expect fun nativeWarn(message: String)

expect fun nativeLog(message: String)

expect fun nativeError(message: String)

expect fun nativeInfo(message: String)