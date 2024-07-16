package prosim.uilib.styled

import java.awt.BorderLayout
import javax.swing.JComponent

abstract class COverlay : CPanel(isOverlay = true) {
    init {
        layout = BorderLayout()
        isFocusable = false

        isVisible = false
    }

    fun showAtLocation(x: Int, y: Int, width: Int?, height: Int?, parentComponent: JComponent) {
        this.setBounds(x, y, width ?: preferredSize.width, height ?: preferredSize.height)
        parentComponent.add(this)
        parentComponent.revalidate()
        isVisible = true
    }

    fun makeInvisible() {
        isVisible = false
        parent?.remove(this)
        parent?.revalidate()
    }
}