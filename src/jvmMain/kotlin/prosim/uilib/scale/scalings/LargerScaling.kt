package prosim.uilib.scale.scalings

import prosim.uilib.scale.core.Scaling

data object LargerScaling: Scaling() {
    override val name: String = "125%"

    override val PATH_FONT_TEXT: String = "fonts/Roboto/Roboto-Regular.ttf"
    override val PATH_FONT_CODE: String = "fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"

    override val FONTSCALE_SMALL: Float = 12f
    override val FONTSCALE_MEDIUM: Float = 16f
    override val FONTSCALE_LARGE: Float = 20f

    override val SIZE_COMBOBOX: Int = 128
    override val SIZE_CONTROL_SMALL: Int = 18
    override val SIZE_CONTROL_MEDIUM: Int = 28
    override val SIZE_INSET_SMALL: Int = 2
    override val SIZE_INSET_MEDIUM: Int = 4
    override val SIZE_INSET_LARGE: Int = 8
    override val SIZE_CORNER_RADIUS: Int = 10
    override val SIZE_BORDER_THICKNESS: Int = 1
    override val SIZE_DIVIDER_THICKNESS: Int = 4
    override val SIZE_SCROLL_THUMB: Int = 8

    init {
        initAWTComponents()
    }

    override fun toString(): String {
        return name
    }
}