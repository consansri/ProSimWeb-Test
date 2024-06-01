package me.c3.ui.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembler.CodeStyle
import emulator.kit.install
import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI() : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = ThemeManager.curr.codeLaF.getFont().deriveFont(ScaleManager.curr.fontScale.codeSize)

        ThemeManager.addThemeChangeListener {
            setDefaults(pane)
        }

        ScaleManager.addScaleChangeEvent {
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, ScaleManager.curr.borderScale.insets, 0, ScaleManager.curr.borderScale.insets)
        tp.background = ThemeManager.curr.globalLaF.bgPrimary
        tp.caretColor = ThemeManager.curr.codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = ThemeManager.curr.codeLaF.getColor(CodeStyle.BASE0)
        ThemeManager.curr.codeLaF.getFont().install(tp, ScaleManager.curr.fontScale.codeSize)
    }

}