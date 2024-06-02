package me.c3.ui.styled.params

import me.c3.ui.States
import me.c3.ui.styled.borders.DirectionalBorder
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

    fun getBorder(): Border {
        return when (this) {
            INSET -> States.scale.get().borderScale.getInsetBorder()
            THICKNESS -> States.scale.get().borderScale.getThicknessBorder()
            NORTH -> DirectionalBorder(north = true)
            SOUTH -> DirectionalBorder(south = true)
            WEST -> DirectionalBorder(west = true)
            EAST -> DirectionalBorder(east = true)
            HORIZONTAL -> DirectionalBorder(north = true, south = true)
            VERTICAL -> DirectionalBorder(west = true, east = true)
            BOWL -> DirectionalBorder(west = true, east = true, south = true)
            BRIDGE -> DirectionalBorder(west = true, east = true, north = true)
            NONE -> BorderFactory.createEmptyBorder()
        }
    }
}