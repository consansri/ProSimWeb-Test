package me.c3.ui.styled.editor

import emulator.kit.nativeLog
import kotlinx.coroutines.*
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.*
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.time.measureTime

class CEditorAnalyzer(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val editor: CEditorArea) : CPanel(themeManager, scaleManager, primary = true, BorderMode.BOWL) {

    private val searchResults = mutableListOf<MatchResult>()
    private var selectedIndex: Int = -1
        set(value) {
            field = value
            if (selectedIndex != -1) {
                resultControls.results.text = "${selectedIndex + 1}/${searchResults.size}"
            } else {
                resultControls.results.text = "${searchResults.size}"
            }
            editor.repaint()
        }

    val searchScope = CoroutineScope(Dispatchers.Default)
    val modeField = ModeField()
    val searchField = SearchField()
    val replaceField = ReplaceField()
    val resultControls = ResultControls()
    val replaceControls = ReplaceControls()
    val closeField = CloseField()

    var opened: Boolean = false
    var mode: Mode = Mode.FIND
        set(value) {
            field = value
            when (mode) {
                Mode.FIND -> {
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
                }
            }
            updateReplaceControls()
            modeField.updateIcon()
            revalidate()
            repaint()
        }

    init {
        attachComponents()
    }

    fun isSelected(result: MatchResult): Boolean {
        return searchResults.indexOf(result) == selectedIndex
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
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(resultControls, gbc)

        gbc.gridx = 3
        gbc.weightx = 0.0
        gbc.gridheight = 2
        gbc.fill = GridBagConstraints.VERTICAL
        add(closeField, gbc)

        revalidate()
        repaint()
    }

    private fun updateReplaceControls() {
        when (mode) {
            Mode.FIND -> remove(replaceControls)
            Mode.REPLACE -> {
                val gbc = GridBagConstraints()

                gbc.gridx = 2
                gbc.gridy = 1
                gbc.weightx = 1.0
                gbc.weighty = 1.0
                gbc.fill = GridBagConstraints.HORIZONTAL
                gbc.gridheight = 1

                add(replaceControls, gbc)
            }
        }
    }

    enum class Mode {
        FIND,
        REPLACE
    }

    inner class ModeField : CPanel(themeManager, scaleManager, primary = true) {
        val modeButton = CIconButton(themeManager, scaleManager, editor.icons.folderClosed, mode = CIconButton.Mode.PRIMARY_SMALL)

        init {
            modeButton.addActionListener {
                switchMode()
            }
            add(modeButton)
        }

        fun updateIcon() {
            when (mode) {
                Mode.FIND -> modeButton.svgIcon = editor.icons.folderClosed
                Mode.REPLACE -> modeButton.svgIcon = editor.icons.folderOpen
            }
        }

        private fun switchMode() {
            mode = when (mode) {
                Mode.FIND -> Mode.REPLACE
                Mode.REPLACE -> Mode.FIND
            }
        }
    }

    inner class SearchField() : CPanel(themeManager, scaleManager, borderMode = BorderMode.VERTICAL, primary = true) {

        val textField = CTextArea(themeManager, scaleManager, FontType.BASIC)

        init {
            layout = BorderLayout()
            add(textField, BorderLayout.CENTER)

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
        }

        fun searchASync() {
            searchScope.launch {
                search()
            }
        }

        suspend fun search() {
            searchResults.clear()
            selectedIndex = -1
            val find = textField.text ?: ""

            if (find.isNotEmpty()) {
                val currEditorContent = editor.getText()

                val matches = if (resultControls.regexMode.isActive) {
                    var seq: Sequence<MatchResult>
                    try {
                        seq = find.toRegex().findAll(currEditorContent)
                        editor.infoLogger?.clearError()
                    } catch (e: Exception) {
                        seq = sequenceOf()
                        editor.infoLogger?.printError("Invalid Regex!")
                    }
                    seq
                } else {
                    editor.infoLogger?.clearError()
                    Regex.escape(find).toRegex().findAll(currEditorContent)
                }

                searchResults.addAll(matches)
                selectedIndex = if (searchResults.isNotEmpty()) 0 else -1
            }

            withContext(Dispatchers.Main){
                editor.repaint()
            }
        }

    }

    inner class ReplaceField() : CPanel(themeManager, scaleManager, borderMode = BorderMode.VERTICAL, primary = true) {
        val textField = CTextArea(themeManager, scaleManager, FontType.BASIC)

        init {
            layout = BorderLayout()
            add(textField, BorderLayout.CENTER)
        }
    }

    inner class ResultControls() : CPanel(themeManager, scaleManager, borderMode = BorderMode.NONE, primary = true) {
        val regexMode = CToggleButton(themeManager, scaleManager, "regex", CToggleButtonUI.ToggleSwitchType.SMALL, FontType.BASIC).apply {
            addActionListener {
                isActive = !isActive
                searchField.searchASync()
            }
        }
        val results = CLabel(themeManager, scaleManager, "0", FontType.BASIC)
        val next = CIconButton(themeManager, scaleManager, editor.icons.forwards, CIconButton.Mode.PRIMARY_SMALL).apply {
            addActionListener {
                if (selectedIndex < searchResults.size - 1) {
                    selectedIndex++
                }
            }
        }
        val previous = CIconButton(themeManager, scaleManager, editor.icons.backwards, CIconButton.Mode.PRIMARY_SMALL).apply {
            addActionListener {
                if (selectedIndex > 0) {
                    selectedIndex--
                }
            }
        }

        init {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(regexMode)
            add(previous)
            add(next)
            add(results)
        }
    }

    inner class ReplaceControls() : CPanel(themeManager, scaleManager, true, BorderMode.NONE) {

        val replace = CTextButton(themeManager, scaleManager, "replace", FontType.BASIC)
        val replaceAll = CTextButton(themeManager, scaleManager, "replace all", FontType.BASIC)

        init {
            attachListeners()
            attachComponents()
        }

        fun replaceAll() {
            val content = editor.getText()
            editor.replaceContent(content.replace(searchField.textField.text, replaceField.textField.text))
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

    inner class CloseField() : CPanel(themeManager, scaleManager, primary = true, BorderMode.WEST) {
        private val closeBtn = CIconButton(themeManager, scaleManager, editor.icons.close, mode = CIconButton.Mode.PRIMARY_SMALL)

        init {
            closeBtn.addActionListener {
                editor.scrollPane.setColumnHeaderView(null)
                opened = false
            }

            add(closeBtn)
        }
    }
}