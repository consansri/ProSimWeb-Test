package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        themeManager.addThemeChangeListener {
            setDefaults(area)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(area)
        }

        setDefaults(area)
    }


    private fun setDefaults(c: CTextArea) {
        c.isOpaque = false
        c.font = fontType.getFont(themeManager, scaleManager)
        c.background = Color(0,0,0,0)
        c.border = c.borderMode.getBorder(themeManager, scaleManager)
        c.foreground = if(c.primary) themeManager.curr.textLaF.base else themeManager.curr.textLaF.baseSecondary
        c.caretColor = themeManager.curr.textLaF.base
    }

}