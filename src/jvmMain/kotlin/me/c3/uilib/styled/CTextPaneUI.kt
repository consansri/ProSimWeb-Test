package me.c3.uilib.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import me.c3.uilib.UIManager
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI() : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = UIManager.theme.get().codeLaF.getFont().deriveFont(UIManager.scale.get().fontScale.codeSize)

        UIManager.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        UIManager.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, UIManager.scale.get().borderScale.insets, 0, UIManager.scale.get().borderScale.insets)
        tp.background = UIManager.theme.get().globalLaF.bgPrimary
        tp.caretColor = UIManager.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = UIManager.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        UIManager.theme.get().codeLaF.getFont().install(tp, UIManager.scale.get().fontScale.codeSize)
    }

}