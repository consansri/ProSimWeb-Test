package me.c3.ui.styled.editor

import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.BorderLayout

open class CEditor(themeManager: ThemeManager, scaleManager: ScaleManager) : CPanel(themeManager, scaleManager, primary = true) {

    val textArea = CEditorArea(themeManager, scaleManager)

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

    init {
        textArea.scrollPane.setRowHeaderView(textArea.lineNumbers)
        textArea.scrollPane.setViewportView(textArea)
        attachLineClickListener()

        layout = BorderLayout()
        this.add(textArea.scrollPane, BorderLayout.CENTER)
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