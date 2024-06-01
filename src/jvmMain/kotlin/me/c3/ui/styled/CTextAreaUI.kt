package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        ThemeManager.addThemeChangeListener {
            setDefaults(area)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(area)
        }

        setDefaults(area)
    }


    private fun setDefaults(c: CTextArea) {
        c.isOpaque = false
        c.font = fontType.getFont()
        c.background = Color(0,0,0,0)
        c.border = c.borderMode.getBorder()
        c.foreground = if(c.primary) ThemeManager.curr.textLaF.base else ThemeManager.curr.textLaF.baseSecondary
        c.caretColor = ThemeManager.curr.textLaF.base
    }

}