package me.c3.uilib.styled

import me.c3.ui.States
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        States.theme.addEvent(WeakReference(area)) { _ ->
            setDefaults(area)
        }

        States.scale.addEvent(WeakReference(area)) { _ ->
            setDefaults(area)
        }

        setDefaults(area)
    }


    private fun setDefaults(c: CTextArea) {
        c.isOpaque = false
        c.font = fontType.getFont()
        c.background = Color(0,0,0,0)
        c.border = c.borderMode.getBorder()
        c.foreground = if(c.primary) States.theme.get().textLaF.base else States.theme.get().textLaF.baseSecondary
        c.caretColor = States.theme.get().textLaF.base
    }

}