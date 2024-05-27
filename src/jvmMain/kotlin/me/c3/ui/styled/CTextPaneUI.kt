package me.c3.ui.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI(private val tm: ThemeManager, private val sm: ScaleManager) : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = tm.curr.codeLaF.getFont().deriveFont(sm.curr.fontScale.codeSize)

        tm.addThemeChangeListener {
            setDefaults(pane)
        }

        sm.addScaleChangeEvent {
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, sm.curr.borderScale.insets, 0, sm.curr.borderScale.insets)
        tp.background = tm.curr.globalLaF.bgPrimary
        tp.caretColor = tm.curr.codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = tm.curr.codeLaF.getColor(CodeStyle.BASE0)
        tm.curr.codeLaF.getFont().install(tp, sm.curr.fontScale.codeSize)
    }

}