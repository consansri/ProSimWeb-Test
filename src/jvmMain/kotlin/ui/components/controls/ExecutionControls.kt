package me.c3.ui.components.controls

import me.c3.ui.UIManager
import me.c3.ui.components.controls.buttons.ThemeSwitch
import me.c3.ui.components.styled.IconButton
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class ExecutionControls(uiManager: UIManager) : JPanel() {
    val continuous = IconButton(uiManager, uiManager.icons.continuousExe)
    val singleStep = IconButton(uiManager, uiManager.icons.singleExe)
    val skipSubroutine = IconButton(uiManager, uiManager.icons.stepOver)
    val returnSubroutine = IconButton(uiManager, uiManager.icons.returnSubroutine)
    val reset = IconButton(uiManager, uiManager.icons.recompile)

    init {
        layout = GridLayout(1,0, uiManager.currScale().borderScale.insets,0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        uiManager.scaleManager.addScaleChangeEvent {
            val insets = it.borderScale.insets
            border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
            layout = GridLayout(1,0, insets,0)
        }

        uiManager.themeManager.addThemeChangeListener {
            background = it.globalStyle.bgSecondary
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

        // Set Defaults
        val insets = uiManager.currScale().borderScale.insets
        border = BorderFactory.createEmptyBorder(insets, insets, insets, insets)
        background = uiManager.currTheme().globalStyle.bgPrimary

        val exeStyle = uiManager.currTheme().exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}