package me.c3.ui.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val areaType: AreaType) : BasicTextAreaUI() {

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
        c.font = when(areaType){
            AreaType.DATA -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)
            AreaType.CODE -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)
            AreaType.TEXT -> themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
        }
        c.background = Color(0,0,0,0)
        c.foreground = themeManager.curr.textLaF.base
        c.caretColor = themeManager.curr.textLaF.base
    }


    enum class AreaType(){
        DATA,
        CODE,
        TEXT
    }

}