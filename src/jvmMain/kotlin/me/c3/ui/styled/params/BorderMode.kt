package me.c3.ui.styled.params

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.borders.DirectionalBorder
import me.c3.ui.theme.ThemeManager
import javax.swing.BorderFactory
import javax.swing.border.Border

enum class BorderMode {
    INSET,
    THICKNESS,
    NORTH,
    SOUTH,
    WEST,
    EAST,
    HORIZONTAL,
    VERTICAL,
    BOWL,
    BRIDGE,
    NONE;

    fun getBorder(tm: ThemeManager, sm: ScaleManager): Border {
        return when (this) {
            INSET -> sm.curr.borderScale.getInsetBorder()
            THICKNESS -> sm.curr.borderScale.getThicknessBorder()
            NORTH -> DirectionalBorder(tm, sm, north = true)
            SOUTH -> DirectionalBorder(tm, sm, south = true)
            WEST -> DirectionalBorder(tm, sm, west = true)
            EAST -> DirectionalBorder(tm, sm, east = true)
            HORIZONTAL -> DirectionalBorder(tm, sm, north = true, south = true)
            VERTICAL -> DirectionalBorder(tm, sm, west = true, east = true)
            BOWL -> DirectionalBorder(tm, sm, west = true, east = true, south = true)
            BRIDGE -> DirectionalBorder(tm, sm, west = true, east = true, north = true)
            NONE -> BorderFactory.createEmptyBorder()
        }
    }
}