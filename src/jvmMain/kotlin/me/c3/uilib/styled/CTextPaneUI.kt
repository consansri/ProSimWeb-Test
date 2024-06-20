package me.c3.uilib.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import me.c3.ui.States
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI() : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = States.theme.get().codeLaF.getFont().deriveFont(States.scale.get().fontScale.codeSize)

        States.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        States.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, States.scale.get().borderScale.insets, 0, States.scale.get().borderScale.insets)
        tp.background = States.theme.get().globalLaF.bgPrimary
        tp.caretColor = States.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = States.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        States.theme.get().codeLaF.getFont().install(tp, States.scale.get().fontScale.codeSize)
    }

}