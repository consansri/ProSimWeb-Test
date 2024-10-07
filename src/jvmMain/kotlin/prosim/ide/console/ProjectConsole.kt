package prosim.ide.console

import cengine.editor.CodeEditor
import cengine.editor.annotation.Severity
import emulator.kit.assembler.CodeStyle
import prosim.uilib.UIStates
import prosim.uilib.styled.editor.CConsole
import prosim.uilib.styled.editor.CEditorArea

class ProjectConsole: CConsole() {

    fun dump(codeEditor: CodeEditor){
        val chars = codeEditor.annotations.flatMap {
            val style = CEditorArea.Style(when(it.severity){
                Severity.INFO -> null
                Severity.WARNING -> UIStates.theme.get().getColor(CodeStyle.YELLOW)
                Severity.ERROR -> UIStates.theme.get().getColor(CodeStyle.RED)
            })
            (it.message + '\n').map { char -> CEditorArea.StyledChar(char, style) }
        }
        updateContent(chars)
    }

}