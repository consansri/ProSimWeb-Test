package me.c3.uilib.styled.editor

import me.c3.ui.States
import me.c3.uilib.styled.CIconButton
import me.c3.uilib.styled.CIconToggle
import me.c3.uilib.styled.CLabel
import me.c3.uilib.styled.CPanel
import me.c3.uilib.styled.params.BorderMode
import me.c3.uilib.styled.params.FontType
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

open class CConsole(maxStackSize: Int = 30, stackQueryMillis: Long = 500) : CPanel(primary = true) {

    var title: String = "Console"
        set(value) {
            field = value
            titleLabel.text = value
        }

    var isEditable: Boolean = false
        set(value) {
            field = value
            textArea.isEditable = value
        }

    var scrollToBottom: Boolean = true
        set(value) {
            field = value
            textArea.caret.moveCaretTo(textArea.getStyledText().size)
        }

    private val titleLabel = CLabel(title, FontType.TITLE)
    private val scrollDown = CIconToggle(States.icon.get().autoscroll, scrollToBottom, CIconButton.Mode.SECONDARY_SMALL) {
        scrollToBottom = it
    }
    private val topBar = CPanel(primary = false, BorderMode.INSET).apply {
        this.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        this.add(titleLabel, gbc)

        gbc.weightx = 1.0
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        this.add(CPanel(), gbc)

        gbc.weightx = 0.0
        gbc.gridx = 2
        gbc.fill = GridBagConstraints.NONE
        this.add(scrollDown, gbc)
    }

    private val textArea = CEditorArea(CEditorArea.Location.IN_SCROLLPANE, maxStackSize, stackQueryMillis)

    init {
        textArea.scrollPane.setViewportView(textArea)

        layout = BorderLayout()
        this.add(topBar, BorderLayout.NORTH)
        this.add(textArea.scrollPane, BorderLayout.CENTER)
    }

    fun updateContent(content: List<CEditorArea.StyledChar>) {
        textArea.replaceAll(content)
        if (scrollToBottom) textArea.caret.moveCaretTo(textArea.getStyledText().size)
    }

}