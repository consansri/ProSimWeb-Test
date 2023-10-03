package emulator.kit.common

import StorageKey
import emulator.kit.Settings
import emulator.kit.assembly.Syntax
import kotlinx.browser.localStorage
import kotlinx.coroutines.*
import debug.DebugTools

/**
 * [FileHandler]
 */
class FileHandler(val fileEnding: String) {

    private val files = mutableListOf<File>()
    private var currentID: Int = 0

    fun import(file: File): Boolean {
        return if (files.map { it.getName() }.contains(file.getName())) {
            console.warn("couldn't import file cause filename duplicate recognized!")
            false

        } else if (file.getName().isEmpty()) {
            console.warn("couldn't import file cause filename is empty!")
            false
        } else {
            this.files.add(file)
            this.currentID = this.files.indexOf(file)
            if (DebugTools.KIT_showFileHandlerInfo) {
                console.log("FileHandler: import file ${file.getName()}\n\t${file.getContent().replace("\n", "\n\t")}")
            }
            refreshLocalStorage(true)
            true
        }
    }

    fun remove(file: File) {
        this.files.remove(file)
        if (files.isEmpty()) {
            files.add(File("main.$fileEnding", ""))
        }
        setCurrent(0)
        refreshLocalStorage(true)
    }

    fun renameCurrent(newName: String): Boolean {
        return if (files.map { it.getName() }.contains(newName)) {
            console.warn("couldn't import file cause filename duplicate recognized!")
            false

        } else if (newName.isEmpty()) {
            console.warn("couldn't import file cause filename is empty!")
            false
        } else {
            if (DebugTools.KIT_showFileHandlerInfo) {
                console.log("FileHandler: rename file ${files[currentID].getName()} to ${newName}")
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

    fun getCurrID(): Int = currentID
    fun getCurrUndoLength(): Int = files[currentID].getUndoStates().size

    fun getCurrRedoLength(): Int = files[currentID].getRedoStates().size

    fun getCurrContent(): String = files[currentID].getContent()

    fun setCurrent(index: Int) {
        currentID = if (index in files.indices) index else 0
    }

    fun getCurrent(): File = files[currentID]

    fun getAllFiles(): List<File> = files

    fun getFromLocalStorage() {
        files.clear()
        val fileCount = localStorage.getItem(StorageKey.FILE_COUNT)?.toIntOrNull() ?: 0
        if (fileCount > 0) {
            for (fileID in 0 until fileCount) {
                val filename = localStorage.getItem("$fileID" + StorageKey.FILE_NAME)
                val filecontent = localStorage.getItem("$fileID" + StorageKey.FILE_CONTENT)
                val fileUndoLength = localStorage.getItem("$fileID" + StorageKey.FILE_UNDO_LENGTH)?.toIntOrNull() ?: 1
                val fileRedoLength = localStorage.getItem("$fileID" + StorageKey.FILE_REDO_LENGTH)?.toIntOrNull() ?: 0
                val fileUndoStates = mutableListOf<String>("")
                val fileRedoStates = mutableListOf<String>()
                for (undoID in 0 until fileUndoLength) {
                    fileUndoStates.add(localStorage.getItem("${fileID}${StorageKey.FILE_UNDO}-$undoID") ?: "")
                }
                for (redoID in 0 until fileRedoLength) {
                    fileRedoStates.add(localStorage.getItem("${fileID}${StorageKey.FILE_UNDO}-$redoID") ?: "")
                }

                if (filename != null && filecontent != null) {
                    if (DebugTools.KIT_showFileHandlerInfo) {
                        console.log("found file: $filename $filecontent")
                    }
                    files.add(File(filename, filecontent, fileUndoStates, fileRedoStates))
                }
            }
        } else {
            files.add(File("main.$fileEnding", ""))
        }
        setCurrent(localStorage.getItem(StorageKey.FILE_CURR)?.toIntOrNull() ?: 0)
        if (DebugTools.KIT_showFileHandlerInfo) {
            console.log("FileHandler.init(): ${files.joinToString { "\n\t" + it.getName() }}")
        }
    }

    private fun refreshLocalStorage(all: Boolean) {
        localStorage.setItem(StorageKey.FILE_CURR, currentID.toString())
        if (all) {
            localStorage.setItem(StorageKey.FILE_COUNT, files.size.toString())
            for (fileID in files.indices) {
                val file = files[fileID]
                localStorage.setItem("$fileID" + StorageKey.FILE_NAME, file.getName())
                localStorage.setItem("$fileID" + StorageKey.FILE_CONTENT, file.getContent())
                localStorage.setItem("$fileID" + StorageKey.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
                localStorage.setItem("$fileID" + StorageKey.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
                file.getUndoStates().forEach {
                    val undoID = file.getUndoStates().indexOf(it)
                    localStorage.setItem("${fileID}${StorageKey.FILE_UNDO}-$undoID", it)
                }
                file.getRedoStates().forEach {
                    val redoID = file.getUndoStates().indexOf(it)
                    localStorage.setItem("${fileID}${StorageKey.FILE_UNDO}-$redoID", it)
                }
            }
        } else {
            val fileID = currentID
            val file = files[fileID]
            localStorage.setItem("$fileID" + StorageKey.FILE_NAME, file.getName())
            localStorage.setItem("$fileID" + StorageKey.FILE_CONTENT, file.getContent())
            localStorage.setItem("$fileID" + StorageKey.FILE_UNDO_LENGTH, file.getUndoStates().size.toString())
            localStorage.setItem("$fileID" + StorageKey.FILE_REDO_LENGTH, file.getRedoStates().size.toString())
            file.getUndoStates().forEach {
                val undoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${StorageKey.FILE_UNDO}-$undoID", it)
            }
            file.getRedoStates().forEach {
                val redoID = file.getUndoStates().indexOf(it)
                localStorage.setItem("${fileID}${StorageKey.FILE_REDO}-$redoID", it)
            }
        }
    }

    data class File(private var name: String, private var content: String, private val hlLines: MutableList<String> = mutableListOf(), private val undoStates: MutableList<String> = mutableListOf(), private val redoStates: MutableList<String> = mutableListOf()) {

        var job: Job? = null
        var syntaxTree: Syntax.SyntaxTree? = null

        fun rename(newName: String) {
            name = newName
        }

        fun getName(): String {
            return name
        }

        fun setContent(fileHandler: FileHandler, content: String) {
            this.addUndoState(fileHandler, content)
            this.content = content
        }

        fun undo() {
            if (undoStates.isNotEmpty()) {
                this.redoStates.add(0, this.content)
                if (this.redoStates.size > Settings.REDO_STATE_COUNT) {
                    this.redoStates.removeLast()
                }
                undoStates.removeFirst()
                this.content = undoStates.first()
            }
        }

        fun redo() {
            if (redoStates.isNotEmpty()) {
                this.undoStates.add(0, this.content)
                if (this.undoStates.size > Settings.UNDO_STATE_COUNT) {
                    this.undoStates.removeLast()
                }
                this.content = redoStates.first()
                redoStates.removeFirst()
            }
        }

        fun linkGrammarTree(syntaxTree: Syntax.SyntaxTree) {
            this.syntaxTree = syntaxTree
        }

        fun getLinkedTree(): Syntax.SyntaxTree? {
            return syntaxTree
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

        private fun addUndoState(fileHandler: FileHandler, content: String) {
            if (content != undoStates.firstOrNull()) {
                job?.cancel()
                job = GlobalScope.launch {
                    try {
                        delay(Settings.UNDO_DELAY_MILLIS)
                        undoStates.add(0, content)
                        if (undoStates.size > Settings.UNDO_STATE_COUNT) {
                            undoStates.removeLast()
                        }
                        fileHandler.refreshLocalStorage(true)

                    } catch (e: CancellationException) {

                    } finally {

                    }
                }
            }
        }

    }

}