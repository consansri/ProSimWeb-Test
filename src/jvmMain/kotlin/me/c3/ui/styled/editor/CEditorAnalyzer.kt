package me.c3.ui.styled.editor

import kotlinx.coroutines.*
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.*
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CEditorAnalyzer(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val editor: CEditorArea) : CPanel(themeManager, scaleManager, primary = true, BorderMode.BOWL) {

    private val searchResults = mutableListOf<MatchResult>()

    val modeField = ModeField()
    val searchField = SearchField()
    val replaceField = ReplaceField()
    val controlField = ControlField()
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
            controlField.updateControls()
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

    fun updateResults() {
        if (this.opened) {
            searchField.search()
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
        gbc.gridheight = 2
        gbc.fill = GridBagConstraints.VERTICAL
        add(controlField, gbc)

        gbc.gridx = 3
        add(closeField, gbc)

        revalidate()
        repaint()
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
                    search()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    search()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    // Not needed for plain text components
                }
            })
        }

        fun search() {
            CoroutineScope(Dispatchers.Default).launch {
                searchResults.clear()
                val find = textField.text ?: ""

                if (find.isNotEmpty()) {
                    val currEditorContent = editor.getText()

                    val matches = if (controlField.regexMode.isActive) {
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
                }

                controlField.results.text = searchResults.size.toString()

                withContext(Dispatchers.Main) {
                    editor.repaint()
                }
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

    inner class ControlField() : CPanel(themeManager, scaleManager, borderMode = BorderMode.EAST, primary = true) {
        val regexMode = CToggleButton(themeManager, scaleManager, "regex", CToggleButtonUI.ToggleSwitchType.SMALL, FontType.BASIC).apply {
            addActionListener {
                isActive = !isActive
                searchField.search()
            }
        }
        val results = CLabel(themeManager, scaleManager, "0", FontType.BASIC)

        init {
            layout = GridBagLayout()

            val gbc = GridBagConstraints()
            add(regexMode, gbc)

            gbc.gridx++
            add(results, gbc)
        }

        fun updateControls() {
            when (mode) {
                Mode.FIND -> removeReplaceControls()
                Mode.REPLACE -> attachReplaceControls()
            }
        }

        fun attachReplaceControls() {
            val gbc = GridBagConstraints()

            gbc.gridy = 1

        }

        fun removeReplaceControls() {

        }

    }

    inner class CloseField() : CPanel(themeManager, scaleManager, primary = true) {
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