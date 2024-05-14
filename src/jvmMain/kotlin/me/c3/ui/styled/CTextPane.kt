package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.*

class CTextPane(themeManager: ThemeManager, scaleManager: ScaleManager) : JTextPane() {

    init {
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