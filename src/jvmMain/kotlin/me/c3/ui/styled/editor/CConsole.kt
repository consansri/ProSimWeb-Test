package me.c3.ui.styled.editor

import kotlinx.coroutines.*
import me.c3.ui.components.styled.CLabel
import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.editor.CEditorArea
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout

open class CConsole(themeManager: ThemeManager, scaleManager: ScaleManager, maxStackSize: Int = 30, stackQueryMillis: Long = 500) : CPanel(themeManager, scaleManager, primary = true) {

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

    private val topBar = CPanel(themeManager, scaleManager, primary = false)
    private val titleLabel = CLabel(themeManager, scaleManager, title, FontType.TITLE)
    private val textArea = CEditorArea(themeManager, scaleManager, CEditorArea.Location.IN_SCROLLPANE, maxStackSize, stackQueryMillis)

    init {
        textArea.scrollPane.setViewportView(textArea)

        layout = BorderLayout()
        topBar.add(titleLabel)
        this.add(topBar, BorderLayout.NORTH)
        this.add(textArea.scrollPane, BorderLayout.CENTER)
    }

    fun updateContent(content: List<CEditorArea.StyledChar>){
        textArea.replaceStyledContent(content)
    }



}