package emulator.kit

import debug.DebugTools
import kotlinx.browser.localStorage
import Keys
import emulator.kit.optional.FileHandler


/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
actual fun FileHandler.loadFiles(files: MutableList<FileHandler.File>) {
    files.clear()
    val fileCount = localStorage.getItem(Keys.FILE_COUNT)?.toIntOrNull() ?: 0
    if (fileCount > 0) {
        for (fileID in 0..<fileCount) {
            val filename = localStorage.getItem("$fileID" + Keys.FILE_NAME)
            val filecontent = localStorage.getItem("$fileID" + Keys.FILE_CONTENT)
            val fileUndoLength = localStorage.getItem("$fileID" + Keys.FILE_UNDO_LENGTH)?.toIntOrNull() ?: 1
            val fileRedoLength = localStorage.getItem("$fileID" + Keys.FILE_REDO_LENGTH)?.toIntOrNull() ?: 0
            val fileUndoStates = mutableListOf<String>()
            val fileRedoStates = mutableListOf<String>()

            for (undoID in 0..<fileUndoLength) {
                fileUndoStates.add(localStorage.getItem("${fileID}${Keys.FILE_UNDO}-$undoID") ?: "")
            }
            for (redoID in 0..<fileRedoLength) {
                fileRedoStates.add(localStorage.getItem("${fileID}${Keys.FILE_UNDO}-$redoID") ?: "")
            }

            if (filename != null && filecontent != null) {
                if (DebugTools.KIT_showFileHandlerInfo) {
                    println("found file: $filename $filecontent ${fileUndoStates.size} ${fileRedoStates.size}")
                }
                files.add(FileHandler.File(filename, filecontent, undoStates = fileUndoStates, redoStates = fileRedoStates))
            }
        }
    } else {
        files.add(FileHandler.File("main.s", "", undoStates = mutableListOf(), redoStates = mutableListOf()))
    }
    setCurrent(localStorage.getItem(Keys.FILE_CURR)?.toIntOrNull() ?: 0)
    if (DebugTools.KIT_showFileHandlerInfo) {
        println("FileHandler.init(): ${files.joinToString { "\n\t" + it.getName() }}")
    }
}


/**
 * Updates local Files to content of [FileHandler.files]
 */
actual fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int) {
    localStorage.setItem(Keys.FILE_CURR, currentID.toString())
    if (!onlyCurrent) {
        localStorage.setItem(Keys.FILE_COUNT, files.size.toString())
        for (fileID in files.indices) {
            val file = files[fileID]
            localStorage.setItem("$fileID" + Keys.FILE_NAME, file.getName())
            localStorage.setItem("$fileID" + Keys.FILE_CONTENT, file.getContent())
            localStorage.setItem("$fileID" + Keys.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
            localStorage.setItem("$fileID" + Keys.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
            file.getUndoStates().forEach {
                val undoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${Keys.FILE_UNDO}-$undoID", it)
            }
            file.getRedoStates().forEach {
                val redoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${Keys.FILE_UNDO}-$redoID", it)
            }
        }
    } else {
        val fileID = currentID
        val file = files[fileID]
        localStorage.setItem("$fileID" + Keys.FILE_NAME, file.getName())
        localStorage.setItem("$fileID" + Keys.FILE_CONTENT, file.getContent())
        localStorage.setItem("$fileID" + Keys.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
        localStorage.setItem("$fileID" + Keys.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
        file.getUndoStates().forEach {
            val undoID = file.getUndoStates().indexOf(it)
            localStorage.setItem("${fileID}${Keys.FILE_UNDO}-$undoID", it)
        }
        file.getRedoStates().forEach {
            val redoID = file.getUndoStates().indexOf(it)
            localStorage.setItem("${fileID}${Keys.FILE_REDO}-$redoID", it)
        }
    }
}

actual fun nativeWarn(message: String) {
    println("Warning: $message")
}

actual fun nativeLog(message: String) {
    println("Log: $message")
}

actual fun nativeError(message: String) {
    println("Error: $message")
}

actual fun nativeInfo(message: String) {
    println("Info: $message")
}