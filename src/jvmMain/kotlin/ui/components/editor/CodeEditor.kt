package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import emulator.kit.nativeLog
import kotlinx.coroutines.*
import me.c3.ui.UIManager
import me.c3.ui.components.styled.*
import me.c3.ui.styled.CAdvancedTabPane
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.core.style.CodeLaF
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.time.measureTime

class CodeEditor(private val uiManager: UIManager) : CAdvancedTabPane(uiManager, true, true) {

    private val panels = mutableListOf<EditPanel>()
    private val fileEditEvents = mutableListOf<(EditorFile) -> Unit>()

    init {
        attachWorkspaceListener()
    }

    private fun attachWorkspaceListener() {
        uiManager.addWSChangedListener {
            removeAllTabs()
            panels.clear()
        }
    }

    fun openFile(file: File) {
        if (searchByName(file.name) != null) return

        if (!file.exists()) {
            file.createNewFile()
        }

        val editorFile = EditorFile(file) {
            triggerFileEdit(it)
        }

        val editPanel = EditPanel(editorFile, uiManager)
        panels.add(editPanel)
        addTab(CLabel(uiManager, file.getName()), editPanel) { e, tab ->
            (tab.content as? EditPanel)?.file?.store()
            when (e) {
                ClosableTab.Event.LOSTFOCUS -> {}
                ClosableTab.Event.CLOSE -> {
                    panels.remove(editPanel)
                }
            }
        }
    }

    fun getControls(): EditorControls = EditorControls(uiManager, this)

    fun searchByName(fileName: String): EditPanel? {
        return tabs.firstOrNull { fileName == (it.content as? EditPanel)?.file?.file?.name }?.content as? EditPanel?
    }

    fun compileCurrent(build: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            getCurrentEditPanel()?.compile(uiManager, build)
        }
    }

    fun getCurrentEditPanel(): EditPanel? {
        return getCurrent()?.content as? EditPanel
    }

    fun addFileEditEvent(event: (EditorFile) -> Unit) {
        fileEditEvents.add(event)
    }

    private fun triggerFileEdit(editorFile: EditorFile) {
        val eventBuffer = ArrayList(fileEditEvents)
        eventBuffer.forEach {
            it(editorFile)
        }
    }

    class EditPanel(val file: EditorFile, uiManager: UIManager) : CPanel(uiManager, primary = true) {

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

        fun triggerCompile(uiManager: UIManager, build: Boolean = false, immediate: Boolean = false) {
            compileJob?.cancel()

            val delay = if (immediate) 0L else 1000L

            compileJob = Coroutines.setTimeout(delay) {
                CoroutineScope(Dispatchers.Default).launch {
                    val measuredTime = measureTime {
                        compile(uiManager, build)
                    }
                    nativeLog("EditPanel: triggerCompile() took ${measuredTime.inWholeNanoseconds} ns")
                }
            }
        }

        suspend fun compile(uiManager: UIManager, build: Boolean): Compiler.CompileResult {
            file.store()
            val compResult = uiManager.currArch().compile(file.toCompilerFile(), uiManager.currWS().getCompilerFiles(file.file), build)
            val codeStyle = uiManager.currTheme().codeLaF
            if (compResult.tokens.joinToString("") { it.content } == textPane.styledDocument.getText(0, textPane.styledDocument.length)) {
                hlContent(uiManager, codeStyle, compResult)
            }
            return compResult
        }

        private fun attachDocument(uiManager: UIManager) {
            textPane.document = file.getRawDocument()
            textPane.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    lineNumbers.update(uiManager)
                    val document = e?.document
                    document?.let {
                        file.edit(e.document.getText(0, document.length))
                    }
                    if (!currentlyUpdating) triggerCompile(uiManager)
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    lineNumbers.update(uiManager)
                    val document = e?.document
                    document?.let {
                        file.edit(e.document.getText(0, document.length))
                    }
                    if (!currentlyUpdating) triggerCompile(uiManager)
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    lineNumbers.update(uiManager)
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

        private fun setEditorDefaults(uiManager: UIManager) {
            setDefaults(uiManager)
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder()
            lineNumbers.border = DirectionalBorder(uiManager, east = true)
            textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
            textPane.isEditable = true
            viewport.background = uiManager.currTheme().globalLaF.bgPrimary
            lineNumbers.update(uiManager)
        }

        private suspend fun hlContent(uiManager: UIManager, codeStyle: CodeLaF, result: Compiler.CompileResult) {
            withContext(Dispatchers.Main) {
                val selStart = textPane.selectionStart
                val selEnd = textPane.selectionEnd
                val bufferedSize = textPane.size
                textPane.isEditable = false
                currentlyUpdating = true
                (textPane.document as? CDocument)?.hlDocument(codeStyle, result.tokens)
                currentlyUpdating = false
                textPane.isEditable = true
                //textPane.size = bufferedSize
                textPane.selectionStart = selStart
                textPane.selectionEnd = selEnd
                uiManager.eventManager.triggerCompileFinished(result.success)
            }
        }

        class LineNumbers(uiManager: UIManager, private val textPane: CTextPane) : JList<String>(LineNumberListModel(textPane)) {

            init {
                // UI Listeners
                uiManager.themeManager.addThemeChangeListener {
                    setDefaults(uiManager)
                }

                uiManager.scaleManager.addScaleChangeEvent {
                    setDefaults(uiManager)
                }

                // Apply Defaults
                setDefaults(uiManager)
            }

            fun update(uiManager: UIManager) {
                setDefaults(uiManager)
                (this.model as LineNumberListModel).update()
                this.updateUI()
            }

            private fun setDefaults(uiManager: UIManager) {
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