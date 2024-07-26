package prosim.uilib.styled

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Point
import javax.swing.JComponent

open class COverlay : CPanel(isOverlay = true) {
    init {
        layout = BorderLayout()
        isFocusable = false

        isVisible = false
    }

    fun showAtLocation(point: Point, childComponent: Component, parentComponent: JComponent) {
        removeAll()
        add(childComponent, BorderLayout.CENTER)
        val size = childComponent.size
        setBounds(point.x - size.width / 2 - insets.left, point.y - size.height / 2 - insets.top, size.width + insets.left + insets.right, size.height + insets.top + insets.bottom)

        parentComponent.add(this)
        parentComponent.revalidate()

        isVisible = true
    }

    fun move(point: Point){
        setBounds(point.x - size.width / 2, point.y - size.height / 2, size.width, size.height)
        parent.revalidate()
    }

    fun showAtLocation(x: Int, y: Int, width: Int?, height: Int?, parentComponent: JComponent) {
        val actualWidth = if (width != null) {
            width + insets.left + insets.right
        } else preferredSize.width
        val actualHeight = if (height != null) {
            height + insets.top + insets.bottom
        } else preferredSize.height

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