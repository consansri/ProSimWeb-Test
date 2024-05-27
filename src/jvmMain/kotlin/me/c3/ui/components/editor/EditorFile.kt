package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.assembler.AssemblerFile
import kotlinx.coroutines.*
import me.c3.ui.styled.editor.FileInterface
import java.io.File
import java.io.FileNotFoundException

/**
 * Represents a file associated with an editor.
 * @property file The file object.
 */
class EditorFile(val file: File) : FileInterface {

    // Buffered content of the file
    private var bufferedContent = file.readText()
        set(value) {
            field = value
            CoroutineScope(Dispatchers.IO).launch {
                store()
            }
        }

    /**
     * Retrieves the name of the file.
     * @return The name of the file.
     */
    fun getName(): String = file.name

    /**
     * Converts the editor file to a compiler file.
     * @return The compiler file object.
     */
    fun toCompilerFile(): AssemblerFile = file.toAsmFile(file.parentFile)

    /**
     * Stores the buffered content of the file.
     */
    private suspend fun store() {
        withContext(Dispatchers.IO) {
            try {
                file.writeText(bufferedContent)
            } catch (e: FileNotFoundException) {
                nativeWarn("File ${getName()} isn't writeable!")
            }
        }
    }

    /**
     * Retrieves the raw content of the file.
     * @return The raw content of the file.
     */
    override fun getRawContent(): String = bufferedContent

    /**
     * Handles the content change event.
     * @param text The new content of the file.
     */
    override suspend fun contentChanged(text: String) {
        bufferedContent = text
    }
}

