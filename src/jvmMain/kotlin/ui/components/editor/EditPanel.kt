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
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.time.measureTime

class EditPanel(val file: FileManager.CodeFile, uiManager: UIManager) : CPanel(uiManager, true) {
    private var compileJob: Job? = null

    // Content
    private var currentlyUpdating = false

    // Elements
    private val textPane = CTextPane(uiManager)
    private val lineNumbers = LineNumbers(uiManager, textPane)
    private val viewport = JViewport()
    private val cScrollPane = textPane.createScrollPane(uiManager)

    init {
        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager)
        }

        attachComponents()
        attachDocument(uiManager)
        setDefaults(uiManager)
    }

    private fun attachDocument(uiManager: UIManager) {
        textPane.document = file.getRawDocument(uiManager)
        textPane.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                val document = e?.document
                document?.let {
                    file.edit(e.document.getText(0, document.length))
                }

                nativeLog("EditPanel: CurrentFont -> ${font.name}, ${textPane.font.name}, ${viewport.font.name}")

                if (!currentlyUpdating) triggerCompile(uiManager)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                val document = e?.document
                document?.let {
                    file.edit(e.document.getText(0, document.length))
                }
                if (!currentlyUpdating) triggerCompile(uiManager)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                lineNumbers.update()
                val document = e?.document
                document?.let {
                    file.edit(e.document.getText(0, document.length))
                }
                if (!currentlyUpdating) triggerCompile(uiManager)
            }
        })
    }

    private fun attachComponents() {
        // Link ViewPort with LineNumbers to ScrollPane
        viewport.view = lineNumbers
        viewport.extentSize = lineNumbers.preferredScrollableViewportSize
        cScrollPane.rowHeader = viewport

        add(cScrollPane)
    }

    fun triggerCompile(uiManager: UIManager, build: Boolean = false, immediate: Boolean = false) {
        compileJob?.cancel()

        val delay = if (immediate) 0L else 1500L

        compileJob = Coroutines.setTimeout(delay) {
            SwingUtilities.invokeLater {
                val measuredTime = measureTime {
                    file.store()
                    val compResult = uiManager.currArch().compile(file.toCompilerFile(), uiManager.currWS().getCompilerFiles(file.file), build)
                    nativeLog("EditPanel: triggerCompile() start")
                    val codeStyle = uiManager.currTheme().codeLaF
                    hlContent(codeStyle, compResult.tokens)
                }
                nativeLog("EditPanel: triggerCompile() took ${measuredTime.inWholeNanoseconds} ns")
                uiManager.eventManager.triggerCompileFinished()
            }
        }
    }

    fun setDefaults(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder()
            lineNumbers.border = DirectionalBorder(uiManager, east = true)
            viewport.font = uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
            textPane.isEditable = true
            viewport.background = uiManager.currTheme().globalLaF.bgPrimary
            textPane.font = uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
            font = uiManager.themeManager.currentTheme.codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
        }
    }

    private fun hlContent(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
        SwingUtilities.invokeLater {
            val selStart = textPane.selectionStart
            val selEnd = textPane.selectionEnd
            val bufferedSize = textPane.size
            textPane.isEditable = false
            currentlyUpdating = true
            (textPane.document as? EditorDocument)?.hlDocument(codeStyle, tokens)
            currentlyUpdating = false
            textPane.isEditable = true
            //textPane.size = bufferedSize
            textPane.selectionStart = selStart
            textPane.selectionEnd = selEnd
        }
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

