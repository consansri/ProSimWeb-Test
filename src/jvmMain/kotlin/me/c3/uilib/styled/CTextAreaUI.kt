package me.c3.uilib.styled

import me.c3.uilib.UIStates
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextAreaUI

class CTextAreaUI(private val fontType: FontType) : BasicTextAreaUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val area = (c as? CTextArea) ?: return

        UIStates.theme.addEvent(WeakReference(area)) { _ ->
            setDefaults(area)
        }

        UIStates.scale.addEvent(WeakReference(area)) { _ ->
            setDefaults(area)
        }

        setDefaults(area)
    }


    private fun setDefaults(c: CTextArea) {
        c.isOpaque = false
        c.font = fontType.getFont()
        c.background = Color(0,0,0,0)
        c.border = c.borderMode.getBorder()
        c.foreground = if(c.primary) UIStates.theme.get().textLaF.base else UIStates.theme.get().textLaF.baseSecondary
        c.caretColor = UIStates.theme.get().textLaF.base
    }

}