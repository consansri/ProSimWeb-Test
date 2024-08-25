package prosim.uilib.scale.core

import java.awt.*
import java.io.IOException
import java.io.InputStream
import javax.swing.BorderFactory
import javax.swing.border.Border

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
    val BORDER_INSET_SMALL: Border by lazy { BorderFactory.createEmptyBorder(SIZE_INSET_SMALL, SIZE_INSET_SMALL, SIZE_INSET_SMALL, SIZE_INSET_SMALL) }
    val BORDER_INSET_MEDIUM: Border by lazy { BorderFactory.createEmptyBorder(SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM) }
    val BORDER_INSET_LARGE: Border by lazy { BorderFactory.createEmptyBorder(SIZE_INSET_LARGE, SIZE_INSET_LARGE, SIZE_INSET_LARGE, SIZE_INSET_LARGE) }
    val BORDER_THICKNESS: Border by lazy { BorderFactory.createEmptyBorder(SIZE_BORDER_THICKNESS, SIZE_BORDER_THICKNESS, SIZE_BORDER_THICKNESS, SIZE_BORDER_THICKNESS) }

    val INSETS_SMALL: Insets by lazy { Insets(SIZE_INSET_SMALL, SIZE_INSET_SMALL, SIZE_INSET_SMALL, SIZE_INSET_SMALL) }
    val INSETS_MEDIUM: Insets by lazy { Insets(SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM, SIZE_INSET_MEDIUM) }
    val INSETS_LARGE: Insets by lazy { Insets(SIZE_INSET_LARGE, SIZE_INSET_LARGE, SIZE_INSET_LARGE, SIZE_INSET_LARGE) }

    val DIM_CONTROL_SMALL: Dimension by lazy { Dimension(SIZE_CONTROL_SMALL, SIZE_CONTROL_SMALL) }
    val DIM_CONTROL_MEDIUM: Dimension by lazy { Dimension(SIZE_CONTROL_MEDIUM, SIZE_CONTROL_MEDIUM) }

    fun initAWTComponents() {
        initFonts()
    }

    private fun initBorders(){

    }

    private fun initFonts(){
        val textFont = loadFont(PATH_FONT_TEXT)
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(textFont)

        val codeFont = loadFont(PATH_FONT_CODE)
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(codeFont)

        FONT_TEXT_SMALL = textFont.deriveFont(FONTSCALE_SMALL)
        FONT_TEXT_MEDIUM = textFont.deriveFont(FONTSCALE_MEDIUM)
        FONT_TEXT_LARGE = textFont.deriveFont(FONTSCALE_LARGE)

        FONT_CODE_SMALL = codeFont.deriveFont(FONTSCALE_SMALL)
        FONT_CODE_MEDIUM = codeFont.deriveFont(FONTSCALE_MEDIUM)
        FONT_CODE_LARGE = codeFont.deriveFont(FONTSCALE_LARGE)
    }

    companion object {
        fun loadFont(url: String): Font {
            val inputStream: InputStream? = this::class.java.classLoader.getResourceAsStream(url)

            requireNotNull(inputStream) { "Font file not found: $url" }

            return try {
                Font.createFont(Font.TRUETYPE_FONT, inputStream)
            } catch (e: FontFormatException) {
                throw RuntimeException("Error loading font", e)
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}