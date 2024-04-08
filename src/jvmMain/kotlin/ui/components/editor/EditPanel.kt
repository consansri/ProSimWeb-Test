package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Color
import java.awt.Component
import java.awt.GridBagConstraints
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class EditPanel(val file: FileManager.CodeFile, val codeEditor: CodeEditor, uiManager: UIManager) : CPanel(uiManager, true) {
    private var compileJob: Job? = null

    // Content
    private var currentlyUpdating = false

    // Elements
    private val textPane = CTextPane(uiManager)
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

        textPane.document = file.getRawDocument(uiManager)
        textPane.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                if (!currentlyUpdating) triggerCompile(uiManager)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                if (!currentlyUpdating) triggerCompile(uiManager)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                if (!currentlyUpdating) triggerCompile(uiManager)
            }
        })

        // Add Listeners
        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager)
        }

        // Apply Defaults
        border = BorderFactory.createEmptyBorder()

        // for lineNumbers
        lineNumbers.border = DirectionalBorder(uiManager, east = true)

        // Link ViewPort with LineNumbers to ScrollPane
        viewport.view = lineNumbers
        viewport.extentSize = lineNumbers.preferredScrollableViewportSize
        cScrollPane.rowHeader = viewport

        // Add Components
        add(cScrollPane)
        setDefaults(uiManager)
    }

    fun triggerCompile(uiManager: UIManager, build: Boolean = false, immediate: Boolean = false) {
        compileJob?.cancel()

        compileJob = CoroutineScope(Dispatchers.IO).launch {
            if (!immediate) {
                delay(1500)
            }
            saveFile()
            val compResult = uiManager.currArch().compile(file.toCompilerFile(), uiManager.currWS().getCompilerFiles(file.file), build)
            uiManager.eventManager.triggerCompileFinished()
            val codeStyle = uiManager.currTheme().codeLaF
            hlContent(codeStyle, compResult.tokens)
        }
    }

    private fun setDefaults(uiManager: UIManager) {
        viewport.font = uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
        //textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
        textPane.isEditable = true
        viewport.background = uiManager.currTheme().globalLaF.bgPrimary
        //textPane.font = uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
    }

    private fun hlContent(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
        val selStart = textPane.selectionStart
        val selEnd = textPane.selectionEnd
        val bufferedSize = textPane.size
        textPane.isEditable = false
        currentlyUpdating = true
        (textPane.document as EditorDocument).hlDocument(codeStyle, tokens)
        currentlyUpdating = false
        textPane.isEditable = true
        //textPane.size = bufferedSize
        textPane.selectionStart = selStart
        textPane.selectionEnd = selEnd
        nativeLog("Highlighting!")
    }

    private fun saveFile() {
        file.file.writeText(this.textPane.document.getText(0, textPane.document.length))
    }

    class LineNumbers(uiManager: UIManager, textPane: CTextPane) : JList<String>(LineNumberListModel(textPane)) {

        init {
            // UI Listeners
            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager, textPane)
            }

            // Apply Defaults
            setDefaults(uiManager, textPane)
        }

        fun update() {
            this.updateUI()
            (this.model as LineNumberListModel).update()
        }

        private fun setDefaults(uiManager: UIManager, textPane: JTextPane) {
            cellRenderer = LineNumberListRenderer(uiManager.currTheme().textLaF.baseSecondary)
            font = uiManager.currTheme().codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            fixedCellWidth = getFontMetrics(font).charWidth('0') * 5
            fixedCellHeight = textPane.getFontMetrics(textPane.font).height
            this.updateUI()
            (this.model as LineNumberListModel).update()
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

