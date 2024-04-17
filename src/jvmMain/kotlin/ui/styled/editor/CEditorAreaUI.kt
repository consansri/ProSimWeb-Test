package me.c3.ui.styled.editor

import emulator.kit.assembly.Compiler
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.Timer
import javax.swing.plaf.ComponentUI

class CEditorAreaUI(
    private val themeManager: ThemeManager,
    private val scaleManager: ScaleManager
) : ComponentUI() {
    companion object {
        fun installScrollPane(editor: CEditorArea, scrollPane: CScrollPane) {
            scrollPane.verticalScrollBar.unitIncrement = editor.getFontMetrics(editor.font).height
            scrollPane.horizontalScrollBar.unitIncrement = editor.getFontMetrics(editor.font).charWidth(' ')
        }
    }

    private var defaultSelectionColor = Color(0, 0, 0, 0)
    private var caretLineBG = Color(0, 0, 0, 0)

    private var caretTimer: Timer? = null
    private var caretColor: Color? = null

    private var selectionColor = themeManager.curr.codeLaF.selectionColor
        set(value) {
            field = value
            defaultSelectionColor = Color(selectionColor.red, selectionColor.green, selectionColor.blue, 77)
            caretLineBG = Color(selectionColor.red, selectionColor.green, selectionColor.blue, 20)
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val CEditorArea = c as? CEditorArea ?: return

        themeManager.addThemeChangeListener {
            setDefaults(CEditorArea)
        }
        scaleManager.addScaleChangeEvent {
            setDefaults(CEditorArea)
        }

        setDefaults(CEditorArea)
    }

    private fun setDefaults(editor: CEditorArea) {
        editor.isOpaque = false
        editor.border = BorderFactory.createEmptyBorder(0, scaleManager.curr.borderScale.insets, 0, scaleManager.curr.borderScale.insets)
        editor.background = themeManager.curr.globalLaF.bgPrimary
        editor.foreground = themeManager.curr.codeLaF.getColor(Compiler.CodeStyle.BASE0)
        editor.font = themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)
        selectionColor = themeManager.curr.codeLaF.selectionColor
        editor.scrollPane.verticalScrollBar.unitIncrement = editor.getFontMetrics(editor.font).height
        editor.scrollPane.horizontalScrollBar.unitIncrement = editor.getFontMetrics(editor.font).charWidth(' ')
        editor.tabSize = scaleManager.curr.fontScale.tabSize
        editor.focusTraversalKeysEnabled = false

        caretTimer?.stop()
        caretTimer = null
        caretTimer = Timer(500) {
            caretColor = if (caretColor == null) {
                editor.foreground
            } else {
                null
            }
            editor.repaint()
        }
        caretTimer?.start()
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return
        val editor = c as? CEditorArea ?: return

        val fm = editor.getFontMetrics(editor.font)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        g2d.color = editor.background
        //g2d.fillRect(editor.x, editor.y, editor.width, editor.height)

        var x = editor.insets.left
        var column = 0
        var y = editor.insets.top + fm.ascent
        val currLine = editor.caretLine
        var lineCounter = 0


        if (editor.selEnd == -1 || editor.selStart == -1) {
            g2d.color = caretLineBG
            g2d.fillRect(x, y + (currLine - 1) * fm.height - fm.ascent, editor.bounds.width - x - editor.insets.right, fm.height)
        }

        // Render styled characters
        for ((i, char) in editor.getStyledText().withIndex()) {
            val style = char.style
            val charWidth = fm.charWidth(char.content)

            // Draw Selection
            val absSelection = editor.getAbsSelection()
            if (i in absSelection.lowIndex until absSelection.highIndex) {
                g2d.color = defaultSelectionColor
                if (char.content == '\n') {
                    g2d.fillRect(x, y - fm.ascent, editor.bounds.width - x - editor.insets.right, fm.height)
                } else {
                    g2d.fillRect(x, y - fm.ascent, charWidth, fm.height)
                }
            }

            // Draw the cursor
            if (i == editor.caretPos) {
                drawCursor(g2d, x, y, fm)
            }

            // Draw Characters
            g2d.color = style?.fgColor ?: editor.foreground

            when (char.content) {
                '\n' -> {
                    x = editor.insets.left
                    column = 0
                    y += fm.height
                    lineCounter += 1
                }

                else -> {
                    g2d.drawString(char.content.toString(), x, y)
                    x += charWidth
                    column += 1
                }
            }
        }
        if (editor.getStyledText().size == editor.caretPos) {
            drawCursor(g2d, x, y, fm)
        }

        g2d.dispose()
    }

    private fun drawCursor(g2d: Graphics2D, x: Int, y: Int, fm: FontMetrics) {
        caretColor?.let {
            g2d.color = it
            g2d.drawLine(x, y + fm.descent, x, y + fm.descent - fm.height)
        }
    }
}