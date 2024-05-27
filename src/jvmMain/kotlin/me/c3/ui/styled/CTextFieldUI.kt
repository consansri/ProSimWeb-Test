package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI(private val tm: ThemeManager, private val sm: ScaleManager, private val fontType: FontType): BasicTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CTextField ?: return
        tf.horizontalAlignment = SwingConstants.CENTER
        tf.border = BorderFactory.createEmptyBorder()

        tm.addThemeChangeListener {
            setDefaults(tf)
        }

        sm.addScaleChangeEvent {
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CTextField){
        tf.isOpaque = false
        tf.font = fontType.getFont(tm, sm)
        tf.background = Color(0,0,0,0)
        tf.foreground = tm.curr.textLaF.base
        tf.caretColor = tm.curr.textLaF.base
    }
}