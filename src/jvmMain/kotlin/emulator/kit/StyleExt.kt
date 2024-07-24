package emulator.kit

import emulator.kit.assembler.lexer.Token
import emulator.kit.common.IConsole
import prosim.uilib.styled.editor.CEditorArea
import prosim.uilib.theme.core.Theme
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.StyleConstants

fun List<Token>.toStyledText(theme: Theme): List<CEditorArea.StyledChar> {
    return this.map { it.toStyledCharSequence(theme) }.flatten()
}

fun Token.toStyledCharSequence(theme: Theme): List<CEditorArea.StyledChar> {
    val severity = this.getMajorSeverity()
    val sevColor = if (severity?.type?.codeStyle != null) {
        theme.getColor(severity.type.codeStyle)
    } else {
        null
    }

    return this.getHL().flatMap { styled ->
        styled.first.map {
            CEditorArea.StyledChar(it, CEditorArea.Style(theme.getColor(styled.second), null, underline = sevColor))
        }
    }
}

fun List<IConsole.Message>.toStyledContent(theme: Theme): List<CEditorArea.StyledChar> {
    return this.flatMap { it.toStyledContent(theme) }
}

fun IConsole.Message.toStyledContent(theme: Theme): List<CEditorArea.StyledChar> {
    val color = theme.getColor(type.style)
    val style = CEditorArea.Style(color)
    return "${message}\n".map { CEditorArea.StyledChar(it, style) }
}

fun Font.install(textPane: JTextPane) {
    SwingUtilities.invokeLater {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(this)

        textPane.font = this

        val style = textPane.addStyle("JetBrainsMonoFont", null)
        StyleConstants.setFontFamily(style, this.name)
        StyleConstants.setFontSize(style, this.size)

        val doc = textPane.styledDocument
        doc.setCharacterAttributes(0, doc.length, textPane.getStyle("JetBrainsMonoFont"), false)
    }
}


