package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlinx.coroutines.*
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
        setTabComponentAt(lastIndex, CClosableTab(lastIndex, uiManager, file.getName()) {
            fileManager.closeFile(file)
            val index = indexOfComponent(editPanel)
            if (index != -1) this.removeTabAt(index)
            this.panels.remove(editPanel)
        })
        selectedComponent = editPanel
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
            textPane.setInitialText(file.contentAsString())

            uiManager.themeManager.addThemeChangeListener {
                setEditorDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setEditorDefaults(uiManager)
            }

            attachComponents()
            attachDocument(uiManager)
            setEditorDefaults(uiManager)
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

            val delay = if (immediate) 0L else 1000L

            compileJob = Coroutines.setTimeout(delay) {
                CoroutineScope(Dispatchers.Default).launch {
                    val measuredTime = measureTime {
                        file.store()
                        val compResult = uiManager.currArch().compile(file.toCompilerFile(), uiManager.currWS().getCompilerFiles(file.file), build)
                        nativeLog("EditPanel: triggerCompile() start")
                        val codeStyle = uiManager.currTheme().codeLaF
                        if (compResult.tokens.joinToString("") { it.content } == textPane.styledDocument.getText(0, textPane.styledDocument.length)) {
                            hlContent(codeStyle, compResult.tokens)
                            nativeLog("Content is the same!")
                        }
                    }
                    nativeLog("EditPanel: triggerCompile() took ${measuredTime.inWholeNanoseconds} ns")
                    uiManager.eventManager.triggerCompileFinished()
                }
            }
        }

        private fun setEditorDefaults(uiManager: UIManager) {
            setDefaults(uiManager)
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder()
            lineNumbers.border = DirectionalBorder(uiManager, east = true)
            textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
            textPane.isEditable = true
            viewport.background = uiManager.currTheme().globalLaF.bgPrimary
            lineNumbers.update()
        }

        private suspend fun hlContent(codeStyle: CodeLaF, tokens: List<Compiler.Token>) {
            withContext(Dispatchers.Main){
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

        class LineNumbers(private val uiManager: UIManager, private val textPane: CTextPane) : JList<String>(LineNumberListModel(textPane)) {

            init {
                // UI Listeners
                uiManager.themeManager.addThemeChangeListener {
                    setDefaults()
                }

                uiManager.scaleManager.addScaleChangeEvent {
                    setDefaults()
                }

                // Apply Defaults
                setDefaults()
            }

            fun update() {
                setDefaults()
                (this.model as LineNumberListModel).update()
                this.updateUI()
            }

            private fun setDefaults() {
                this.font = textPane.font
                this.background = uiManager.currTheme().globalLaF.bgPrimary
                this.cellRenderer = LineNumberListRenderer(uiManager.currTheme().textLaF.baseSecondary, textPane.font, uiManager.currTheme().globalLaF.bgPrimary)
                this.fixedCellWidth = getFontMetrics(textPane.font).charWidth('0') * 5
                this.fixedCellHeight = getFontMetrics(textPane.font).height
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