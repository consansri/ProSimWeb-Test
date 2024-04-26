package me.c3.ui.components.editor

import Settings
import emulator.kit.compiler.CodeStyle
import emulator.kit.compiler.Process
import emulator.kit.nativeLog
import kotlinx.coroutines.*
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
        mainManager.archManager.addArchChangeListener {
            fireCompilation(false)
        }
        mainManager.archManager.addFeatureChangeListener {
            fireCompilation(false)
        }
    }

    override fun onLineClicked(lineNumber: Int) {
        mainManager.currArch().exeUntilLine(lineNumber, editorFile.getName())
        mainManager.eventManager.triggerExeEvent()
    }

    override suspend fun highlight(text: String): List<CEditorArea.StyledChar> {
        nativeLog("ProSimEditor: fire highlighting")
        val result = compile(false)
        nativeLog("ProSimEditor: highlight finished!")
        return result.tokens.toStyledText(mainManager.currTheme().codeLaF)
    }

    private suspend fun compile(build: Boolean): Process.Result {
        val result = mainManager.currArch().compile(editorFile.toCompilerFile(), mainManager.currWS().getCompilerFiles(editorFile.file), build).await()
        SwingUtilities.invokeLater {
            mainManager.eventManager.triggerCompileFinished(result.success)
        }
        return result
    }

    fun fireCompilation(build: Boolean) {
        nativeLog("fireCompilation")
        CoroutineScope(Dispatchers.Default).launch {
            val result = compile(build)
            withContext(Dispatchers.Main){
                this@ProSimEditor.setStyledContent(result.tokens.toStyledText(mainManager.currTheme().codeLaF))
            }
        }
    }

    private fun markPC() {
        val lineLoc = mainManager.currArch().getCompiler().assembly.currentAssemblyMap.lineAddressMap.get(mainManager.currArch().getRegContainer().pc.get().toHex().toRawString())
        if (lineLoc == null) {
            mark()
            return
        }

        val content = CEditorLineNumbers.LineContent.Text(lineLoc.lineID + 1, mainManager.currTheme().codeLaF.getColor(CodeStyle.GREENPC), ">")
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