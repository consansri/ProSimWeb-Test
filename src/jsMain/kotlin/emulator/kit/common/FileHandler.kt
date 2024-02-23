package emulator.kit.common

import StorageKey
import debug.DebugTools
import emulator.kit.Settings
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler.File
import kotlinx.browser.localStorage
import kotlinx.coroutines.CancellationException
import web.timers.Timeout

/**
 * [FileHandler] loads and saves [File]s to memory and holds undo/redo states.
 */
class FileHandler(private val fileEnding: String) {

    private val files = mutableListOf<File>()
    private var currentID: Int = 0

    init {
        getFromLocalStorage()
    }

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
                println("FileHandler: import file ${file.getName()}\n\t${file.getContent().replace("\n", "\n\t")}")
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
        files.clear()
        val fileCount = localStorage.getItem(StorageKey.FILE_COUNT)?.toIntOrNull() ?: 0
        if (fileCount > 0) {
            for (fileID in 0..<fileCount) {
                val filename = localStorage.getItem("$fileID" + StorageKey.FILE_NAME)
                val filecontent = localStorage.getItem("$fileID" + StorageKey.FILE_CONTENT)
                val fileUndoLength = localStorage.getItem("$fileID" + StorageKey.FILE_UNDO_LENGTH)?.toIntOrNull() ?: 1
                val fileRedoLength = localStorage.getItem("$fileID" + StorageKey.FILE_REDO_LENGTH)?.toIntOrNull() ?: 0
                val fileUndoStates = mutableListOf<String>()
                val fileRedoStates = mutableListOf<String>()

                for (undoID in 0..<fileUndoLength) {
                    fileUndoStates.add(localStorage.getItem("${fileID}${StorageKey.FILE_UNDO}-$undoID") ?: "")
                }
                for (redoID in 0..<fileRedoLength) {
                    fileRedoStates.add(localStorage.getItem("${fileID}${StorageKey.FILE_UNDO}-$redoID") ?: "")
                }

                if (filename != null && filecontent != null) {
                    if (DebugTools.KIT_showFileHandlerInfo) {
                        println("found file: $filename $filecontent ${fileUndoStates.size} ${fileRedoStates.size}")
                    }
                    files.add(File(filename, filecontent, undoStates =  fileUndoStates, redoStates =  fileRedoStates))
                }
            }
        } else {
            files.add(File("main.$fileEnding", "", undoStates =  mutableListOf(), redoStates =  mutableListOf()))
        }
        setCurrent(localStorage.getItem(StorageKey.FILE_CURR)?.toIntOrNull() ?: 0)
        if (DebugTools.KIT_showFileHandlerInfo) {
            println("FileHandler.init(): ${files.joinToString { "\n\t" + it.getName() }}")
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

    data class File(private var name: String, private var content: String, private val undoStates: MutableList<String> = mutableListOf(), private val redoStates: MutableList<String> = mutableListOf()) {
        private var timeout: Timeout? = null
        private var syntaxTree: Syntax.SyntaxTree? = null

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
            timeout?.let {
                web.timers.clearTimeout(it)
            }
            timeout = web.timers.setTimeout({
                try {
                    undoStates.add(0, content)
                    if (undoStates.size > Settings.UNDO_STATE_COUNT) {
                        undoStates.removeLast()
                    }
                    fileHandler.refreshLocalStorage(true)

                } catch (_: CancellationException) {

                } finally {

                }
            }, Settings.UNDO_DELAY_MILLIS)
        }

    }

}