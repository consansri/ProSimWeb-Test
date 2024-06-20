package me.c3.uilib.styled.editor

import me.c3.uilib.styled.CPanel
import java.awt.BorderLayout

open class CEditor(maxStackSize: Int = 30, stackQueryMillis: Long = 500) : CPanel(primary = true) {

    val textArea = CEditorArea(CEditorArea.Location.IN_SCROLLPANE, maxStackSize, stackQueryMillis)

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

    protected fun setStyledContent(styledChars: List<CEditorArea.StyledChar>) {
        textArea.replaceAll(styledChars)
    }

    protected fun mark(vararg content: CEditorLineNumbers.LineContent) {
        textArea.lineNumbers.mark(*content)
    }

    fun undo() {
        textArea.undo()
    }

    fun redo() {
        textArea.redo()
    }

    fun invokeHL(){
        textArea.debounceHighlighting()
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