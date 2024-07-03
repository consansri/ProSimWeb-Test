package prosim.uilib.styled.core.layouts

import emulator.kit.nativeLog
import org.jetbrains.skia.Canvas

class BorderLayout : Layout() {
    override val width: Int get() = calcWidth()
    override val height: Int get() = calcHeight()

    fun calcHeight(): Int {
        val top = components[BorderAttr.NORTH]?.preferredHeight ?: 0
        val center = components[BorderAttr.CENTER]?.preferredHeight ?: 0
        val bottom = components[BorderAttr.SOUTH]?.preferredHeight ?: 0
        return top + center + bottom
    }

    fun calcWidth(): Int {
        val left = components[BorderAttr.WEST]?.preferredWidth ?: 0
        val center = components[BorderAttr.CENTER]?.preferredWidth ?: 0
        val right = components[BorderAttr.EAST]?.preferredWidth ?: 0
        return left + center + right
    }

    override fun renderContent(canvas: Canvas) {
        nativeLog("Render Border Layout!")
        val northSpace = components[BorderAttr.NORTH]?.preferredHeight ?: 0
        val westSpace = components[BorderAttr.WEST]?.preferredWidth ?: 0
        val southSpace = components[BorderAttr.SOUTH]?.preferredHeight ?: 0
        val eastSpace = components[BorderAttr.EAST]?.preferredWidth ?: 0
        val centerHeight = components[BorderAttr.CENTER]?.preferredHeight ?: 0
        val centerWidth = components[BorderAttr.CENTER]?.preferredWidth ?: 0

        for ((pos, child) in components) {
            canvas.save()
            if (pos !is BorderAttr) continue
            when (pos) {
                BorderAttr.CENTER -> canvas.translate(westSpace.toFloat(), northSpace.toFloat())
                BorderAttr.EAST -> canvas.translate(westSpace.toFloat() + centerWidth.toFloat(), northSpace.toFloat())
                BorderAttr.NORTH -> canvas.translate(0f, 0f)
                BorderAttr.SOUTH -> canvas.translate(0f, northSpace.toFloat() + centerHeight.toFloat())
                BorderAttr.WEST -> canvas.translate(0f, northSpace.toFloat())
            }
            child.render(canvas)
            canvas.restore()
        }
    }
}