package prosim.uilib.theme.core.style

import emulator.kit.assembler.CodeStyle
import org.jetbrains.skia.Typeface
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class CodeLaF(
    codeFontPath: String,
    markupFontPath: String,
    val pcIdenticator: String,
    val selectionColor: Color,
    val searchResultColor: Color,
    val getColor: (CodeStyle?) -> Color
) {
    private var codeTF: Typeface = Theme.loadSkiaTF(codeFontPath) ?: Typeface.makeDefault()
    private var markupTF: Typeface = Theme.loadSkiaTF(markupFontPath) ?: Typeface.makeDefault()
    private var codeFont: Font = Theme.loadFont(codeFontPath)
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }
    private var markupFont: Font = Theme.loadFont(markupFontPath)
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }
    
    fun getFont(): Font = codeFont
    fun getMarkupFont(): Font = markupFont
    fun getTF(): Typeface = codeTF
    fun getMarkupTF(): Typeface = markupTF

}