package me.c3.ui.components.editor

import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.event.ChangeListener
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.Style
import javax.swing.text.StyleContext

class CodeEditor: JScrollPane() {

    private val document = DefaultStyledDocument()
    private val textPane = JTextPane(document)

    init {
        textPane.isVisible = true
        textPane.isEditable = true
        textPane.text = "Hallo ich bin Text!"
        this.add(textPane)
        this.isVisible = true
    }


}