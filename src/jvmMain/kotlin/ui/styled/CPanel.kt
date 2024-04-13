package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CPanelUI
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.round

open class CPanel(themeManager: ThemeManager, scaleManager: ScaleManager, primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false, val isOverlay: Boolean = false) : JPanel() {

    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            repaint()
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            repaint()
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CPanelUI(themeManager, scaleManager))
    }

    enum class BorderMode {
        INSET,
        BASIC,
        NORTH,
        SOUTH,
        WEST,
        EAST,
        NONE
    }
}