package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.assembly.Compiler
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import javax.swing.text.SimpleAttributeSet

class EditorFile(val file: File) {

    private val undoStates = mutableListOf<String>()
    private val redoStates = mutableListOf<String>()
    private var undoSaveJob: Job? = null

    private var bufferedContent = file.readText()
        set(value) {
            field = value
            CoroutineScope(Dispatchers.IO).launch {
                store()
            }
        }

    fun contentAsString(): String = bufferedContent
    fun getName(): String = file.name
    fun toCompilerFile(): Compiler.CompilerFile = file.toCompilerFile()
    fun reload() {
        bufferedContent = file.readText()
    }

    fun edit(content: String) {
        val oldContent = bufferedContent
        bufferedContent = content
        if (oldContent != content) {
            undoSaveJob?.cancel()
            undoSaveJob = CoroutineScope(Dispatchers.Default).launch {
                delay(1500)
                undoStates.add(oldContent)
                while (undoStates.size > 30) {
                    undoStates.removeFirst()
                }
            }
        }
    }

    fun undo(): Boolean {
        if (undoStates.isEmpty()) return false
        val lastAdded = undoStates.removeLast()
        redoStates.add(bufferedContent)
        while (redoStates.size > 30) {
            redoStates.removeFirst()
        }
        bufferedContent = lastAdded
        return true
    }

    fun redo(): Boolean {
        if (redoStates.isEmpty()) return false
        val lastAdded = redoStates.removeLast()
        undoStates.add(bufferedContent)
        bufferedContent = lastAdded
        return true
    }

    suspend fun store() {
        withContext(Dispatchers.IO) {
            try {
                file.writeText(bufferedContent)
            } catch (e: FileNotFoundException) {
                nativeWarn("File ${getName()} isn't writeable!")
            }
        }
    }

    fun getRawDocument(): CDocument {
        val document = CDocument()
        val attrs = SimpleAttributeSet()
        document.insertString(0, bufferedContent, attrs)
        return document
    }
}

