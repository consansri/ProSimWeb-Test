package me.c3.emulator.kit

import emulator.kit.assembly.Compiler
import emulator.kit.common.IConsole
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.c3.ui.styled.editor.CEditorArea
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument


fun List<Compiler.Token>.toStyledText(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    return this.map { it.toStyledCharSequence(codeLaF) }.flatten()
}

fun Compiler.Token.toStyledCharSequence(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    val severity = getSeverity()
    val codeStyle = getCodeStyle()

    val color = codeLaF.getColor(severity?.type?.codeStyle ?: codeStyle)
    val style = CEditorArea.Style(color)

    val styledChars: List<CEditorArea.StyledChar> = when (this) {
        is Compiler.Token.Constant.Expression -> {
            this.tokens.map { it.toStyledCharSequence(codeLaF) }.flatten()
        }

        else -> {
            this.content.map { CEditorArea.StyledChar(it, style) }
        }
    }

    return styledChars
}

fun Compiler.Token.hlAndAppendToDoc(codeStyle: CodeLaF, document: StyledDocument) {
    val severity = this.getSeverity()
    val sevAttrs = SimpleAttributeSet()
    val textAttrs = SimpleAttributeSet()
    if (severity != null) {
        when (severity.type) {
            Compiler.SeverityType.ERROR -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.getColor(severity.type.codeStyle))
                StyleConstants.setUnderline(sevAttrs, true)
            }

            Compiler.SeverityType.WARNING -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.getColor(severity.type.codeStyle))
                StyleConstants.setUnderline(sevAttrs, true)
            }

            Compiler.SeverityType.INFO -> {
                StyleConstants.setForeground(sevAttrs, codeStyle.getColor(severity.type.codeStyle))
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
            StyleConstants.setForeground(textAttrs, codeStyle.getColor(this@hlAndAppendToDoc.getCodeStyle()))
            document.insertString(document.length, this@hlAndAppendToDoc.content, textAttrs)
            if (severity != null) document.setCharacterAttributes(document.length - this@hlAndAppendToDoc.content.length, this@hlAndAppendToDoc.content.length, sevAttrs, false)
        }
    }
}

fun List<IConsole.Message>.toStyledContent(codeLaF: CodeLaF): List<CEditorArea.StyledChar>{
    return this.flatMap { it.toStyledContent(codeLaF) }
}

fun IConsole.Message.toStyledContent(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    val color = codeLaF.getColor(type.style)
    val style = CEditorArea.Style(color)
    return "${message}\n".map { CEditorArea.StyledChar(it, style) }
}

fun IConsole.Message.hlAndAppendToDoc(codeStyle: CodeLaF, document: StyledDocument) {
    val textAttrs = SimpleAttributeSet()
    StyleConstants.setForeground(textAttrs, codeStyle.getColor(this.type.style))
    document.insertString(document.length, "${this.time.toLocalDateTime(TimeZone.currentSystemDefault()).time}: " + this.message + "\n", textAttrs)
}

fun Font.install(textPane: JTextPane, scale: Float) {
    SwingUtilities.invokeLater {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(this)

        textPane.font = this.deriveFont(scale)

        val style = textPane.addStyle("JetBrainsMonoFont", null)
        StyleConstants.setFontFamily(style, this.name)
        StyleConstants.setFontSize(style, scale.toInt())

        val doc = textPane.styledDocument
        doc.setCharacterAttributes(0, doc.length, textPane.getStyle("JetBrainsMonoFont"), false)
    }
}
