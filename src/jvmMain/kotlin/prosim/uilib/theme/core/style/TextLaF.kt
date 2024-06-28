package prosim.uilib.theme.core.style

import org.jetbrains.skia.Typeface
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class TextLaF(
    val base: Color,
    val baseSecondary: Color,
    val selected: Color,
    fontPath: String,
    titleFontPath: String
){
    private var baseTF: Typeface = Theme.loadSkiaTF(fontPath) ?: Typeface.makeDefault()

    private var font: Font = Theme.loadFont(fontPath)
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }

    private var titleTF: Typeface = Theme.loadSkiaTF(titleFontPath) ?: Typeface.makeDefault()
    private var titleFont: Font = Theme.loadFont(titleFontPath)
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }

    fun getBaseFont(): Font = font
    fun getTitleFont(): Font = titleFont
    fun getBaseTypeface(): Typeface = baseTF
    fun getTitleTypeface(): Typeface = titleTF

}