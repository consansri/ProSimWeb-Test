package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.assembler.AsmFile
import emulator.kit.assembler.lexer.Token
import kotlinx.coroutines.*
import me.c3.ui.workspace.Workspace
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
     * @return The relative file path from the workspace root directory.
     */
    fun getWSRelativeName(ws: Workspace): String = file.toRelativeString(ws.rootDir).replace('\\', '/')

    /**
     * @return true if [Token.LineLoc.file] points on [this]. Checked by Workspace dependent name.
     */
    fun matches(ws: Workspace, lineLoc: Token.LineLoc): Boolean {
       return lineLoc.file.wsRelativeName == getWSRelativeName(ws)
    }

    /**
     * Converts the editor file to a compiler file.
     * @return The compiler file object.
     */
    fun toAsmMainFile(workspace: Workspace): AsmFile = file.toAsmFile(file, workspace.rootDir)

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

