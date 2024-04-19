package me.c3.emulator.kit

import emulator.kit.assembly.Compiler
import emulator.kit.common.Docs
import emulator.kit.common.IConsole
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.c3.ui.styled.editor.CEditorArea
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.JComponent
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

fun List<IConsole.Message>.toStyledContent(codeLaF: CodeLaF): List<CEditorArea.StyledChar>{
    return this.flatMap { it.toStyledContent(codeLaF) }
}

fun IConsole.Message.toStyledContent(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    val color = codeLaF.getColor(type.style)
    val style = CEditorArea.Style(color)
    return "${message}\n".map { CEditorArea.StyledChar(it, style) }
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


