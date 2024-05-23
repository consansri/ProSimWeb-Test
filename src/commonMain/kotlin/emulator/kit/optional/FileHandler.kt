package emulator.kit.optional

import Coroutines
import debug.DebugTools
import emulator.kit.assembler.AssemblerFile
import emulator.kit.loadFiles
import emulator.kit.nativeWarn
import emulator.kit.updateFiles
import kotlinx.coroutines.*

class FileHandler() {

    private val files = mutableListOf<File>()
    private var currentID: Int = 0

    init {
        getFromLocalStorage()
    }

    fun import(file: File): Boolean {
        return if (files.map { it.getName() }.contains(file.getName())) {
            nativeWarn("couldn't import file cause filename duplicate recognized!")
            false

        } else if (file.getName().isEmpty()) {
            nativeWarn("couldn't import file cause filename is empty!")
            false
        } else {
            this.files.add(file)
            this.currentID = this.files.indexOf(file)
            if (DebugTools.KIT_showFileHandlerInfo) {
                println("FileHandler: import file ${file.getName()}\n\t${file.getContent().replace("\n", "\n\t")}")
            }
            refreshLocalStorage(true)
            true
        }
    }

    fun remove(file: File) {
        this.files.remove(file)
        if (files.isEmpty()) {
            files.add(File("main.s", ""))
        }
        setCurrent(0)
        refreshLocalStorage(true)
    }

    fun renameCurrent(newName: String): Boolean {
        return if (files.map { it.getName() }.contains(newName)) {
            nativeWarn("couldn't import file cause filename duplicate recognized!")
            false

        } else if (newName.isEmpty()) {
            nativeWarn("couldn't import file cause filename is empty!")
            false
        } else {
            if (DebugTools.KIT_showFileHandlerInfo) {
                println("FileHandler: rename file ${files[currentID].getName()} to $newName")
            }
            files[currentID].rename(newName)
            refreshLocalStorage(true)
            true
        }
    }

    fun undoCurr() {
        files[currentID].undo()
        refreshLocalStorage(false)
    }

    fun redoCurr() {
        files[currentID].redo()
        refreshLocalStorage(false)
    }

    fun editCurr(content: String) {
        files[currentID].setContent(this, content)
        refreshLocalStorage(false)
    }

    fun getCurrNameWithoutType(): String {
        val name = files[currentID].getName()
        val index = name.lastIndexOf('.')
        return if (index > 0) {
            name.substring(0, index)
        } else {
            name
        }
    }

    fun getOrNull(fileName: String): File? {
        return files.firstOrNull { it.getName() == fileName }
    }

    fun getOthers(): List<AssemblerFile> {
        return (files - files[currentID]).map { it.toCompilerFile() }
    }

    fun getCurrID(): Int = currentID
    fun getCurrUndoLength(): Int = files[currentID].getUndoStates().size

    fun getCurrRedoLength(): Int = files[currentID].getRedoStates().size

    fun getCurrContent(): String = files[currentID].getContent()

    fun setCurrent(index: Int) {
        currentID = if (index in files.indices) index else 0
    }

    fun getCurrent(): File = files[currentID]

    fun getAllFiles(): List<File> = files


    private fun getFromLocalStorage() {
        loadFiles(this.files)
    }

    private fun refreshLocalStorage(all: Boolean) {
        updateFiles(files, !all, currentID)
    }

    class File(private var name: String, private var content: String, private val undoStates: MutableList<String> = mutableListOf(), private val redoStates: MutableList<String> = mutableListOf()) {

        private var job: Job? = null

        fun rename(newName: String) {
            name = newName
        }

        fun getName(): String {
            return name
        }

        fun setContent(fileHandler: FileHandler, content: String) {
            this.addUndoState(fileHandler, content)
            this.redoStates.clear()
            this.content = content
        }

        fun undo() {
            if (undoStates.isNotEmpty()) {
                this.redoStates.add(0, this.content)
                if (this.redoStates.size > Settings.REDO_STATE_MAX) {
                    this.redoStates.removeLast()
                }
                undoStates.removeFirst()
                this.content = undoStates.first()
            }
        }

        fun redo() {
            if (redoStates.isNotEmpty()) {
                this.undoStates.add(0, this.content)
                if (this.undoStates.size > Settings.UNDO_STATE_MAX) {
                    this.undoStates.removeLast()
                }
                this.content = redoStates.first()
                redoStates.removeFirst()
            }
        }

        fun getContent(): String {
            return this.content
        }

        fun getUndoStates(): List<String> {
            return undoStates
        }

        fun getRedoStates(): List<String> {
            return redoStates
        }

        fun toCompilerFile(): AssemblerFile = AssemblerFile(name, content)

        private fun addUndoState(fileHandler: FileHandler, content: String) {
            job?.cancel()

            job = Coroutines.setTimeout(Settings.UNDO_DELAY_MILLIS) {
                try {
                    undoStates.add(0, content)
                    if (undoStates.size > Settings.UNDO_STATE_MAX) {
                        undoStates.removeLast()
                    }
                    fileHandler.refreshLocalStorage(true)

                } catch (_: CancellationException) {

                } finally {

                }
            }
        }

    }

}