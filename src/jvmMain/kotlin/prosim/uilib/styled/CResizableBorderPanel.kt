package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

class CResizableBorderPanel : CPanel() {

    companion object {
        val layoutConstraints = listOf(BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST, BorderLayout.WEST)
        val minCompSize = 50
        val minCompDim = Dimension(minCompSize, minCompSize)
        val zeroCompDim = Dimension(0,0)
    }

    private val thisLayout = BorderLayout()
    private val resizeHandles = mutableMapOf<String, ResizeHandle>()

    init {
        layout = thisLayout
        createResizeHandles()
    }

    private fun createResizeHandles() {
        layoutConstraints.forEach { direction ->
            val handle = ResizeHandle(direction)
            resizeHandles[direction] = handle
            add(handle, direction)
        }
    }

    /*override fun add(comp: Component, constraints: Any?) {
        if (constraints is String) {
            when (constraints) {
                BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST, BorderLayout.WEST -> {
                    val wrapper = JLayeredPane()
                    wrapper.layout = BorderLayout()
                    wrapper.add(comp, BorderLayout.CENTER)
                    wrapper.add(resizeHandles[constraints], BorderLayout.CENTER)
                    wrapper.setLayer(resizeHandles[constraints], JLayeredPane.DRAG_LAYER)
                    return super.add(wrapper, constraints)
                }
                else -> return super.add(comp, constraints)
            }
        }
        return super.add(comp, constraints)
    }*/

    override fun addImpl(comp: Component?, constraints: Any?, index: Int) {
        if (comp != null && constraints is String) {
            when (constraints) {
                BorderLayout.NORTH, BorderLayout.SOUTH -> {
                    val wrapper = CPanel().apply {
                        layout = BorderLayout()
                    }
                    wrapper.add(comp, BorderLayout.CENTER)
                    wrapper.add(resizeHandles[constraints], if (constraints == BorderLayout.NORTH) BorderLayout.SOUTH else BorderLayout.NORTH)
                    super.addImpl(wrapper, constraints, index)
                }

                BorderLayout.EAST, BorderLayout.WEST -> {
                    val wrapper = CPanel().apply {
                        layout = BorderLayout()
                    }
                    wrapper.add(comp, BorderLayout.CENTER)
                    wrapper.add(resizeHandles[constraints], if (constraints == BorderLayout.EAST) BorderLayout.WEST else BorderLayout.EAST)
                    super.addImpl(wrapper, constraints, index)
                }

                else -> super.addImpl(comp, constraints, index)
            }
            comp.minimumSize = minCompDim
        } else {
            super.addImpl(comp, constraints, index)
        }
    }

    private inner class ResizeHandle(private val direction: String) : CPanel() {

        private val resizeAdapter = ResizeAdapter()

        override val customBG: Color
            get() = UIStates.theme.get().COLOR_BORDER

        init {
            cursor = Cursor.getPredefinedCursor(
                when (direction) {
                    BorderLayout.NORTH, BorderLayout.SOUTH -> Cursor.N_RESIZE_CURSOR
                    else -> Cursor.E_RESIZE_CURSOR
                }
            )

            addMouseListener(resizeAdapter)
            addMouseMotionListener(resizeAdapter)
        }

        override fun getPreferredSize(): Dimension {
            return when (direction) {
                BorderLayout.NORTH, BorderLayout.SOUTH -> Dimension(0, UIStates.scale.get().SIZE_DIVIDER_THICKNESS)
                else -> Dimension(UIStates.scale.get().SIZE_DIVIDER_THICKNESS, 0)
            }
        }

        private inner class ResizeAdapter : MouseAdapter(), MouseMotionListener {

            private var startPoint: Point? = null
            private var startSize: Dimension? = null
            private val resizeBounds: IntRange get() {
                val limitingComponentSize: Int = when (direction) {
                    BorderLayout.NORTH -> {
                        val southComp = thisLayout.getLayoutComponent(BorderLayout.SOUTH)?.size ?: zeroCompDim
                        this@CResizableBorderPanel.height - minCompDim.height - southComp.height
                    }
                    BorderLayout.SOUTH -> {
                        val northComp = thisLayout.getLayoutComponent(BorderLayout.NORTH)?.size ?: zeroCompDim
                        this@CResizableBorderPanel.height - minCompDim.height - northComp.height
                    }
                    BorderLayout.EAST -> {
                        val westComp = thisLayout.getLayoutComponent(BorderLayout.WEST)?.size ?: zeroCompDim
                        this@CResizableBorderPanel.width - minCompDim.width - westComp.width
                    }

                    BorderLayout.WEST -> {
                        val eastComp = thisLayout.getLayoutComponent(BorderLayout.EAST)?.size ?: zeroCompDim
                        this@CResizableBorderPanel.width - minCompDim.width - eastComp.width
                    }

                    else -> Int.MAX_VALUE
                }

                val range = UIStates.scale.get().SIZE_DIVIDER_THICKNESS..limitingComponentSize
                return range
            }


            override fun mousePressed(e: MouseEvent) {
                startPoint = e.locationOnScreen
                startSize = when (direction) {
                    BorderLayout.NORTH -> thisLayout.getLayoutComponent(BorderLayout.NORTH)?.size
                    BorderLayout.SOUTH -> thisLayout.getLayoutComponent(BorderLayout.SOUTH)?.size
                    BorderLayout.EAST -> thisLayout.getLayoutComponent(BorderLayout.EAST)?.size
                    BorderLayout.WEST -> thisLayout.getLayoutComponent(BorderLayout.WEST)?.size
                    else -> null
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                startSize = null
                startPoint = null
            }

            override fun mouseDragged(e: MouseEvent) {
                val resizeBounds = resizeBounds
                val currentPoint = e.locationOnScreen
                val currStartPoint = startPoint
                val currStartSize = startSize
                if (currStartPoint != null && currStartSize != null) {
                    val diff = when (direction) {
                        BorderLayout.NORTH, BorderLayout.SOUTH -> currentPoint.y - currStartPoint.y
                        else -> currentPoint.x - currStartPoint.x
                    }

                    val newSize = when (direction) {
                        BorderLayout.NORTH -> Dimension(currStartSize.width, (currStartSize.height + diff).coerceIn(resizeBounds))
                        BorderLayout.SOUTH -> Dimension(currStartSize.width, (currStartSize.height - diff).coerceIn(resizeBounds))
                        BorderLayout.EAST -> Dimension((currStartSize.width - diff).coerceIn(resizeBounds), currStartSize.height)
                        BorderLayout.WEST -> Dimension((currStartSize.width + diff).coerceIn(resizeBounds), currStartSize.height)
                        else -> currStartSize
                    }

                    val component = thisLayout.getLayoutComponent(direction)
                    if (component != null) {
                        component.preferredSize = newSize
                        this@CResizableBorderPanel.revalidate()
                        this@CResizableBorderPanel.repaint()
                    }
                }
            }

            override fun mouseMoved(e: MouseEvent?) {}

        }

    }


}