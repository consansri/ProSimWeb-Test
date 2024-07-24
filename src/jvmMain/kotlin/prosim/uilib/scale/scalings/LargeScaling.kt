package prosim.uilib.scale.scalings

import prosim.uilib.scale.core.Scaling

data object LargeScaling: Scaling() {
    override val name: String = "150%"

    override val PATH_FONT_TEXT: String = "fonts/Roboto/Roboto-Regular.ttf"
    override val PATH_FONT_CODE: String = "fonts/JetBrainsMono/JetBrainsMono-Regular.ttf"

    override val FONTSCALE_SMALL: Float = 16f
    override val FONTSCALE_MEDIUM: Float = 20f
    override val FONTSCALE_LARGE: Float = 24f

    override val SIZE_COMBOBOX: Int = 128

    override val SIZE_CONTROL_SMALL: Int = 24
    override val SIZE_CONTROL_MEDIUM: Int = 32

    override val SIZE_INSET_SMALL: Int = 2
    override val SIZE_INSET_MEDIUM: Int = 4
    override val SIZE_INSET_LARGE: Int = 8

    override val SIZE_CORNER_RADIUS: Int = 10
    override val SIZE_BORDER_THICKNESS: Int = 2
    override val SIZE_DIVIDER_THICKNESS: Int = 4
    override val SIZE_SCROLL_THUMB: Int = 12

    init {
        initializeFonts()
    }

    override fun toString(): String {
        return name
    }
}