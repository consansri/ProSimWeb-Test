package me.c3.uilib.styled

import me.c3.ui.States
import me.c3.uilib.styled.params.FontType
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val fontType: FontType) : BasicLabelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CLabel ?: return

        States.theme.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        States.scale.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CLabel) {
        cLabel.font = fontType.getFont()
        cLabel.border = States.scale.get().borderScale.getInsetBorder()
        cLabel.foreground = States.theme.get().textLaF.base
        cLabel.repaint()
    }

}