package me.c3.ui.components.editor

import Settings
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.Process
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import emulator.kit.toStyledText
import me.c3.ui.MainManager
import me.c3.ui.styled.editor.*
import javax.swing.SwingUtilities

/**
 * Represents a custom editor component tailored for ProSim application.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 * @property editorFile The file associated with the editor.
 */
class ProSimEditor(private val mainManager: MainManager, val editorFile: EditorFile) : CEditor(mainManager.tm, mainManager.sm, mainManager.icons, maxStackSize = Settings.UNDO_STATE_MAX, stackQueryMillis = Settings.UNDO_DELAY_MILLIS), Highlighter, InfoLogger, ShortCuts {
    init {
        fileInterface = editorFile
        // Attach listeners for various events
        if (editorFile.file.name.endsWith(".s") || editorFile.file.name.endsWith(".S")) highlighter = this
        infoLogger = this
        shortCuts = this

        mainManager.eventManager.addExeEventListener {
            markPC()
        }
        mainManager.eventManager.addCompileListener {
            markPC()
        }
        mainManager.archManager.addArchChangeListener {
            fireCompilation(false)
        }
        mainManager.archManager.addFeatureChangeListener {
            fireCompilation(false)
        }

        markPC()
    }

    /**
     * Fires the compilation process.
     * @param build Specifies if a build is required.
     */
    fun fireCompilation(build: Boolean) {
        nativeLog("fireCompilation")
        CoroutineScope(Dispatchers.Default).launch {
            val result = compile(build)
            if (editorFile.file.name.endsWith(".s") || editorFile.file.name.endsWith(".S")) {
                withContext(Dispatchers.Main) {
                    this@ProSimEditor.setStyledContent(result.tree.source.toStyledText(mainManager.currTheme().codeLaF))
                }
            }
        }
    }

    /**
     * Performs the compilation.
     * @param build Specifies if a build is required.
     * @return The compilation result.
     */
    private suspend fun compile(build: Boolean): Process.Result {
        val result = mainManager.currArch().compile(editorFile.toAsmMainFile(mainManager.currWS()), mainManager.currWS().getImportableFiles(editorFile.file), build)
        SwingUtilities.invokeLater {
            mainManager.eventManager.triggerCompileFinished(result)
        }
        return result
    }

    /**
     * Marks the current program counter position in the editor.
     */
    private fun markPC() {
        val lineLoc = mainManager.currArch().assembler.getLastLineMap()[mainManager.currArch().regContainer.pc.get().toHex().toRawString()]?.firstOrNull {
            editorFile.matches(mainManager.currWS(), it)
        }

        if (lineLoc == null) {
            mark()
            return
        }

        val content = CEditorLineNumbers.LineContent.Text(lineLoc.lineID + 1, mainManager.currTheme().codeLaF.getColor(CodeStyle.GREENPC), ">")
        mark(content)
    }

    /**
     * Handles the click event on a line in the editor.
     * @param lineNumber The line number that was clicked.
     */
    override fun onLineClicked(lineNumber: Int) {
        mainManager.currArch().exeUntilLine(lineNumber, editorFile.getWSRelativeName(mainManager.currWS()))
        mainManager.eventManager.triggerExeEvent()
    }

    /**
     * Highlights the assembly code text.
     * @param text The text to highlight.
     * @return The list of styled characters representing the highlighted text.
     */
    override suspend fun highlight(text: String): List<CEditorArea.StyledChar> {
        val result = compile(false)
        return result.tokens.toStyledText(mainManager.currTheme().codeLaF)
    }

    /**
     * Prints the caret information in the editor.
     * @param caret The current caret position.
     * @param lowSelPosition The start position of the selection.
     * @param highSelPosition The end position of the selection.
     */
    override fun printCaretInfo(caret: InfoLogger.CodePosition, lowSelPosition: InfoLogger.CodePosition, highSelPosition: InfoLogger.CodePosition) {
        val selstring = if (lowSelPosition.index != -1 && highSelPosition.index != -1) {
            "(${highSelPosition.index - lowSelPosition.index})"
        } else ""

        val caretString = if (caret.index != -1) {
            "${caret.line}:${caret.column}"
        } else ""

        mainManager.bBar.editorInfo.text = "$caretString $selstring"
    }

    override fun printError(text: String) {
        mainManager.bBar.setError(text)
    }

    override fun clearError() {
        mainManager.bBar.generalPurpose.text = ""
    }

    override fun ctrlS() {
        fireCompilation(true)
    }
}