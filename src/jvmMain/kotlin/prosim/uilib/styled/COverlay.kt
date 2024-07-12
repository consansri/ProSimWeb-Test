package prosim.uilib.styled

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent

class COverlay : CPanel(isOverlay = true) {
    private val content: CTextPane = CTextPane().apply {
        isEditable = false
    }

    init {
        layout = BorderLayout()
        add(content, BorderLayout.CENTER)

        isVisible = false
    }

    fun showAtLocation(x: Int, y: Int, width: Int, parentComponent: JComponent) {
        setBounds(x, y, width, preferredSize.height)
        parentComponent.add(this)
        parentComponent.revalidate()
        isVisible = true
    }

    fun makeInvisible() {
        isVisible = false
        parent?.remove(this)
        parent?.revalidate()
    }

    fun setContent(text: String, isHtml: Boolean = false) {
        content.contentType = if (isHtml) "text/html" else "text/plain"
        content.text = text
        preferredSize = Dimension(preferredSize.width, content.preferredSize.height)
    }

}