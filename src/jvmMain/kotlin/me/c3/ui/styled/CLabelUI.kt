package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val fontType: FontType) : BasicLabelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CLabel ?: return

        ThemeManager.addThemeChangeListener {
            setDefaults(cLabel)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CLabel) {
        cLabel.font = fontType.getFont()
        cLabel.border = ScaleManager.curr.borderScale.getInsetBorder()
        cLabel.foreground = ThemeManager.curr.textLaF.base
        cLabel.repaint()
    }

}