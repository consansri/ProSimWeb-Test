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
        val actualWidth = if(width != null){
            width + insets.left + insets.right
        }else preferredSize.width
        val actualHeight = if(height != null){
            height + insets.top + insets.bottom
        }else preferredSize.height

        this.setBounds(x, y, actualWidth, actualHeight)
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