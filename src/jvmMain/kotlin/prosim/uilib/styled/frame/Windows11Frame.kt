package prosim.uilib.styled.frame

import prosim.uilib.styled.CPanel
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.*
import kotlin.system.exitProcess

open class Windows11Frame(val content: JComponent = CPanel()) : JFrame() {
    private val titleBar = Windows11TitleBar()
    private val contentWrapper = ModernContentWrapper(content)

    private var resizeMode: ResizeMode? = null
    private var mouseOffset = Point()

    init {
        isUndecorated = true
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        add(titleBar, BorderLayout.NORTH)
        add(contentWrapper, BorderLayout.CENTER)

        setupWindowBehavior()
        applyModernLook()
    }

    private fun setupWindowBehavior() {
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                shape = getRoundedShape(width, height)
            }
        })

        addMouseListener(ResizeListener())
        addMouseMotionListener(ResizeMotionListener())
    }

    private fun applyModernLook() {
        background = Color(243, 243, 243)
        contentWrapper.border = BorderFactory.createEmptyBorder(0, 1, 1, 1)
    }

    private fun getRoundedShape(width: Int, height: Int): Shape {
        val radius = 8
        return RoundRectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), radius.toDouble(), radius.toDouble())
    }

    inner class Windows11TitleBar : JPanel() {
        private val titleLabel = JLabel("Modern Application")
        private val minimizeButton = createButton("\uE921")
        private val maximizeButton = createButton("\uE922")
        private val closeButton = createButton("\uE8BB")

        init {
            layout = BorderLayout()
            background = Color(243, 243, 243)
            preferredSize = Dimension(getWidth(), 32)

            val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 12, 0)).apply {
                isOpaque = false
                add(titleLabel)
            }

            val centerPanel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0)).apply {
                isOpaque = false
                add(createButtonPanel())
            }

            add(leftPanel, BorderLayout.WEST)
            add(centerPanel, BorderLayout.CENTER)

            addMouseListener(DragListener())
            addMouseMotionListener(DragMotionListener())
        }

        private fun createButtonPanel(): JPanel {
            return JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
                isOpaque = false
                add(minimizeButton)
                add(maximizeButton)
                add(closeButton)
            }
        }

        private fun createButton(text: String): JButton {
            return JButton(text).apply {
                font = Font("Segoe Fluent Icons", Font.PLAIN, 10)
                preferredSize = Dimension(46, 32)
                isFocusPainted = false
                isContentAreaFilled = false
                foreground = Color(0, 0, 0)
                border = BorderFactory.createEmptyBorder()

                addMouseListener(object : MouseAdapter() {
                    override fun mouseEntered(e: MouseEvent) {
                        background = when (text) {
                            "\uE8BB" -> Color(232, 17, 35)
                            else -> Color(229, 229, 229)
                        }
                        isContentAreaFilled = true
                    }

                    override fun mouseExited(e: MouseEvent) {
                        isContentAreaFilled = false
                    }
                })

                addActionListener {
                    when (text) {
                        "\uE921" -> state = Frame.ICONIFIED
                        "\uE922" -> {
                            extendedState = if (extendedState == Frame.MAXIMIZED_BOTH) Frame.NORMAL else Frame.MAXIMIZED_BOTH
                            this.text = if (extendedState == Frame.MAXIMIZED_BOTH) "\uE923" else "\uE922"
                        }
                        "\uE8BB" -> exitProcess(0)
                    }
                }
            }
        }
    }
    inner class ModernContentWrapper(content: JComponent) : JPanel() {
        init {
            layout = BorderLayout()
            isOpaque = false
            add(content, BorderLayout.CENTER)
        }
    }

    inner class ResizeListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            resizeMode = getResizeMode(e.point)
            mouseOffset.setLocation(e.point)
        }

        override fun mouseReleased(e: MouseEvent) {
            resizeMode = null
        }
    }

    inner class ResizeMotionListener : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            if (resizeMode != null) {
                val newBounds = calculateNewBounds(e.point)
                bounds = newBounds
                revalidate()
                repaint()
            }
        }

        override fun mouseMoved(e: MouseEvent) {
            cursor = getResizeMode(e.point)?.cursor ?: Cursor.getDefaultCursor()
        }
    }

    inner class DragListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            mouseOffset.setLocation(e.point)
        }
    }

    inner class DragMotionListener : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            setLocation(location.x + e.x - mouseOffset.x, location.y + e.y - mouseOffset.y)
        }
    }

    private fun getResizeMode(point: Point): ResizeMode? {
        val borderSize = 5
        val isLeft = point.x <= borderSize
        val isRight = point.x >= width - borderSize
        val isTop = point.y <= borderSize
        val isBottom = point.y >= height - borderSize

        return when {
            isLeft && isTop -> ResizeMode.NORTHWEST
            isRight && isTop -> ResizeMode.NORTHEAST
            isLeft && isBottom -> ResizeMode.SOUTHWEST
            isRight && isBottom -> ResizeMode.SOUTHEAST
            isLeft -> ResizeMode.WEST
            isRight -> ResizeMode.EAST
            isTop -> ResizeMode.NORTH
            isBottom -> ResizeMode.SOUTH
            else -> null
        }
    }

    private fun calculateNewBounds(point: Point): Rectangle {
        val dx = point.x - mouseOffset.x
        val dy = point.y - mouseOffset.y
        var newX = x
        var newY = y
        var newWidth = width
        var newHeight = height

        when (resizeMode) {
            ResizeMode.NORTH -> {
                newY += dy
                newHeight -= dy
            }
            ResizeMode.SOUTH -> newHeight += dy
            ResizeMode.WEST -> {
                newX += dx
                newWidth -= dx
            }
            ResizeMode.EAST -> newWidth += dx
            ResizeMode.NORTHWEST -> {
                newX += dx
                newY += dy
                newWidth -= dx
                newHeight -= dy
            }
            ResizeMode.NORTHEAST -> {
                newY += dy
                newWidth += dx
                newHeight -= dy
            }
            ResizeMode.SOUTHWEST -> {
                newX += dx
                newWidth -= dx
                newHeight += dy
            }
            ResizeMode.SOUTHEAST -> {
                newWidth += dx
                newHeight += dy
            }
            null -> {}
        }

        return Rectangle(newX, newY, newWidth, newHeight)
    }

    enum class ResizeMode(val cursor: Cursor) {
        NORTH(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)),
        SOUTH(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)),
        EAST(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)),
        WEST(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)),
        NORTHEAST(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)),
        NORTHWEST(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)),
        SOUTHEAST(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)),
        SOUTHWEST(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR))
    }

    // ... (rest of the code remains the same)
}