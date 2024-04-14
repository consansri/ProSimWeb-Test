package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, val type: Type): BasicTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CTextField ?: return
        tf.horizontalAlignment = SwingConstants.CENTER
        tf.border = BorderFactory.createEmptyBorder()

        themeManager.addThemeChangeListener {
            setDefaults(tf)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CTextField){
        tf.isOpaque = false
        tf.font = when(type){
            Type.DATA -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.dataSize)
            Type.CODE -> themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)
            Type.TEXT -> themeManager.curr.textLaF.getBaseFont().deriveFont(scaleManager.curr.fontScale.textSize)
        }
        tf.background = Color(0,0,0,0)
        tf.foreground = themeManager.curr.textLaF.base
        tf.caretColor = themeManager.curr.textLaF.base
    }

    enum class Type(){
        DATA,
        CODE,
        TEXT
    }

}