package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.States
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.resources.icons.ProSimIcons
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

open class CFrame() : JFrame() {
    val titleBar = TitleBar()
    val content = CPanel( primary = false)

    private var cornerRadius: Int = States.scale.get().borderScale.cornerRadius

    private var posX = 0
    private var posY = 0
    private var resizeMode: ResizeMode? = null

    private var mouseOffsetX = 0
    private var mouseOffsetY = 0

    init {
        size = Dimension(1920, 1080)
        isUndecorated = true
        layout = BorderLayout()

        addBaseComponents()
        this.setupUI()

        isVisible = true
    }

    private fun addBaseComponents() {
        add(titleBar, BorderLayout.NORTH)
        add(content, BorderLayout.CENTER)

        /*addMouseListener(DragListener())
        addMouseMotionListener(DragListener())*/

        addMouseListener(EdgeResizeListener())
        addMouseMotionListener(EdgeResizeMotionListener())
    }

    fun setFrameTitle(newtitle: String) {
        title = newtitle
        titleBar.titleLabel.text = newtitle
    }

    private fun setupUI() {
        SwingUtilities.invokeLater {
            States.scale.addEvent { _ ->
                setDefaults()
            }

            States.theme.addEvent { _ ->
                setDefaults()
            }

            setDefaults()
        }
    }

    private fun setDefaults() {
        rootPane.border = BorderFactory.createEmptyBorder()
        content.border = BorderFactory.createEmptyBorder()
        cornerRadius = States.scale.get().borderScale.cornerRadius
        background = States.theme.get().globalLaF.bgSecondary

        val icon = States.icon.get().appLogo.derive(64, 64)
        icon.colorFilter = States.theme.get().icon.colorFilter
        iconImage = icon.image
    }

    fun addTitleBar(comp: Component) {
        titleBar.titleContent.add(comp)
    }

    fun addContent(comp: Component?): Component {
        return content.add(comp)
    }

    fun addContent(comp: Component, constraints: Any?) {
        content.add(comp, constraints)
    }

    fun addContent(comp: Component?, index: Int): Component {
        return content.add(comp, index)
    }

    fun addContent(comp: Component?, constraints: Any?, index: Int) {
        content.add(comp, constraints, index)
    }

    inner class TitleBar : CPanel( primary = false, BorderMode.SOUTH) {

        val logoButton = CIconButton( States.icon.get().appLogo, CIconButton.Mode.GRADIENT_NORMAL)
        val titleLabel = CLabel( title, FontType.BASIC)
        val minimizeButton = CIconButton( States.icon.get().decrease, CIconButton.Mode.SECONDARY_SMALL)
        val maximizeButton = CIconButton( States.icon.get().increase, CIconButton.Mode.SECONDARY_SMALL)
        val closeButton = CIconButton( States.icon.get().close, CIconButton.Mode.SECONDARY_SMALL)
        val titleContent = CPanel( primary = false)

        init {
            attachContent()

            // Add mouse listener for dragging the window
            addMouseListener(DragListener())
            addMouseMotionListener(DragMotionListener())

            addButtonListeners()

            applyDefaultLook()
        }

        private fun attachContent() {
            layout = GridBagLayout()
            val gbc = GridBagConstraints()

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(logoButton, gbc)

            gbc.gridx = 1
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(titleLabel, gbc)

            gbc.gridx = 2
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(titleContent, gbc)

            gbc.gridx = 3
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(minimizeButton, gbc)

            gbc.gridx = 4
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(maximizeButton, gbc)

            gbc.gridx = 5
            gbc.gridy = 0
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            add(closeButton, gbc)

            titleContent.layout = BoxLayout(titleContent, BoxLayout.X_AXIS)
        }

        private fun addButtonListeners() {
            // Add minimize button
            minimizeButton.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    this@CFrame.state = ICONIFIED
                }
            })

            // Add full-screen button
            maximizeButton.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    this@CFrame.extendedState = if (this@CFrame.extendedState != MAXIMIZED_BOTH)
                        MAXIMIZED_BOTH
                    else
                        NORMAL
                }
            })

            // Add close button
            closeButton.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    exitProcess(0)
                }
            })
        }

        private fun applyDefaultLook() {
            titleLabel.horizontalAlignment = SwingConstants.LEFT
            logoButton.isDeactivated = true

            States.theme.addEvent { _ ->
                applyThemeDefaults()
            }
            States.scale.addEvent { _ ->
                applyThemeDefaults()
            }

            applyThemeDefaults()
        }

        private fun applyThemeDefaults() {
            States.icon.get().appLogo.colorFilter = FlatSVGIcon.ColorFilter {
               States.theme.get().textLaF.base
            }

            logoButton.svgIcon = States.icon.get().appLogo
        }
    }

    inner class EdgeResizeListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            resizeMode = isEdge(e.point)
            if (resizeMode != null) {
                posX = e.x
                posY = e.y
                if (resizeMode == ResizeMode.LEFT || resizeMode == ResizeMode.LEFTANDBOTTOM) {
                    mouseOffsetX = e.x
                    mouseOffsetY = e.y
                }
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            resizeMode = null
        }
    }

    inner class EdgeResizeMotionListener : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            if (resizeMode != null) {
                val deltaX = e.x - posX
                val deltaY = e.y - posY

                size = when (resizeMode) {
                    ResizeMode.RIGHT -> {
                        Dimension(width + deltaX, height)
                    }

                    ResizeMode.BOTTOM -> {
                        Dimension(width, height + deltaY)
                    }

                    ResizeMode.LEFT -> {
                        val x = e.x - mouseOffsetX
                        setLocation(location.x + x, location.y)
                        Dimension(width - x, height)
                    }

                    ResizeMode.LEFTANDBOTTOM -> {
                        val x = e.x - mouseOffsetX
                        setLocation(location.x + x, location.y)
                        Dimension(width - x, height + deltaY)
                    }

                    ResizeMode.RIGHTANDBOTTOM -> {
                        Dimension(width + deltaX, height + deltaY)
                    }

                    null -> {
                        Dimension(width, height)
                    }
                }

                posX = e.x
                posY = e.y
            }
        }

        override fun mouseMoved(e: MouseEvent) {
            cursor = isEdge(e.point)?.cursor ?: Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
        }
    }

    private fun isEdge(point: Point): ResizeMode? {
        val left = point.x <= States.scale.get().borderScale.insets
        val right = point.x >= width - States.scale.get().borderScale.insets
        val bottom = point.y >= height - States.scale.get().borderScale.insets

        return when {
            left && bottom -> ResizeMode.LEFTANDBOTTOM
            right && bottom -> ResizeMode.RIGHTANDBOTTOM
            bottom -> ResizeMode.BOTTOM
            left -> ResizeMode.LEFT
            right -> ResizeMode.RIGHT
            else -> null
        }
    }

    inner class DragListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            mouseOffsetX = e.x
            mouseOffsetY = e.y
        }
    }

    inner class DragMotionListener : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            val x = e.x - mouseOffsetX
            val y = e.y - mouseOffsetY
            setLocation(location.x + x, location.y + y)
        }
    }

    enum class ResizeMode(val cursor: Cursor) {
        RIGHT(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)),
        BOTTOM(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)),
        LEFT(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)),
        LEFTANDBOTTOM(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)),
        RIGHTANDBOTTOM(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR))
    }


}