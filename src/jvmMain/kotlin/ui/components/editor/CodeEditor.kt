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
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.SimpleAttributeSet
import kotlin.time.measureTime

class CodeEditor(private val uiManager: UIManager) : CAdvancedTabPane(uiManager.themeManager, uiManager.scaleManager, uiManager.icons, true, true, emptyMessage = "Open File through the tree!") {

    private val panels = mutableListOf<EditPanel>()
    private val fileEditEvents = mutableListOf<(EditorFile) -> Unit>()

    init {
        attachWorkspaceListener()
        selectCurrentTab(null)
    }

    private fun attachWorkspaceListener() {
        uiManager.addWSChangedListener { ws ->
            SwingUtilities.invokeLater {
                val bufferedTabs = ArrayList(tabs)
                for (tab in bufferedTabs) {
                    val content = tab.content
                    if (content !is EditPanel) {
                        removeTab(tab)
                        continue
                    }

                    if (ws.getAllFiles().firstOrNull { it.path == content.editorFile.file.path && it.name == content.editorFile.file.name } == null) {
                        removeTab(tab)
                        panels.remove(content)
                        continue
                    }
                }
            }
        }
    }

    fun openFile(file: File) {
        if (searchByName(file.name) != null) return

        if (!file.exists()) {
            file.createNewFile()
        }

        val editorFile = EditorFile(file)

        val editPanel = EditPanel(editorFile, uiManager)
        panels.add(editPanel)
        addTab(CLabel(uiManager.themeManager, uiManager.scaleManager, file.getName()), editPanel) { e, tab ->
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
        return tabs.firstOrNull { fileName == (it.content as? EditPanel)?.editorFile?.file?.name }?.content as? EditPanel?
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

    class EditPanel(val editorFile: EditorFile, uiManager: UIManager) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = true) {

        private var compileJob: Job? = null
        private var currentlyUpdating = false

        val documentListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                lineNumbers.update(uiManager)
                if (!currentlyUpdating) {
                    val document = e?.document
                    document?.let {
                        editorFile.edit(e.document.getText(0, document.length))
                    }
                    triggerCompile(uiManager)
                }

            }

            override fun removeUpdate(e: DocumentEvent?) {
                lineNumbers.update(uiManager)
                if (!currentlyUpdating) {
                    val document = e?.document
                    document?.let {
                        editorFile.edit(e.document.getText(0, document.length))
                    }
                    triggerCompile(uiManager)
                }
            }

            override fun changedUpdate(e: DocumentEvent?) {
                lineNumbers.update(uiManager)
                if (!currentlyUpdating) {
                    val document = e?.document
                    document?.let {
                        editorFile.edit(e.document.getText(0, document.length))
                    }
                    triggerCompile(uiManager)
                }
            }
        }


        // Elements
        private val textPane = CTextPane(uiManager.themeManager, uiManager.scaleManager)
        private val lineNumbers = LineNumbers(uiManager, textPane).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    e?.let {
                        val index = locationToIndex(it.point)
                        nativeLog("LineNumbers clicked on $index!")
                        if (index != -1 && SwingUtilities.isLeftMouseButton(it)) {
                            uiManager.currArch().exeUntilLine(index, editorFile.getName())
                            uiManager.eventManager.triggerExeEvent()
                        }
                    }
                }
            })
        }
        private val viewport = JViewport()
        private val cScrollPane = textPane.createScrollPane(uiManager.themeManager, uiManager.scaleManager)

        init {
            textPane.setInitialText(editorFile.contentAsString())

            uiManager.themeManager.addThemeChangeListener {
                setEditorDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setEditorDefaults(uiManager)
            }

            attachComponents()
            attachDocument()
            setEditorDefaults(uiManager)
            attachPCListener(uiManager)
            attachSelectionListener(uiManager)
            triggerCompile(uiManager, immediate = true)
        }

        fun undo() {
            editorFile.undo()
            currentlyUpdating = true
            textPane.document.remove(0, textPane.document.length)
            val attrs = SimpleAttributeSet()
            textPane.document.insertString(0, editorFile.contentAsString(), attrs)
            currentlyUpdating = false
        }

        fun redo() {
            editorFile.redo()
            currentlyUpdating = true
            textPane.document.remove(0, textPane.document.length)
            val attrs = SimpleAttributeSet()
            textPane.document.insertString(0, editorFile.contentAsString(), attrs)
            currentlyUpdating = false
        }

        fun triggerCompile(uiManager: UIManager, build: Boolean = false, immediate: Boolean = false) {
            compileJob?.cancel()

            val delay = if (immediate) 0L else 3000L

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
            editorFile.store()
            val compResult = uiManager.currArch().compile(editorFile.toCompilerFile(), uiManager.currWS().getCompilerFiles(editorFile.file), build)
            val codeStyle = uiManager.currTheme().codeLaF
            if (compResult.tokens.joinToString("") { it.content } == textPane.document.getText(0, textPane.document.length)) {
                hlContent(uiManager, codeStyle, compResult)
            }

            withContext(Dispatchers.Default) {
                uiManager.eventManager.triggerCompileFinished(compResult.success)
            }

            lineNumbers.update(uiManager)
            return compResult
        }

        private fun attachDocument() {
            textPane.document = editorFile.getRawDocument()
            textPane.document.addDocumentListener(documentListener)
        }

        private suspend fun hlContent(uiManager: UIManager, codeStyle: CodeLaF, result: Compiler.CompileResult) {
            SwingUtilities.invokeLater {
                val selStart = textPane.selectionStart
                val selEnd = textPane.selectionEnd
                currentlyUpdating = true
                (textPane.document as? CDocument)?.hlDocument(codeStyle, result.tokens)
                currentlyUpdating = false
                textPane.selectionStart = selStart
                textPane.selectionEnd = selEnd
            }
        }

        private fun attachPCListener(uiManager: UIManager) {
            uiManager.eventManager.addCompileListener {
                updateLineNumber(uiManager)
            }
            uiManager.eventManager.addExeEventListener {
                updateLineNumber(uiManager)
            }
        }

        private fun attachSelectionListener(uiManager: UIManager) {
            textPane.addCaretListener {
                printSelectionInfoTobBar(uiManager)
            }
            printSelectionInfoTobBar(uiManager)
        }

        private fun printSelectionInfoTobBar(uiManager: UIManager) {
            CoroutineScope(Dispatchers.Default).launch {
                val selectionLength = textPane.selectionEnd - textPane.selectionStart
                val textUntilStart = textPane.document.getText(0, textPane.selectionStart)
                val lineList = textUntilStart.split("\n")
                val lineOfStart = lineList.size
                val charOfStart = lineList.lastOrNull()?.length
                uiManager.bBar.editorInfo.text = "${lineOfStart}:${charOfStart}${if (selectionLength > 0) " ($selectionLength)" else ""}"
            }
        }

        private fun updateLineNumber(uiManager: UIManager) {
            val lineLoc = uiManager.currArch().getCompiler().getAssemblyMap().lineAddressMap.get(uiManager.currArch().getRegContainer().pc.get().toHex().toRawString())
            lineNumbers.currentPCLineNumber = if (lineLoc?.fileName == editorFile.getName()) {
                lineLoc.lineID
            } else {
                -1
            }
        }

        private fun attachComponents() {
            // Link ViewPort with LineNumbers to ScrollPane
            viewport.view = lineNumbers
            viewport.extentSize = lineNumbers.preferredScrollableViewportSize
            cScrollPane.rowHeader = viewport

            add(cScrollPane)
        }

        private fun setEditorDefaults(uiManager: UIManager) {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder()
            lineNumbers.border = DirectionalBorder(uiManager.themeManager, uiManager.scaleManager, east = true)
            textPane.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
            textPane.isEditable = true
            viewport.background = uiManager.currTheme().globalLaF.bgPrimary
            lineNumbers.update(uiManager)
        }

        class LineNumbers(uiManager: UIManager, private val textPane: CTextPane) : JList<String>(LineNumberListModel(textPane)) {
            var currentPCLineNumber = -1
                set(value) {
                    field = value
                    (this.model as? LineNumberListModel)?.currentPCLineNumber = value
                    repaint()
                }

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
                attachClickListener(uiManager)
            }

            fun update(uiManager: UIManager) {
                SwingUtilities.invokeLater {
                    setDefaults(uiManager)
                    (this.model as LineNumberListModel).update()
                    this.updateUI()
                }
            }

            private fun attachClickListener(uiManager: UIManager) {

            }

            private fun setDefaults(uiManager: UIManager) {
                val currTheme = uiManager.currTheme()
                (this.model as? LineNumberListModel)?.pcIdenticator = currTheme.codeLaF.pcIdenticator
                this.font = textPane.font
                this.background = currTheme.globalLaF.bgPrimary
                this.cellRenderer = LineNumberListRenderer(currTheme.textLaF.baseSecondary, currTheme.codeLaF.getColor(Compiler.CodeStyle.GREENPC), textPane.font, currTheme.globalLaF.bgPrimary, currTheme.codeLaF.pcIdenticator)
                this.fixedCellWidth = getFontMetrics(textPane.font).charWidth('0') * 5
                this.fixedCellHeight = getFontMetrics(textPane.font).height
            }

            class LineNumberListModel(private val textPane: CTextPane) : AbstractListModel<String>() {

                var pcIdenticator: String = ""
                var currentPCLineNumber = -1

                override fun getSize(): Int {
                    return textPane.text.split("\n").size
                }

                override fun getElementAt(index: Int): String {
                    return if (currentPCLineNumber == index) {
                        "$pcIdenticator ${index + 1}"
                    } else {
                        "${index + 1}"
                    }
                }

                fun update() {
                    fireContentsChanged(this, 0, size)
                }
            }

            class LineNumberListRenderer(private val lineNumberColor: Color, private val pcLineColor: Color, private val font: Font, private val bg: Color, private val pcIdenticator: String) : DefaultListCellRenderer() {
                init {
                    horizontalAlignment = RIGHT
                }

                override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    val isCurrPCLine = value.toString().contains(pcIdenticator)
                    label.font = font
                    label.border = BorderFactory.createEmptyBorder()
                    label.foreground = if (isCurrPCLine) pcLineColor else lineNumberColor
                    label.background = bg
                    return label
                }
            }
        }

    }
}