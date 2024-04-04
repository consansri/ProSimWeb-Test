package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import me.c3.ui.components.borders.DirectionalBorder
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.theme.core.style.CodeStyle
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import javax.swing.*

class EditPanel(uiManager: UIManager, val fileName: String) : CPanel(uiManager, true) {
    private var compileJob: Job? = null

    // Content
    private val document = EditorDocument(uiManager)

    // Elements
    private val textPane = CTextPane(uiManager, document)
    private val lineNumbers = LineNumbers(uiManager, textPane)
    private val viewport = JViewport()
    private val cScrollPane = textPane.createScrollPane(uiManager)

    // Layout
    private val constraints = GridBagConstraints()

    init {
        // Setup Layout
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH

        // Add Listeners
        uiManager.themeManager.addThemeChangeListener {
            viewport.background = it.globalStyle.bgPrimary
            textPane.background = it.globalStyle.bgPrimary
            textPane.caretColor = it.codeStyle.codeStyle(Compiler.CodeStyle.BASE0)
            textPane.foreground = it.codeStyle.codeStyle(Compiler.CodeStyle.BASE0)
            textPane.font = it.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            viewport.font = it.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            textPane.font = uiManager.themeManager.currentTheme.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            viewport.font = uiManager.themeManager.currentTheme.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
        }

        uiManager.eventManager.addEditListener {
            triggerCompile(uiManager, build = false)
        }

        // Apply Defaults
        border = BorderFactory.createEmptyBorder()

        // for lineNumbers
        lineNumbers.border = DirectionalBorder(uiManager, east = true)

        // for textPane
        textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
        textPane.font = uiManager.currTheme().codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
        textPane.isEditable = true
        textPane.caretColor = uiManager.currTheme().codeStyle.codeStyle(Compiler.CodeStyle.BASE0)
        textPane.foreground = uiManager.currTheme().codeStyle.codeStyle(Compiler.CodeStyle.BASE0)
        textPane.document = EditorDocument(uiManager) // Use PlainDocument for better performance

        // Link ViewPort with LineNumbers to ScrollPane
        viewport.view = lineNumbers
        viewport.extentSize = lineNumbers.preferredScrollableViewportSize
        cScrollPane.rowHeader = viewport

        // Add Components
        add(cScrollPane)
    }

    fun triggerCompile(uiManager: UIManager, build: Boolean = false, immediate: Boolean = false) {
        compileJob?.cancel()

        compileJob = CoroutineScope(Dispatchers.Default).launch {
            if (!immediate) {
                delay(1500)
            }
            val content = textPane.document.getText(0, textPane.document.length)

            val compResult = uiManager.currArch().compile(content, fileName, emptyList(), build)
            uiManager.eventManager.compileFinished()
            val codeStyle = uiManager.currTheme().codeStyle
            hlContent(codeStyle, compResult.tokens)
        }
    }

    private fun hlContent(codeStyle: CodeStyle, tokens: List<Compiler.Token>) {
        val selStart = textPane.selectionStart
        val selEnd = textPane.selectionEnd
        textPane.isEditable = false
        (textPane.document as EditorDocument).hlDocument(codeStyle, tokens)
        textPane.isEditable = true
        textPane.selectionStart = selStart
        textPane.selectionEnd = selEnd
        nativeLog("Highlighting!")
    }

    class LineNumbers(uiManager: UIManager, textPane: JTextPane) : JList<String>(LineNumberListModel(textPane)) {

        init {
            // UI Listeners
            uiManager.themeManager.addThemeChangeListener {
                cellRenderer = LineNumberListRenderer(it.textStyle.baseSecondary)
                font = it.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
                fixedCellWidth = getFontMetrics(font).charWidth('0') * 5
                fixedCellHeight = textPane.getFontMetrics(textPane.font).height
            }

            uiManager.eventManager.addEditListener {
                this.updateUI()
                (this.model as LineNumberListModel).update()
            }

            // Apply Defaults
            cellRenderer = LineNumberListRenderer(uiManager.themeManager.currentTheme.textStyle.baseSecondary)
            fixedCellWidth = this.getFontMetrics(this.font).charWidth('0') * 5
            fixedCellHeight = textPane.getFontMetrics(textPane.font).height
        }

        class LineNumberListModel(private val textPane: JTextPane) : AbstractListModel<String>() {

            override fun getSize(): Int {
                return textPane.text.split("\n").size
            }

            override fun getElementAt(index: Int): String {
                return (index + 1).toString()
            }

            fun update() {
                fireContentsChanged(this, 0, size)
            }
        }

        class LineNumberListRenderer(private val lineNumberColor: Color) : DefaultListCellRenderer() {
            init {
                horizontalAlignment = RIGHT
            }

            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                label.foreground = lineNumberColor
                return label
            }
        }
    }

}

