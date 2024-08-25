package ui.uilib.scale

import androidx.compose.ui.text.font.Font

abstract class Scaling {

    abstract val name: String

    // SOURCE PATHS
    abstract val PATH_FONT_TEXT: String
    abstract val PATH_FONT_CODE: String

    // FONT SCALING
    abstract val FONTSCALE_SMALL: Float
    abstract val FONTSCALE_MEDIUM: Float
    abstract val FONTSCALE_LARGE: Float

    // SIZES
    abstract val SIZE_COMBOBOX: Int
    abstract val SIZE_CONTROL_SMALL: Int
    abstract val SIZE_CONTROL_MEDIUM: Int
    abstract val SIZE_INSET_SMALL: Int
    abstract val SIZE_INSET_MEDIUM: Int
    abstract val SIZE_INSET_LARGE: Int
    abstract val SIZE_CORNER_RADIUS: Int
    abstract val SIZE_BORDER_THICKNESS: Int
    abstract val SIZE_DIVIDER_THICKNESS: Int
    abstract val SIZE_SCROLL_THUMB: Int

    val SIZE_BORDER_THICKNESS_MARKED: Int
        get() = SIZE_BORDER_THICKNESS * 4

    // FONTS
    lateinit var FONT_TEXT_SMALL: Font
    lateinit var FONT_TEXT_MEDIUM: Font
    lateinit var FONT_TEXT_LARGE: Font
    lateinit var FONT_CODE_SMALL: Font
    lateinit var FONT_CODE_MEDIUM: Font
    lateinit var FONT_CODE_LARGE: Font

    // BORDERS





}