package prosim.uilib.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import prosim.uilib.UIStates
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI() : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = UIStates.theme.get().codeLaF.getFont().deriveFont(UIStates.scale.get().fontScale.codeSize)

        UIStates.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        UIStates.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, UIStates.scale.get().borderScale.insets, 0, UIStates.scale.get().borderScale.insets)
        tp.background = UIStates.theme.get().globalLaF.bgPrimary
        tp.caretColor = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = UIStates.theme.get().codeLaF.getColor(CodeStyle.BASE0)
        UIStates.theme.get().codeLaF.getFont().install(tp, UIStates.scale.get().fontScale.codeSize)
    }

}