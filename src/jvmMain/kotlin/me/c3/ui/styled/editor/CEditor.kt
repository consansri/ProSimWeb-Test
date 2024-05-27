package me.c3.ui.styled.editor

import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.styled.CPanel
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout

open class CEditor(tm: ThemeManager, sm: ScaleManager, icons: ProSimIcons, maxStackSize: Int = 30, stackQueryMillis: Long = 500) : CPanel(tm, sm, primary = true) {

    val textArea = CEditorArea(tm, sm, icons, CEditorArea.Location.IN_SCROLLPANE, maxStackSize, stackQueryMillis)

    var fileInterface: FileInterface? = null
        set(value) {
            field = value
            textArea.fileInterface = value
        }

    var highlighter: Highlighter? = null
        set(value) {
            field = value
            textArea.highlighter = value
        }

    var infoLogger: InfoLogger? = null
        set(value) {
            field = value
            textArea.infoLogger = value
        }

    var shortCuts: ShortCuts? = null
        set(value) {
            field = value
            textArea.shortCuts = value
        }

    init {
        textArea.scrollPane.setRowHeaderView(textArea.lineNumbers)
        textArea.scrollPane.setViewportView(textArea)
        attachLineClickListener()

        layout = BorderLayout()
        this.add(textArea.scrollPane, BorderLayout.CENTER)
    }

    protected fun setStyledContent(styledChars: List<CEditorArea.StyledChar>){
        textArea.replaceAll(styledChars)
    }

    protected fun mark(vararg content: CEditorLineNumbers.LineContent) {
        textArea.lineNumbers.mark(*content)
    }

    fun undo(){
        textArea.undo()
    }

    fun redo(){
        textArea.redo()
    }

    protected open fun onLineClicked(lineNumber: Int) {}

    private fun attachLineClickListener() {
        textArea.lineNumbers.addMouseListener(object : CEditorLineNumbers.LineClickListener(textArea.lineNumbers) {
            override fun lineClick(lineNumber: Int) {
                onLineClicked(lineNumber)
            }
        })
    }


}