package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.hlAndAppendToDoc
import me.c3.ui.theme.core.style.CodeLaF
import javax.swing.text.*

class CDocument : DefaultStyledDocument() {
    fun hlDocument(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
        remove(0, length)
        tokens.forEach {
            it.hlAndAppendToDoc(codeStyle, this)
        }
    }
}