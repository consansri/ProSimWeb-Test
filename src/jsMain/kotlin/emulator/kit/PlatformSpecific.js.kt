package emulator.kit

import debug.DebugTools
import kotlinx.browser.localStorage
import Constants.WebStorageKey
import emulator.kit.common.FileHandler

/**
 * Load Files from Storage.
 * Modifies [FileHandler.files]
 */
actual fun FileHandler.loadFiles(files: MutableList<FileHandler.File>) {
    files.clear()
    val fileCount = localStorage.getItem(WebStorageKey.FILE_COUNT)?.toIntOrNull() ?: 0
    if (fileCount > 0) {
        for (fileID in 0..<fileCount) {
            val filename = localStorage.getItem("$fileID" + WebStorageKey.FILE_NAME)
            val filecontent = localStorage.getItem("$fileID" + WebStorageKey.FILE_CONTENT)
            val fileUndoLength = localStorage.getItem("$fileID" + WebStorageKey.FILE_UNDO_LENGTH)?.toIntOrNull() ?: 1
            val fileRedoLength = localStorage.getItem("$fileID" + WebStorageKey.FILE_REDO_LENGTH)?.toIntOrNull() ?: 0
            val fileUndoStates = mutableListOf<String>()
            val fileRedoStates = mutableListOf<String>()

            for (undoID in 0..<fileUndoLength) {
                fileUndoStates.add(localStorage.getItem("${fileID}${WebStorageKey.FILE_UNDO}-$undoID") ?: "")
            }
            for (redoID in 0..<fileRedoLength) {
                fileRedoStates.add(localStorage.getItem("${fileID}${WebStorageKey.FILE_UNDO}-$redoID") ?: "")
            }

            if (filename != null && filecontent != null) {
                if (DebugTools.KIT_showFileHandlerInfo) {
                    println("found file: $filename $filecontent ${fileUndoStates.size} ${fileRedoStates.size}")
                }
                files.add(emulator.kit.common.FileHandler.File(filename, filecontent, undoStates = fileUndoStates, redoStates = fileRedoStates))
            }
        }
    } else {
        files.add(emulator.kit.common.FileHandler.File("main.$fileEnding", "", undoStates = mutableListOf(), redoStates = mutableListOf()))
    }
    setCurrent(localStorage.getItem(WebStorageKey.FILE_CURR)?.toIntOrNull() ?: 0)
    if (DebugTools.KIT_showFileHandlerInfo) {
        println("FileHandler.init(): ${files.joinToString { "\n\t" + it.getName() }}")
    }
}


/**
 * Updates local Files to content of [FileHandler.files]
 */
actual fun FileHandler.updateFiles(files: MutableList<FileHandler.File>, onlyCurrent: Boolean, currentID: Int) {
    localStorage.setItem(WebStorageKey.FILE_CURR, currentID.toString())
    if (!onlyCurrent) {
        localStorage.setItem(WebStorageKey.FILE_COUNT, files.size.toString())
        for (fileID in files.indices) {
            val file = files[fileID]
            localStorage.setItem("$fileID" + WebStorageKey.FILE_NAME, file.getName())
            localStorage.setItem("$fileID" + WebStorageKey.FILE_CONTENT, file.getContent())
            localStorage.setItem("$fileID" + WebStorageKey.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
            localStorage.setItem("$fileID" + WebStorageKey.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
            file.getUndoStates().forEach {
                val undoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${WebStorageKey.FILE_UNDO}-$undoID", it)
            }
            file.getRedoStates().forEach {
                val redoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${WebStorageKey.FILE_UNDO}-$redoID", it)
            }
        }
    } else {
        val fileID = currentID
        val file = files[fileID]
        localStorage.setItem("$fileID" + WebStorageKey.FILE_NAME, file.getName())
        localStorage.setItem("$fileID" + WebStorageKey.FILE_CONTENT, file.getContent())
        localStorage.setItem("$fileID" + WebStorageKey.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
        localStorage.setItem("$fileID" + WebStorageKey.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
        file.getUndoStates().forEach {
            val undoID = file.getUndoStates().indexOf(it)
            localStorage.setItem("${fileID}${WebStorageKey.FILE_UNDO}-$undoID", it)
        }
        file.getRedoStates().forEach {
            val redoID = file.getUndoStates().indexOf(it)
            localStorage.setItem("${fileID}${WebStorageKey.FILE_REDO}-$redoID", it)
        }
    }
}