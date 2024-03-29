package me.c3.emulator.kit

import emulator.kit.assembly.Compiler
import me.c3.ui.theme.core.style.CodeStyle
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

fun Compiler.Token.hlAndAppendToDoc(codeStyle: CodeStyle, document: StyledDocument) {
    val severity = this.getSeverity()
    val sevAttrs = SimpleAttributeSet()
    val textAttrs = SimpleAttributeSet()
    if (severity != null) {
        when (severity.type) {
            Compiler.SeverityType.ERROR -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.codeStyle(severity.type.codeStyle))
                StyleConstants.setUnderline(sevAttrs, true)
            }

            Compiler.SeverityType.WARNING -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.codeStyle(severity.type.codeStyle))
                StyleConstants.setUnderline(sevAttrs, true)
            }

            Compiler.SeverityType.INFO -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.codeStyle(severity.type.codeStyle))
                StyleConstants.setUnderline(sevAttrs, true)
            }
        }
    }

    when (this) {
        is Compiler.Token.Constant.Expression -> {
            this.tokens.forEach {
                it.hlAndAppendToDoc(codeStyle, document)
            }
        }

        else -> {
            StyleConstants.setForeground(textAttrs, codeStyle.codeStyle(this.getCodeStyle()))
            document.insertString(document.length, this.content, textAttrs)
            if (severity != null) document.setCharacterAttributes(document.length - this.content.length, this.content.length, sevAttrs, false)
        }
    }
}