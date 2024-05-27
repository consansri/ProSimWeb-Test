package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val tm: ThemeManager, private val sm: ScaleManager, private val fontType: FontType) : BasicLabelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CLabel ?: return

        tm.addThemeChangeListener {
            setDefaults(cLabel)
        }

        sm.addScaleChangeEvent {
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CLabel) {
        cLabel.font = fontType.getFont(tm, sm)
        cLabel.border = sm.curr.borderScale.getInsetBorder()
        cLabel.foreground = tm.curr.textLaF.base
        cLabel.repaint()
    }

}