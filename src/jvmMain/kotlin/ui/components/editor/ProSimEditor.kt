package me.c3.ui.components.editor

import Settings
import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.toStyledText
import me.c3.ui.MainManager
import me.c3.ui.styled.editor.*
import javax.swing.SwingUtilities

class ProSimEditor(private val mainManager: MainManager, val editorFile: EditorFile) : CEditor(mainManager.themeManager, mainManager.scaleManager, maxStackSize = Settings.UNDO_STATE_MAX, stackQueryMillis = Settings.UNDO_DELAY_MILLIS), Highlighter, InfoLogger {
    init {
        fileInterface = editorFile
        highlighter = this
        infoLogger = this

        mainManager.eventManager.addExeEventListener {
            markPC()
        }
        mainManager.eventManager.addCompileListener {
            markPC()
        }
    }

    override fun onLineClicked(lineNumber: Int) {
        mainManager.currArch().exeUntilLine(lineNumber, editorFile.getName())
        mainManager.eventManager.triggerExeEvent()
    }

    override suspend fun highlight(text: String): List<CEditorArea.StyledChar> {
        val result = compile(false)
        return result.tokens.toStyledText(mainManager.currTheme().codeLaF)
    }

    fun compile(build: Boolean): Compiler.CompileResult {
        val result = mainManager.currArch().compile(editorFile.toCompilerFile(), mainManager.currWS().getCompilerFiles(editorFile.file), build)
        SwingUtilities.invokeLater {
            mainManager.eventManager.triggerCompileFinished(result.success)
        }
        return result
    }

    private fun markPC() {
        val lineLoc = mainManager.currArch().getCompiler().getAssemblyMap().lineAddressMap.get(mainManager.currArch().getRegContainer().pc.get().toHex().toRawString())
        if (lineLoc == null) {
            mark()
            return
        }

        val content = CEditorLineNumbers.LineContent.Text(lineLoc.lineID + 1, mainManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.GREENPC), ">")
        mark(content)
    }

    override fun printCaretInfo(caret: InfoLogger.CodePosition, lowSelPosition: InfoLogger.CodePosition, highSelPosition: InfoLogger.CodePosition) {
        val selstring = if (lowSelPosition.index != -1 && highSelPosition.index != -1) {
            "(${highSelPosition.index - lowSelPosition.index})"
        } else ""

        val caretString = if (caret.index != -1) {
            "${caret.line}:${caret.column}"
        } else ""

        mainManager.bBar.editorInfo.text = "$caretString $selstring"
    }
}