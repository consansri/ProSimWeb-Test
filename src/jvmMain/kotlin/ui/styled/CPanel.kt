package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CPanelUI
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities

open class CPanel(uiManager: UIManager, private val primary: Boolean = false, private val borderMode: BorderMode = BorderMode.NONE) : JPanel(), UIAdapter {

    init {
        this.setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CPanelUI())

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {
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