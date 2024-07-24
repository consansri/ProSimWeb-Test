package prosim.uilib.theme.themes

import com.formdev.flatlaf.extras.FlatSVGIcon
import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.Memory
import prosim.uilib.UIStates
import prosim.uilib.theme.core.Theme
import java.awt.Color

data object LightTheme : Theme {
    override val name: String = "light"
    override val icon: FlatSVGIcon = UIStates.icon.get().getLightMode()
    override val dark: Boolean = false
    override val COLOR_BG_0: Color = Color(0xFFFFFF)
    override val COLOR_BG_1: Color = Color(0xF0F2F5)
    override val COLOR_BG_OVERLAY: Color = Color(0xFFFFFF)
    override val COLOR_FG_0: Color = Color(0x454545)
    override val COLOR_FG_1: Color = Color(0x999999)
    override val COLOR_BORDER: Color = Color(0xCDCDCD)
    override val COLOR_SELECTION: Color = Color(0x4d90fe)
    override val COLOR_SEARCH_RESULT: Color = Color(0xE2B124)
    override val COLOR_GREEN_LIGHT: Color = Color(0x41A05A)
    override val COLOR_GREEN: Color = Color(0x19A744)
    override val COLOR_YELLOW: Color = Color(0xB68B0F)
    override val COLOR_BLUE: Color = Color(0x126EB4)
    override val COLOR_ORANGE: Color = Color(0xAC5916)
    override val COLOR_RED: Color = Color(0x9A0000)
    override val COLOR_ICON_FG_0: Color = Color(0x333333)
    override val COLOR_ICON_FG_1: Color = Color(0x666666)
    override val COLOR_ICON_FG_INACTIVE: Color = Color(0x77777733, true)
    override val COLOR_ICON_BG: Color = Color(0x00EFEFEF, true)
    override val COLOR_ICON_BG_HOVER: Color = Color(0x20777777, true)
    override val COLOR_ICON_BG_ACTIVE: Color = Color(0x50777777, true)

    override fun getColor(style: CodeStyle?): Color {
        if (style == null) return Color(0x333333)
        return Color(style.lightHexColor)
    }

    override fun getColor(style: Memory.InstanceType): Color = Color(style.light)
}
