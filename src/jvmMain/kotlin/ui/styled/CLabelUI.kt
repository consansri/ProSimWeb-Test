package me.c3.ui.styled

import me.c3.ui.components.styled.CLabel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager) : BasicLabelUI() {

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
        cLabel.font = themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
        cLabel.border = scaleManager.curr.borderScale.getInsetBorder()
        cLabel.foreground = themeManager.curr.textLaF.base
        cLabel.repaint()
    }

}