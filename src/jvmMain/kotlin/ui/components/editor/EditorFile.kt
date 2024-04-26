package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.compiler.CompilerFile
import kotlinx.coroutines.*
import me.c3.ui.styled.editor.FileInterface
import java.io.File
import java.io.FileNotFoundException

class EditorFile(val file: File) : FileInterface {

    private var bufferedContent = file.readText()
        set(value) {
            field = value
            CoroutineScope(Dispatchers.IO).launch {
                store()
            }
        }

    fun getName(): String = file.name
    fun toCompilerFile(): CompilerFile = file.toCompilerFile()
    fun reload() {
        bufferedContent = file.readText()
    }

    fun edit(content: String) {
        bufferedContent = content
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

    override fun getRawContent(): String = bufferedContent

    override suspend fun contentChanged(text: String) {
        bufferedContent = text
    }
}

