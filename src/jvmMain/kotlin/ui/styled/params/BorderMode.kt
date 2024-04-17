package me.c3.ui.styled.params

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import javax.swing.BorderFactory
import javax.swing.border.Border

enum class BorderMode {
    INSET,
    BASIC,
    NORTH,
    SOUTH,
    WEST,
    EAST,
    NONE;
    fun getBorder(themeManager: ThemeManager, scaleManager: ScaleManager): Border{
        return when (this) {
            INSET -> scaleManager.curr.borderScale.getInsetBorder()
            BASIC -> scaleManager.curr.borderScale.getThicknessBorder()
            NORTH -> DirectionalBorder(themeManager, scaleManager, north = true)
            SOUTH -> DirectionalBorder(themeManager, scaleManager, south = true)
            WEST -> DirectionalBorder(themeManager, scaleManager, west = true)
            EAST -> DirectionalBorder(themeManager, scaleManager, east = true)
            NONE -> BorderFactory.createEmptyBorder()
        }
    }
}