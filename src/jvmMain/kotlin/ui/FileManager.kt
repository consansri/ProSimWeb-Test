package me.c3.ui

import emulator.kit.nativeLog
import java.io.File

class FileManager {

    private val openFiles = mutableListOf<File>()
    private val openFilesChangeListeners = mutableListOf<(FileManager) -> Unit>()
    private var currentIndex = -1

    fun openFile(file: File) {
        openFiles.add(file)
        currentIndex = openFiles.indexOf(file)
        triggerFileEvent()
    }

    fun getCurrentFile(): File? {
        return openFiles.getOrNull(currentIndex)
    }

    fun addOpenFileChangeListener(event: (FileManager) -> Unit) {
        openFilesChangeListeners.add(event)
    }

    fun removeOpenFileChangeListener(event: (FileManager) -> Unit) {
        openFilesChangeListeners.remove(event)
    }

    private fun triggerFileEvent() {
        nativeLog("File Event: ${openFiles.joinToString() { it.name }}")
        openFilesChangeListeners.forEach {
            it(this)
        }
    }


}