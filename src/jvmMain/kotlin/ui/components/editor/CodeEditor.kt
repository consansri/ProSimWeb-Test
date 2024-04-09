package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.Job
import me.c3.emulator.kit.install
import me.c3.ui.UIManager
import me.c3.ui.components.styled.*
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import kotlin.time.measureTime

class CodeEditor(private val uiManager: UIManager) : CTabbedPane(uiManager, true) {

    val fileManager = FileManager()
    private val panels = mutableListOf<EditPanel>()

    init {
        filesChangedReaction()
        currFileEditReaction()
    }

    private fun filesChangedReaction() {
        fileManager.addOpenFileChangeListener { fm ->
            this.removeAll()
            fm.openFiles.forEach {
                this.addTextFileTab(it)
            }
        }
    }

    private fun addTextFileTab(file: FileManager.CodeFile) {
        if (!file.file.exists()) {
            file.file.createNewFile()
        }

        val editPanel = EditPanel(file, uiManager)
        addTab(null, editPanel)
        val lastIndex = tabCount - 1
        panels.add(editPanel)
        setTabComponentAt(lastIndex, CClosableTab(uiManager, file.getName()) {
            fileManager.closeFile(file)
            try {
                this.removeTabAt(lastIndex)
            }catch (e: IndexOutOfBoundsException){
                nativeWarn("CodeEditor: Throws Input out of Bounds Exception!")
            }

            this.panels.remove(editPanel)
        })
    }

    private fun currFileEditReaction() {
        fileManager.addCurrFileEditEventListener { fm ->

        }
    }

    fun getControls(): EditorControls = EditorControls(uiManager, this)

    class EditPanel(private val file: FileManager.CodeFile, private val uiManager: UIManager) : CPanel(uiManager, primary = true) {

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
            setEditorDefaults(uiManager)
            textPane.setInitialText(file.contentAsString())
        }

        private fun attachDocument(uiManager: UIManager) {
            textPane.document = file.getRawDocument()
            textPane.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    lineNumbers.update()
                    val document = e?.document
                    document?.let {
                        file.edit(e.document.getText(0, document.length))
                    }
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

            val delay = if (immediate) 0L else 2500L

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

        private fun setEditorDefaults(uiManager: UIManager) {
            SwingUtilities.invokeLater {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = BorderFactory.createEmptyBorder()
                lineNumbers.border = DirectionalBorder(uiManager, east = true)
                viewport.font = uiManager.themeManager.currentTheme.codeLaF.getFont().deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
                textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
                textPane.isEditable = true
                viewport.background = uiManager.currTheme().globalLaF.bgPrimary
                font = uiManager.themeManager.currentTheme.codeLaF.getFont().deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
                font.install(textPane, uiManager.currScale().fontScale.codeSize)
            }
        }

        private fun hlContent(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
            SwingUtilities.invokeLater {
                val selStart = textPane.selectionStart
                val selEnd = textPane.selectionEnd
                val bufferedSize = textPane.size
                textPane.isEditable = false
                currentlyUpdating = true
                (textPane.document as? CDocument)?.hlDocument(codeStyle, tokens)
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

                uiManager.scaleManager.addScaleChangeEvent {
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
                val loadedFont = uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.codeSize)
                this.font = loadedFont
                this.background = uiManager.currTheme().globalLaF.bgPrimary
                cellRenderer = LineNumberListRenderer(uiManager.currTheme().textLaF.baseSecondary, loadedFont, uiManager.currTheme().globalLaF.bgPrimary)
                fixedCellWidth = getFontMetrics(loadedFont).charWidth('0') * 5
                fixedCellHeight = textPane.getFontMetrics(loadedFont).height
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

            class LineNumberListRenderer(private val lineNumberColor: Color, private val font: Font, private val bg: Color) : DefaultListCellRenderer() {
                init {
                    horizontalAlignment = RIGHT
                }

                override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    label.font = font
                    label.foreground = lineNumberColor
                    label.background = bg
                    return label
                }
            }
        }

    }


}