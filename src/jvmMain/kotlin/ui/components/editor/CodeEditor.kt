package me.c3.ui.components.editor

import kotlinx.coroutines.Job
import me.c3.ui.UIManager
import me.c3.ui.spacing.core.Scaling
import me.c3.ui.theme.core.Theme
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane

class CodeEditor(uiManager: UIManager) : JScrollPane() {

    private val textPane = JTextPane()
    private val lineNumberArea = LineNumberArea()
    private val content: JPanel

    init {
        textPane.isVisible = true
        textPane.isEditable = true
        textPane.size = Dimension(300, 300)
        textPane.background = Color(0,0,0,0)

        content = JPanel()
        content.layout = BoxLayout(content, BoxLayout.X_AXIS)
        content.add(lineNumberArea)
        content.add(textPane)

        //this.setRowHeaderView(lineNumberArea)

        uiManager.themeManager.addThemeChangeListener {
            val scale = uiManager.scaleManager.currentScaling
            updateLAF(it, scale)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            val theme = uiManager.themeManager.currentTheme
            updateLAF(theme, it)
        }

        textPane.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_TAB) {
                    e.consume()
                    textPane.replaceSelection("    ")
                }
            }
        })

        this.add(content)
        this.isVisible = true
        textPane.requestFocusInWindow()
    }

    private fun updateLAF(theme: Theme, scale: Scaling) {
        textPane.font = theme.codeStyle.font.deriveFont(scale.fontScale.codeSize)
        textPane.foreground = theme.textStyle.base
        textPane.caretColor = theme.textStyle.base
        background = theme.globalStyle.bgPrimary
    }

    inner class LineNumberArea : JComponent() {
        private val fontMetrics = getFontMetrics(textPane.font)
        private val lineHeight = fontMetrics.height
        private val startOffset = 3
        private val padding = 5

        override fun paintComponent(g: Graphics) {
            val lineCount = textPane.height / lineHeight
            val startLine = Math.max(0, textPane.viewToModel(Point(0, 0)) / lineHeight)
            val endLine = Math.min(textPane.document.defaultRootElement.elementCount, startLine + lineCount)

            g.color = Color.LIGHT_GRAY
            g.fillRect(0, 0, width, height)

            g.color = Color.BLACK
            for (line in startLine until endLine) {
                val x = width - fontMetrics.stringWidth((line + 1).toString()) - padding
                val y = (line + 1) * lineHeight - startOffset
                g.drawString((line + 1).toString(), x, y)
            }
        }

        override fun getPreferredSize(): Dimension {
            val width = fontMetrics.stringWidth(textPane.text.split("\n").size.toString()) + padding * 2
            return Dimension(width, textPane.height)
        }
    }


}