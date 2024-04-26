package me.c3.ui.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.compiler.CodeStyle
import me.c3.emulator.kit.install
import me.c3.ui.components.styled.CTextPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import javax.swing.BorderFactory
import javax.swing.JComponent

class CTextPaneUI(private val themeManager: ThemeManager, private val scaleManager: ScaleManager) : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = themeManager.curr.codeLaF.getFont().deriveFont(scaleManager.curr.fontScale.codeSize)

        themeManager.addThemeChangeListener {
            setDefaults(pane)
        }

        scaleManager.addScaleChangeEvent {
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, scaleManager.curr.borderScale.insets, 0, scaleManager.curr.borderScale.insets)
        tp.background = themeManager.curr.globalLaF.bgPrimary
        tp.caretColor = themeManager.curr.codeLaF.getColor(CodeStyle.BASE0)
        tp.foreground = themeManager.curr.codeLaF.getColor(CodeStyle.BASE0)
        themeManager.curr.codeLaF.getFont().install(tp, scaleManager.curr.fontScale.codeSize)
    }

}