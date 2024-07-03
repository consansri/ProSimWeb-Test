package prosim.uilib.styled.editor2

import prosim.uilib.styled.core.SMainPanel
import prosim.uilib.styled.core.layouts.BorderAttr
import prosim.uilib.styled.core.layouts.BorderLayout
import java.awt.Graphics
import javax.swing.JFrame

class Editor(frame: JFrame) : SMainPanel(frame) {
    override val preferredWidth: Int = 0
    override val preferredHeight: Int = 0

    val editorArea = REditorArea()
    val rRowHeader = RRowHeader(editorArea)
    init {
        layout = BorderLayout()
        addComponent(editorArea, BorderAttr.CENTER)
        addComponent(rRowHeader, BorderAttr.WEST)
        repaint()
    }

    override fun paint(g: Graphics?) {
        skikoLayer.needRedraw()
    }
}