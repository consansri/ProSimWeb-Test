package me.c3.ui.components.styled

import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.install
import me.c3.ui.UIManager
import me.c3.ui.components.editor.CDocument
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CTextPaneUI
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.SimpleAttributeSet

class CTextPane(themeManager: ThemeManager, scaleManager: ScaleManager) : JTextPane() {

    init {
        this.document = CDocument()
        setUI(CTextPaneUI(themeManager, scaleManager))
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val size = this.preferredSize
        super.setBounds(x, y, size.width.coerceAtLeast(width), height)
    }

    fun setInitialText(text: String) {
        this.document.remove(0, this.document.length)
        val attrs = SimpleAttributeSet()
        this.document.insertString(0, text, attrs)
    }

    fun createScrollPane(themeManager: ThemeManager, scaleManager: ScaleManager): CScrollPane {
        return CScrollPane(themeManager, scaleManager, true, this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        return false
    }

}