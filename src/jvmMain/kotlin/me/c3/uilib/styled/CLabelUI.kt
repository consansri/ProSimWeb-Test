package me.c3.uilib.styled

import me.c3.uilib.UIStates
import me.c3.uilib.styled.params.FontType
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CLabelUI(private val fontType: FontType) : BasicLabelUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CLabel ?: return

        UIStates.theme.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        UIStates.scale.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CLabel) {
        cLabel.font = fontType.getFont()
        cLabel.border = UIStates.scale.get().borderScale.getInsetBorder()
        cLabel.foreground = UIStates.theme.get().textLaF.base
        cLabel.repaint()
    }
}