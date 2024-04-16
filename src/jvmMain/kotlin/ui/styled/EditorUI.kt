package me.c3.ui.styled

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import me.c3.emulator.kit.install
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class EditorUI(
    private val themeManager: ThemeManager,
    private val scaleManager: ScaleManager
) : ComponentUI() {

    private var selectionColor = themeManager.curr.codeLaF.selectionColor

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val editor = c as? Editor ?: return

        themeManager.addThemeChangeListener {
            setDefaults(editor)
        }
        scaleManager.addScaleChangeEvent {
            setDefaults(editor)
        }

        setDefaults(editor)
    }

    private fun setDefaults(editor: Editor) {
        editor.isOpaque = false
        editor.border = BorderFactory.createEmptyBorder(0, scaleManager.curr.borderScale.insets, 0, scaleManager.curr.borderScale.insets)
        editor.background = themeManager.curr.globalLaF.bgPrimary
        editor.foreground = themeManager.curr.codeLaF.getColor(Compiler.CodeStyle.BASE0)
        editor.font = themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)
        selectionColor = themeManager.curr.codeLaF.selectionColor
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return
        val editor = c as? Editor ?: return

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        val fontMetrics = editor.getFontMetrics(editor.font)

        g2d.color = editor.background
        //g2d.fillRect(editor.x, editor.y, editor.width, editor.height)

        var x = editor.insets.left
        var y = editor.insets.top + fontMetrics.ascent

        // Render styled characters
        for ((i, char) in editor.styledText.withIndex()) {
            val style = char.style
            val charWidth = fontMetrics.charWidth(char.content)

            // Draw Selection
            val absSelection = editor.getAbsSelection()
            if (i in absSelection.lowIndex until absSelection.highIndex) {
                g2d.color = Color(selectionColor.red, selectionColor.green, selectionColor.blue, 77)
                if (char.content == '\n') {
                    g2d.fillRect(x, y - fontMetrics.ascent, editor.bounds.width - x, fontMetrics.height)
                } else {
                    g2d.fillRect(x, y - fontMetrics.ascent, charWidth, fontMetrics.height)
                }
            }

            // Draw the cursor
            if (i == editor.caretPos) {
                g2d.color = editor.foreground
                g2d.drawLine(x, y + fontMetrics.descent, x, y + fontMetrics.descent - fontMetrics.height)
            }

            // Draw Characters
            g2d.color = style?.fgColor ?: editor.foreground

            if (char.content == '\n') {
                x = editor.insets.left
                y += fontMetrics.height
            } else {
                g2d.drawString(char.content.toString(), x, y)
                x += charWidth
            }


        }
        if (editor.styledText.size == editor.caretPos) {
            g2d.color = editor.foreground
            g2d.drawLine(x, y + fontMetrics.descent, x, y + fontMetrics.descent - fontMetrics.height)
        }

        g2d.dispose()
    }



}