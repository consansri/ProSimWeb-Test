package prosim.uilib.styled.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prosim.uilib.UIStates
import prosim.uilib.styled.*
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BoxLayout
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CEditorAnalyzer(private val editor: CEditorArea) : CPanel(primary = true, BorderMode.BOWL) {

    private val searchResults = mutableListOf<MatchResult>()
    private var selectedIndex: Int = -1
        set(value) {
            field = value
            if (selectedIndex != -1) {
                resultControls.results.text = "${selectedIndex + 1}/${searchResults.size} results"
            } else {
                resultControls.results.text = "${searchResults.size} results"
            }
            searchResults.getOrNull(selectedIndex)?.let {
                editor.select(it.range)
            }

            editor.repaint()
        }

    val searchScope = CoroutineScope(Dispatchers.Default)
    val modeField = ModeField()
    val searchField = SearchField()
    val replaceField = ReplaceField()
    val resultControls = ResultControls()
    val replaceControls = ReplaceControls()
    val space = CPanel(true, BorderMode.NONE)
    val closeField = CloseField()

    var opened: Boolean = false
    var mode: Mode = Mode.FIND
        set(value) {
            field = value
            updateModeComps()
            modeField.updateIcon()
            revalidate()
            repaint()
        }

    init {
        attachComponents()
    }

    fun getResults(): List<MatchResult> = ArrayList(searchResults)

    fun open(text: String, mode: Mode = Mode.FIND) {
        this.opened = true
        this.mode = mode
        searchField.textField.text = text
        editor.scrollPane.setColumnHeaderView(this)
        when (mode) {
            Mode.FIND -> searchField.textField.requestFocus()
            Mode.REPLACE -> replaceField.textField.requestFocus()
        }
    }

    fun close() {
        editor.scrollPane.setColumnHeaderView(null)
        searchResults.clear()
        selectedIndex = -1
        opened = false
    }

    fun updateResults() {
        if (this.opened) {
            searchField.searchASync()
        }
    }

    private fun attachComponents() {
        layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.gridy = 0
        gbc.weightx = 0.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL

        gbc.gridx = 0
        gbc.gridheight = 2
        add(modeField, gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        gbc.gridheight = 1
        gbc.fill = GridBagConstraints.BOTH
        add(searchField, gbc)

        gbc.gridx = 2
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        add(resultControls, gbc)

        gbc.gridx = 3
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridheight = 2
        gbc.fill = GridBagConstraints.BOTH
        add(space, gbc)

        gbc.gridx = 4
        gbc.weightx = 0.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL
        add(closeField, gbc)

        revalidate()
        repaint()
    }

    private fun updateModeComps() {
        when (mode) {
            Mode.FIND -> {
                remove(replaceControls)
                remove(replaceField)
            }

            Mode.REPLACE -> {
                val gbc = GridBagConstraints()
                gbc.gridx = 1
                gbc.gridy = 1
                gbc.weighty = 1.0
                gbc.weightx = 1.0
                gbc.fill = GridBagConstraints.BOTH
                add(replaceField, gbc)

                gbc.gridx = 2
                gbc.gridy = 1
                gbc.weightx = 0.0
                gbc.weighty = 0.0
                gbc.fill = GridBagConstraints.NONE
                gbc.gridheight = 1

                add(replaceControls, gbc)
            }
        }
    }

    enum class Mode {
        FIND,
        REPLACE
    }

    inner class ModeField : CPanel(primary = true) {
        val modeButton = CIconButton(UIStates.icon.get().folderClosed, mode = CIconButton.Mode.PRIMARY_SMALL)

        init {
            modeButton.addActionListener {
                switchMode()
            }
            add(modeButton)
        }

        fun updateIcon() {
            when (mode) {
                Mode.FIND -> modeButton.svgIcon = UIStates.icon.get().folderClosed
                Mode.REPLACE -> modeButton.svgIcon = UIStates.icon.get().folderOpen
            }
        }

        private fun switchMode() {
            mode = when (mode) {
                Mode.FIND -> Mode.REPLACE
                Mode.REPLACE -> Mode.FIND
            }
        }
    }

    inner class SearchField() : CPanel(borderMode = BorderMode.VERTICAL, primary = true) {

        val textField = CTextArea(FontType.CODE)
        val scrollPane = CScrollPane(true, textField)
        val controls = SearchControls()

        init {
            layout = BorderLayout()
            add(scrollPane, BorderLayout.CENTER)
            add(controls, BorderLayout.EAST)

            // Add DocumentListener to the textField's Document
            textField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    searchASync()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    searchASync()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    // Not needed for plain text components
                }
            })
            textField.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_ESCAPE -> {
                            close()
                        }
                    }
                }
            })
        }

        fun searchASync() {
            val currEditorContent = editor.getText()
            searchScope.launch {
                search(currEditorContent)
            }
        }

        suspend fun search(text: String) {
            searchResults.clear()
            selectedIndex = -1
            val find = textField.text ?: ""

            if (find.isNotEmpty()) {
                val matches = if (controls.regexMode.isActive) {
                    var seq: Sequence<MatchResult>
                    try {
                        seq = find.toRegex().findAll(text)
                        editor.infoLogger?.clearError()
                    } catch (e: Exception) {
                        seq = sequenceOf()
                        editor.infoLogger?.printError("Invalid Regex!")
                    }
                    seq
                } else {
                    editor.infoLogger?.clearError()
                    Regex.escape(find).toRegex().findAll(text)
                }

                searchResults.addAll(matches)
            }

            withContext(Dispatchers.Main) {
                selectedIndex = searchResults.indexOfLast { editor.caret.getIndex() in it.range || editor.caret.getIndex() < it.range.first }
                editor.repaint()
            }
        }

        inner class SearchControls() : CPanel(true, BorderMode.NONE) {
            val regexMode = CToggleButton(".*", CToggleButtonUI.ToggleSwitchType.SMALL, FontType.BASIC).apply {
                addActionListener {
                    isActive = !isActive
                    searchField.searchASync()
                }
            }

            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)

                add(regexMode)
            }
        }
    }

    inner class ReplaceField() : CPanel(borderMode = BorderMode.VERTICAL, primary = true) {
        val textField = CTextArea(FontType.CODE)
        val scrollPane = CScrollPane(true, textField)

        init {
            layout = BorderLayout()
            add(scrollPane, BorderLayout.CENTER)

            textField.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_ESCAPE -> {
                            close()
                        }
                    }
                }
            })
        }
    }

    inner class ResultControls() : CPanel(borderMode = BorderMode.THICKNESS, primary = true) {

        val results = CLabel("0", FontType.BASIC)
        val next = CIconButton(UIStates.icon.get().forwards, CIconButton.Mode.PRIMARY_SMALL).apply {
            addActionListener {
                val nextIndex = searchResults.indexOfFirst { editor.caret.getIndex() < it.range.first }
                if (nextIndex != -1) {
                    selectedIndex = nextIndex
                }
            }
        }
        val previous = CIconButton(UIStates.icon.get().backwards, CIconButton.Mode.PRIMARY_SMALL).apply {
            addActionListener {
                val prevIndex = searchResults.indexOfLast { editor.caret.getIndex() > it.range.last + 1 }
                if (prevIndex != -1) {
                    selectedIndex = prevIndex
                }
            }
        }

        init {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(previous)
            add(next)
            add(results)
        }
    }

    inner class ReplaceControls() : CPanel(true, BorderMode.THICKNESS) {

        val replace = CTextButton("replace", FontType.BASIC)
        val replaceAll = CTextButton("replace all", FontType.BASIC)

        init {
            attachListeners()
            attachComponents()
        }

        fun replaceAll() {
            val currResults = searchResults.toList()
            val replacement = replaceField.textField.text
            var offset = 0
            for (result in currResults) {
                val range = result.range
                editor.replace((range.first + offset)..(range.last + offset), replacement)
                val diff = replacement.length - range.count()
                offset += diff
            }
            //editor.replaceContent(content.replace(searchField.textField.text, replaceField.textField.text))
        }

        fun replace(index: Int): Boolean {
            val rangeToReplace = searchResults.getOrNull(index)?.range ?: return false
            editor.deleteText(rangeToReplace.first, rangeToReplace.last + 1)
            editor.insertText(rangeToReplace.first, replaceField.textField.text)
            return true
        }

        private fun attachListeners() {
            replace.addActionListener {
                replace(selectedIndex)
            }

            replaceAll.addActionListener {
                replaceAll()
            }
        }

        private fun attachComponents() {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(replace)
            add(replaceAll)
        }
    }

    inner class CloseField() : CPanel(primary = true, BorderMode.WEST) {
        private val closeBtn = CIconButton(UIStates.icon.get().close, mode = CIconButton.Mode.PRIMARY_SMALL)

        init {
            closeBtn.addActionListener {
                close()
            }

            add(closeBtn)
        }
    }
}