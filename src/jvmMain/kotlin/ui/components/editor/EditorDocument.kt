package me.c3.ui.components.editor

import emulator.kit.assembly.Compiler
import kotlinx.coroutines.*
import me.c3.emulator.kit.hlAndAppendToDoc
import me.c3.ui.UIManager
import me.c3.ui.theme.core.style.CodeStyle
import java.awt.Font
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

class EditorDocument(private val uiManager: UIManager) : DefaultStyledDocument() {

    private var currentlyUpdating = false

    init {
        addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                if (!currentlyUpdating) uiManager.eventManager.triggerEdit(this@EditorDocument)
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (!currentlyUpdating) uiManager.eventManager.triggerEdit(this@EditorDocument)
            }

            override fun changedUpdate(e: DocumentEvent?) {
                if (!currentlyUpdating) uiManager.eventManager.triggerEdit(this@EditorDocument)
            }
        })
    }

    fun hlDocument(codeStyle: CodeStyle, tokens: List<Compiler.Token>) {
        currentlyUpdating = true
        remove(0, length)
        tokens.forEach {
            it.hlAndAppendToDoc(codeStyle, this)
        }
        currentlyUpdating = false
    }

    override fun getFont(attr: AttributeSet?): Font {
        return uiManager.themeManager.currentTheme.codeStyle.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
    }
}