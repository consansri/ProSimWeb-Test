package emulator.kit

import emulator.kit.common.IConsole
import emulator.kit.assembler.lexer.Token
import me.c3.ui.styled.editor.CEditorArea
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.StyleConstants


fun List<Token>.toStyledText(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    return this.map { it.toStyledCharSequence(codeLaF) }.flatten()
}

fun Token.toStyledCharSequence(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
    val severity = this.getMajorSeverity()

    return this.getHL().flatMap {styled ->
        styled.first.map {
            CEditorArea.StyledChar(it, CEditorArea.Style(codeLaF.getColor(severity?.type?.codeStyle ?: styled.second)))
        }
    }
}

fun List<IConsole.Message>.toStyledContent(codeLaF: CodeLaF): List<CEditorArea.StyledChar> {
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


