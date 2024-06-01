package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI( private val fontType: FontType): BasicTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CTextField ?: return
        tf.horizontalAlignment = SwingConstants.CENTER
        tf.border = BorderFactory.createEmptyBorder()

        ThemeManager.addThemeChangeListener {
            setDefaults(tf)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CTextField){
        tf.isOpaque = false
        tf.font = fontType.getFont()
        tf.background = Color(0,0,0,0)
        tf.foreground = ThemeManager.curr.textLaF.base
        tf.caretColor = ThemeManager.curr.textLaF.base
    }
}