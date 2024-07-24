package prosim.uilib.styled.params

import prosim.uilib.UIStates
import prosim.uilib.styled.borders.DirectionalBorder
import javax.swing.BorderFactory
import javax.swing.border.Border

enum class BorderMode {
    INSET,
    THICKNESS,
    BASIC,
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
            INSET -> UIStates.scale.get().BORDER_INSET_MEDIUM
            THICKNESS -> UIStates.scale.get().BORDER_THICKNESS
            BASIC -> DirectionalBorder(north = true, west = true, south = true, east = true)
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