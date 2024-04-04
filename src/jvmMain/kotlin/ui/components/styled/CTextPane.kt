package me.c3.ui.components.styled

import me.c3.ui.UIManager
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.StyledDocument

class CTextPane(uiManager: UIManager, doc: StyledDocument) : JTextPane(doc) {

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val size = this.preferredSize
        super.setBounds(x, y, size.width.coerceAtLeast(width), height)
    }

    fun createScrollPane(uiManager: UIManager): CScrollPane {
        return CScrollPane(uiManager, true, this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    }

}