package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.assembly.Compiler
import me.c3.ui.Workspace
import java.io.File
import java.io.FileNotFoundException
import javax.swing.text.SimpleAttributeSet

class FileManager {
    val openFiles = mutableListOf<CodeFile>()
    private val openFilesChangeListeners = mutableListOf<(FileManager) -> Unit>()
    private val currFileEditEventListeners = mutableListOf<(FileManager) -> Unit>()

    private var currentIndex = -1

    fun openFile(file: File) {
        val codeFile = CodeFile(file)
        openFiles.add(codeFile)
        currentIndex = openFiles.indexOf(codeFile)
        triggerFileEvent()
    }

    fun closeFile(file: CodeFile) {
        val codeFileToClose = openFiles.firstOrNull { it == file }
        codeFileToClose?.store()
        openFiles.remove(codeFileToClose)
        triggerFileEvent()
    }

    fun closeAllFiles() {
        openFiles.forEach {
            it.store()
        }
        openFiles.clear()
        triggerFileEvent()
    }

    fun getCurrentFile(): CodeFile? {
        return openFiles.getOrNull(currentIndex)
    }

    fun addOpenFileChangeListener(event: (FileManager) -> Unit) {
        openFilesChangeListeners.add(event)
    }

    fun addCurrFileEditEventListener(event: (FileManager) -> Unit) {
        currFileEditEventListeners.add(event)
    }

    fun removeOpenFileChangeListener(event: (FileManager) -> Unit) {
        openFilesChangeListeners.remove(event)
    }

    fun removeCurrFileEditEventListener(event: (FileManager) -> Unit) {
        currFileEditEventListeners.remove(event)
    }

    fun compileCurrent(arch: Architecture, workspace: Workspace, build: Boolean): Compiler.CompileResult? {
        val file = getCurrentFile()
        file?.let { currFile ->
            val compileResult = arch.compile(currFile.toCompilerFile(), workspace.getCompilerFiles(currFile.file))
            return compileResult
        }
        return null
    }

    private fun triggerFileEvent() {
        openFilesChangeListeners.forEach {
            it(this)
        }
        triggerCurrFileEditEvent()
    }

    private fun triggerCurrFileEditEvent() {
        currFileEditEventListeners.forEach {
            it(this)
        }
    }

    class CodeFile(val file: File, var hlState: List<Compiler.Token>? = null) {
        private var bufferedContent = file.readText()
        fun contentAsString(): String = bufferedContent
        fun getName(): String = file.name
        fun toCompilerFile(): Compiler.CompilerFile = file.toCompilerFile()
        fun reload() {
            bufferedContent = file.readText()
        }

        fun edit(content: String) {
            bufferedContent = content
        }

        fun store() {
            try {
                file.writeText(bufferedContent)
            } catch (e: FileNotFoundException) {
                nativeWarn("File ${getName()} isn't writeable!")
            }
        }

        fun getRawDocument(): CDocument {
            val document = CDocument()
            val attrs = SimpleAttributeSet()
            document.insertString(0, bufferedContent, attrs)
            return document
        }
    }


}