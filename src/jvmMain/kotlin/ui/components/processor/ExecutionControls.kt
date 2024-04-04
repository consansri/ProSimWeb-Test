package me.c3.ui.components.processor

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class ExecutionControls(uiManager: UIManager) : CPanel(uiManager, primary = false) {
    val continuous = CIconButton(uiManager, uiManager.icons.continuousExe)
    val singleStep = CIconButton(uiManager, uiManager.icons.singleExe)
    val skipSubroutine = CIconButton(uiManager, uiManager.icons.stepOver)
    val returnSubroutine = CIconButton(uiManager, uiManager.icons.returnSubroutine)
    val reset = CIconButton(uiManager, uiManager.icons.recompile)

    init {
        layout = GridLayout(1,0, uiManager.currScale().borderScale.insets,0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        uiManager.scaleManager.addScaleChangeEvent {
            layout = GridLayout(1,0, it.borderScale.insets,0)
        }

        uiManager.themeManager.addThemeChangeListener {
            val exeStyle = it.exeStyle
            continuous.customColor = exeStyle.continuous
            singleStep.customColor = exeStyle.single
            skipSubroutine.customColor = exeStyle.skipSR
            returnSubroutine.customColor = exeStyle.returnSR
            reset.customColor = exeStyle.reassemble
        }

        add(continuous)
        add(singleStep)
        add(skipSubroutine)
        add(returnSubroutine)
        add(reset)

        val exeStyle = uiManager.currTheme().exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}