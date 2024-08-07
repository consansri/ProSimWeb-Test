package prosim.ui.components.editor

import Settings
import emulator.kit.Architecture
import emulator.kit.assembler.CodeStyle
import emulator.kit.assembler.Process
import emulator.kit.nativeLog
import emulator.kit.toAsmFile
import emulator.kit.toStyledText
import kotlinx.coroutines.*
import prosim.ui.Events
import prosim.ui.States
import prosim.ui.components.controls.BottomBar
import prosim.uilib.UIStates
import prosim.uilib.state.*
import prosim.uilib.styled.editor.*
import javax.swing.SwingUtilities

/**
 * Represents a custom editor component tailored for ProSim application.
 * @property mainManager The main manager responsible for coordinating UI components and actions.
 * @property editorFile The file associated with the editor.
 */
class ProSimEditor(val editorFile: EditorFile, val bBar: BottomBar) : CEditor(maxStackSize = Settings.UNDO_STATE_MAX, stackQueryMillis = Settings.UNDO_DELAY_MILLIS), Highlighter, InfoLogger, ShortCuts, StateListener<Architecture> {

    private val exeListener = Events.exe.createAndAddListener {
        markPC()
    }

    private val compileListener = Events.compile.createAndAddListener {
        markPC()
    }

    private val featureListener = Events.archFeatureChange.createAndAddListener {
        invokeHL()
    }

    init {
        fileInterface = editorFile
        // Attach listeners for various events
        if (editorFile.file.name.endsWith(".s") || editorFile.file.name.endsWith(".S")) highlighter = this
        infoLogger = this
        shortCuts = this

        States.arch.addEvent(this)

        markPC()
    }

    override suspend fun onStateChange(newVal: Architecture) {
        invokeHL()
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
                    this@ProSimEditor.setStyledContent(result.tree.source.toStyledText(UIStates.theme.get()))
                }
            }
        }
    }

    fun reloadFromDisk() {
        editorFile.reload()
        this.textArea.replaceAll(editorFile.getRawContent())
    }

    /**
     * Performs the compilation.
     * @param build Specifies if a build is required.
     * @return The compilation result.
     */
    private suspend fun compile(build: Boolean): Process.Result {
        val ws = States.ws.get()

        val result = if (ws != null) {
            States.arch.get().compile(editorFile.toAsmMainFile(ws), ws.getImportableFiles(editorFile.file), build)
        } else {
            bBar.setWSWarning("Workspace is not selected! -> Only Single File Assembling available!")
            States.arch.get().compile(editorFile.file.toAsmFile(), listOf(), build)
        }
        SwingUtilities.invokeLater {
            Events.compile.triggerEvent(result)
        }
        return result
    }

    /**
     * Marks the current program counter position in the editor.
     */
    private fun markPC() {
        val ws = States.ws.get()
        if (ws == null) {
            mark()
            return
        }

        val lineLoc = States.arch.get().assembler.getLastLineMap()[States.arch.get().regContainer.pc.get().toHex().toRawString()]?.firstOrNull {
            editorFile.matches(ws, it)
        }

        if (lineLoc == null) {
            mark()
            return
        }

        val content = CEditorLineNumbers.LineContent.Text(lineLoc.lineID + 1, UIStates.theme.get().getColor(CodeStyle.GREENPC), ">")
        mark(content)
    }

    /**
     * Handles the click event on a line in the editor.
     * @param lineNumber The line number that was clicked.
     */
    override fun onLineClicked(lineNumber: Int) {
        States.arch.get().exeUntilLine(lineNumber, editorFile.getWSRelativeName(States.ws.get()))
        Events.exe.triggerEvent(States.arch.get())
    }

    /**
     * Highlights the assembly code text.
     * @param text The text to highlight.
     * @return The list of styled characters representing the highlighted text.
     */
    override suspend fun highlight(text: String): List<CEditorArea.StyledChar> {
        val result = compile(false)
        return result.tokens.toStyledText(UIStates.theme.get())
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

        bBar.editorInfo.text = "$caretString $selstring"
    }

    override fun printError(text: String) {
        bBar.setError(text)
    }

    override fun clearError() {
        bBar.generalPurpose.text = ""
    }

    override fun ctrlS() {
        fireCompilation(true)
    }
}