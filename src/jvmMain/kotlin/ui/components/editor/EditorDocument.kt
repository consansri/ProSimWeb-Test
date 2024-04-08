package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.hlAndAppendToDoc
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Font
import java.io.File
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

class EditorDocument(private val uiManager: UIManager) : DefaultStyledDocument() {
    fun hlDocument(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
        remove(0, length)
        tokens.forEach {
            it.hlAndAppendToDoc(codeStyle, this)
        }
    }

    override fun getFont(attr: AttributeSet?): Font {
        return uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
    }
}