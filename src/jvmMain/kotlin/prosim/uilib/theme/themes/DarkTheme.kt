package prosim.uilib.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.Memory
import prosim.uilib.UIStates
import prosim.uilib.theme.core.Theme
import java.awt.Color

data object DarkTheme : Theme {
    override val name: String = "dark"
    override val icon: FlatSVGIcon = UIStates.icon.get().getDarkMode()
    override val dark: Boolean = true
    override val COLOR_BG_0: Color = Color(0x222222)
    override val COLOR_BG_1: Color = Color(0x373737)
    override val COLOR_BG_OVERLAY: Color = Color(0x222222)
    override val COLOR_FG_0: Color = Color(0xD5D5D5)
    override val COLOR_FG_1: Color = Color(0x777777)
    override val COLOR_BORDER: Color = Color(0x777777)
    override val COLOR_SELECTION: Color = Color(0x777777)
    override val COLOR_SEARCH_RESULT: Color = Color(0xB68B0F)
    override val COLOR_GREEN_LIGHT: Color = Color(0x98D8AA)
    override val COLOR_GREEN: Color = Color(0x58CC79)
    override val COLOR_YELLOW: Color = Color(0xE2B124)
    override val COLOR_BLUE: Color = Color(0x549FD8)
    override val COLOR_ORANGE: Color = Color(0xEE9955)
    override val COLOR_RED: Color = Color(0xEE2222)
    override val COLOR_ICON_FG_0: Color = Color(0xD5D5D5)
    override val COLOR_ICON_FG_1: Color = Color(0xAAAAAA)
    override val COLOR_ICON_FG_INACTIVE: Color = Color(0x77777733, true)
    override val COLOR_ICON_BG: Color = Color(0, 0, 0, 0)
    override val COLOR_ICON_BG_HOVER: Color = Color(0x30777777, true)
    override val COLOR_ICON_BG_ACTIVE: Color = Color(0x50777777, true)

    override fun getColor(style: CodeStyle?): Color {
        if (style == null) return Color(CodeStyle.baseColor.getDarkElseLight())
        return Color(style.getDarkElseLight())
    }

    override fun getColor(style: Memory.InstanceType): Color {
        val colInt = style.dark ?: style.light
        return Color(colInt)
    }

}