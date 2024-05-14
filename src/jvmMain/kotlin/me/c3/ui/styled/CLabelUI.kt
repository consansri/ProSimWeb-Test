package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val fontType: FontType) : BasicLabelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CLabel ?: return

        themeManager.addThemeChangeListener {
            setDefaults(cLabel)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CLabel) {
        cLabel.font = fontType.getFont(themeManager, scaleManager)
        cLabel.border = scaleManager.curr.borderScale.getInsetBorder()
        cLabel.foreground = themeManager.curr.textLaF.base
        cLabel.repaint()
    }

}