package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.*

class CTextPane(tm: ThemeManager, sm: ScaleManager) : JTextPane() {

    init {
        setUI(CTextPaneUI(tm, sm))
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

    fun createScrollPane(tm: ThemeManager, sm: ScaleManager): CScrollPane {
        return CScrollPane(tm, sm, true, this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        return false
    }
}