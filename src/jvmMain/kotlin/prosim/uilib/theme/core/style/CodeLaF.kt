package prosim.uilib.theme.core.style

import emulator.kit.assembler.CodeStyle
import org.jetbrains.skia.Typeface
import prosim.uilib.theme.core.Theme
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

class CodeLaF(
    fontPath: String,
    val pcIdenticator: String,
    val selectionColor: Color,
    val searchResultColor: Color,
    val getColor: (CodeStyle?) -> Color
) {
    private var typeface: Typeface = Theme.loadSkiaTF(fontPath) ?: Typeface.makeDefault()
    private var font: Font = Theme.loadFont(fontPath)
        set(value) {
            field = value
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(value)
        }

    fun getFont(): Font = font
    fun getTF(): Typeface = typeface

}