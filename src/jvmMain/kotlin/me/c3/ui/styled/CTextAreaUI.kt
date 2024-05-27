package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val tm: ThemeManager, private val sm: ScaleManager, private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        tm.addThemeChangeListener {
            setDefaults(area)
        }

        sm.addScaleChangeEvent {
            setDefaults(area)
        }

        setDefaults(area)
    }


    private fun setDefaults(c: CTextArea) {
        c.isOpaque = false
        c.font = fontType.getFont(tm, sm)
        c.background = Color(0,0,0,0)
        c.border = c.borderMode.getBorder(tm, sm)
        c.foreground = if(c.primary) tm.curr.textLaF.base else tm.curr.textLaF.baseSecondary
        c.caretColor = tm.curr.textLaF.base
    }

}