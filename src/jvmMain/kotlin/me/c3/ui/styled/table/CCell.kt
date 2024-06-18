package me.c3.ui.styled.table

import me.c3.ui.styled.params.FontType
import java.awt.Color
import java.awt.Dimension
import java.awt.FontMetrics
import javax.swing.JComponent

abstract class CCell(fontType: FontType) : JComponent() {

    var customBG: Color? = null
        set(value) {
            field = value
            (ui as? CCellUI)?.updateTextColors(this)
        }

    var customFG: Color? = null
        set(value) {
            field = value
            (ui as? CCellUI)?.updateTextColors(this)
        }

    var fontMetrics: FontMetrics = this.getFontMetrics(fontType.getFont())
        set(value) {
            field = value
            revalidate()
        }

    init {
        this.setUI(CCellUI(fontType))
    }

    abstract fun textToDraw(): String

    override fun getMinimumSize(): Dimension {
        val string = textToDraw()
        val width = fontMetrics.stringWidth(string)
        val height = fontMetrics.height
        return Dimension(width, height)
    }
}