package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.toStyledText
import me.c3.ui.UIManager
import me.c3.ui.styled.editor.*
import javax.swing.SwingUtilities

class ProSimEditor(private val uiManager: UIManager, val editorFile: EditorFile) : CEditor(uiManager.themeManager, uiManager.scaleManager), Highlighter, InfoLogger {
    init {
        fileInterface = editorFile
        highlighter = this
        infoLogger = this

        uiManager.eventManager.addExeEventListener {
            markPC()
        }
        uiManager.eventManager.addCompileListener {
            markPC()
        }
    }

    override fun onLineClicked(lineNumber: Int) {
        uiManager.currArch().exeUntilLine(lineNumber, editorFile.getName())
        uiManager.eventManager.triggerExeEvent()
    }

    override suspend fun highlight(text: String): List<CEditorArea.StyledChar> {
        val result = compile(false)
        return result.tokens.toStyledText(uiManager.currTheme().codeLaF)
    }

    fun compile(build: Boolean): Compiler.CompileResult {
        val result = uiManager.currArch().compile(editorFile.toCompilerFile(), uiManager.currWS().getCompilerFiles(editorFile.file), build)
        SwingUtilities.invokeLater {
            uiManager.eventManager.triggerCompileFinished(result.success)
        }
        return result
    }

    private fun markPC() {
        val lineLoc = uiManager.currArch().getCompiler().getAssemblyMap().lineAddressMap.get(uiManager.currArch().getRegContainer().pc.get().toHex().toRawString())
        if (lineLoc == null) {
            mark()
            return
        }

        val content = CEditorLineNumbers.LineContent.Text(lineLoc.lineID + 1, uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.GREENPC), ">")
        mark(content)
    }

    override fun printCaretInfo(caret: InfoLogger.CodePosition, lowSelPosition: InfoLogger.CodePosition, highSelPosition: InfoLogger.CodePosition) {
        val selstring = if (lowSelPosition.index != -1 && highSelPosition.index != -1) {
            "(${highSelPosition.index - lowSelPosition.index})"
        } else ""

        val caretString = if (caret.index != -1) {
            "${caret.line}:${caret.column}"
        } else ""

        uiManager.bBar.editorInfo.text = "$caretString $selstring"
    }
}