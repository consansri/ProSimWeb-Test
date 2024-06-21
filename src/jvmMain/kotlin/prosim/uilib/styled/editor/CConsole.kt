package prosim.uilib.styled.editor

import prosim.uilib.UIStates
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CIconToggle
import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
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
    private val scrollDown = CIconToggle(UIStates.icon.get().autoscroll, scrollToBottom, CIconButton.Mode.SECONDARY_SMALL) {
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