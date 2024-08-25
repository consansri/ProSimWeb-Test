package ui.uilib.theme

import androidx.compose.ui.graphics.Color
import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.Memory

interface Theme {
    val name: String
    val icon: String
    val dark: Boolean

    // COLORS (BASIC)

    val COLOR_BG_0: Color
    val COLOR_BG_1: Color
    val COLOR_BG_OVERLAY: Color

    val COLOR_FG_0: Color
    val COLOR_FG_1: Color

    val COLOR_BORDER: Color

    val COLOR_SELECTION: Color
    val COLOR_SEARCH_RESULT: Color

    // COLORS (SIMPLE)

    val COLOR_GREEN_LIGHT: Color
    val COLOR_GREEN: Color
    val COLOR_YELLOW: Color
    val COLOR_BLUE: Color
    val COLOR_ORANGE: Color
    val COLOR_RED: Color

    // COLORS (ICON)

    val COLOR_ICON_FG_0: Color
    val COLOR_ICON_FG_1: Color
    val COLOR_ICON_FG_INACTIVE: Color
    val COLOR_ICON_BG: Color
    val COLOR_ICON_BG_HOVER: Color
    val COLOR_ICON_BG_ACTIVE: Color

    fun getColor(style: CodeStyle?): Color
    fun getColor(style: Memory.InstanceType): Color
}