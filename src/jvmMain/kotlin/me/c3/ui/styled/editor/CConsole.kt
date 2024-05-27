package me.c3.ui.styled.editor

import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.styled.CLabel
import me.c3.ui.styled.CPanel
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout

open class CConsole(tm: ThemeManager, sm: ScaleManager, icons: ProSimIcons, maxStackSize: Int = 30, stackQueryMillis: Long = 500) : CPanel(tm, sm, primary = true) {

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

    private val topBar = CPanel(tm, sm, primary = false)
    private val titleLabel = CLabel(tm, sm, title, FontType.TITLE)
    private val textArea = CEditorArea(tm, sm, icons, CEditorArea.Location.IN_SCROLLPANE, maxStackSize, stackQueryMillis)

    init {
        textArea.scrollPane.setViewportView(textArea)

        layout = BorderLayout()
        topBar.add(titleLabel)
        this.add(topBar, BorderLayout.NORTH)
        this.add(textArea.scrollPane, BorderLayout.CENTER)
    }

    fun updateContent(content: List<CEditorArea.StyledChar>){
        textArea.replaceAll(content)
    }



}