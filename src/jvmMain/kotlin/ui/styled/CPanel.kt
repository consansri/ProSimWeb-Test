package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CPanelUI
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.round

open class CPanel(private val uiManager: UIManager, primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false) : JPanel(), UIAdapter {

    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            setDefaults(uiManager)
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            setDefaults(uiManager)
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            setDefaults(uiManager)
        }

    init {
        this.setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {


            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {
        this.setUI(CPanelUI(roundedCorners))

        background = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary
        border = when (borderMode) {
            BorderMode.INSET -> uiManager.currScale().borderScale.getInsetBorder()
            BorderMode.NORTH -> DirectionalBorder(uiManager, north = true)
            BorderMode.SOUTH -> DirectionalBorder(uiManager, south = true)
            BorderMode.WEST -> DirectionalBorder(uiManager, west = true)
            BorderMode.EAST -> DirectionalBorder(uiManager, east = true)
            BorderMode.NONE -> BorderFactory.createEmptyBorder()
        }

        repaint()
    }

    enum class BorderMode {
        INSET,
        NORTH,
        SOUTH,
        WEST,
        EAST,
        NONE
    }
}