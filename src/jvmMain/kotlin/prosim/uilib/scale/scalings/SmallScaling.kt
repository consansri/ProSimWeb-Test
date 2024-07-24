package prosim.uilib.scale.scalings

import prosim.uilib.scale.core.Scaling

data object SmallScaling : Scaling() {
    override val name: String = "75%"

    override val PATH_FONT_TEXT: String = "fonts/Roboto/Roboto-Regular.ttf"
    override val PATH_FONT_CODE: String = "fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"

    override val FONTSCALE_SMALL: Float = 8f
    override val FONTSCALE_MEDIUM: Float = 12f
    override val FONTSCALE_LARGE: Float = 16f

    override val SIZE_COMBOBOX: Int = 96

    override val SIZE_CONTROL_SMALL: Int = 14
    override val SIZE_CONTROL_MEDIUM: Int = 20

    override val SIZE_INSET_SMALL: Int = 2
    override val SIZE_INSET_MEDIUM: Int = 3
    override val SIZE_INSET_LARGE: Int = 4

    override val SIZE_CORNER_RADIUS: Int = 10
    override val SIZE_BORDER_THICKNESS: Int = 1
    override val SIZE_DIVIDER_THICKNESS: Int = 3
    override val SIZE_SCROLL_THUMB: Int = 6

    init {
        initializeFonts()
    }

    override fun toString(): String {
        return name
    }
}