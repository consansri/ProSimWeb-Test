package prosim.uilib.styled.table

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.JComponent

abstract class CCell(val fontType: FontType) : JComponent() {

    var customBG: Color? = null
        set(value) {
            field = value
            repaint()
        }

    var customFG: Color? = null
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CCellUI(fontType))
    }

    abstract fun textToDraw(): String

    override fun getMinimumSize(): Dimension {
        val string = textToDraw()
        val fm = getFontMetrics(font)
        val width = fm.stringWidth(string)
        val height = fm.height
        return Dimension(width, height)
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().textLaF.base
    }

    override fun getBackground(): Color {
        return customBG ?: Color(0, 0, 0, 0)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

}