package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, private val fontType: FontType): BasicTextFieldUI() {

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
        tf.font = fontType.getFont(themeManager, scaleManager)
        tf.background = Color(0,0,0,0)
        tf.foreground = themeManager.curr.textLaF.base
        tf.caretColor = themeManager.curr.textLaF.base
    }
}