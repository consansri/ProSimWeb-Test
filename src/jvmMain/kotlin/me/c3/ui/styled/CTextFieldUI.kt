package me.c3.ui.styled

import me.c3.ui.States
import me.c3.ui.styled.params.FontType
import java.awt.Color
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI(private val fontType: FontType) : BasicTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CTextField ?: return
        tf.horizontalAlignment = SwingConstants.CENTER
        tf.border = BorderFactory.createEmptyBorder()

        States.theme.addEvent(WeakReference(tf)) { _ ->
            setDefaults(tf)
        }

        States.scale.addEvent(WeakReference(tf)) { _ ->
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CTextField) {
        tf.isOpaque = false
        tf.font = fontType.getFont()
        tf.caretColor = States.theme.get().textLaF.base
        updateTextColors(tf)
    }

    fun updateTextColors(tf: CTextField) {
        val customFG = tf.customFG
        val customBG = tf.customBG
        tf.background = customBG ?: Color(0, 0, 0, 0)
        tf.foreground = customFG ?: States.theme.get().textLaF.base
    }

}