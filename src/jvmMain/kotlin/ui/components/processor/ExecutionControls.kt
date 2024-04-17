package me.c3.ui.components.processor

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CIconInput
import me.c3.ui.styled.CTextFieldUI
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ExecutionControls(uiManager: UIManager) : CPanel(uiManager.themeManager, uiManager.scaleManager, primary = false, BorderMode.SOUTH) {
    val continuous = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.continuousExe).apply {
        addActionListener {
            uiManager.currArch().exeContinuous()
            uiManager.eventManager.triggerExeEvent()
        }
    }
    val singleStep = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.singleExe).apply {
        addActionListener {
            uiManager.currArch().exeSingleStep()
            uiManager.eventManager.triggerExeEvent()
        }
    }
    val mStep = CIconInput(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.stepMultiple, FontType.BASIC).apply {
        val inputRegex = Regex("\\d+")
        input.text = 10.toString()
        button.addActionListener {
            val steps = this.input.text.toIntOrNull()
            steps?.let {
                uiManager.currArch().exeMultiStep(steps)
                uiManager.eventManager.triggerExeEvent()
            }
        }
        input.apply {
            (document as? AbstractDocument)?.documentFilter = object : DocumentFilter() {
                override fun insertString(fb: FilterBypass?, offset: Int, string: String, attr: AttributeSet?) {
                    if (string.matches(inputRegex)) {
                        super.insertString(fb, offset, string, attr)
                    }
                }

                override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
                    if (text.matches(inputRegex)) {
                        super.insertString(fb, offset, text, attrs)
                    }
                }
            }
        }
    }

    val skipSubroutine = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.stepOver).apply {
        addActionListener {
            uiManager.currArch().exeSkipSubroutine()
            uiManager.eventManager.triggerExeEvent()
        }
    }
    val returnSubroutine = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.returnSubroutine).apply {
        addActionListener {
            uiManager.currArch().exeReturnFromSubroutine()
            uiManager.eventManager.triggerExeEvent()
        }
    }
    val reset = CIconButton(uiManager.themeManager, uiManager.scaleManager, uiManager.icons.recompile).apply {
        addActionListener {
            uiManager.currArch().exeReset()
            uiManager.eventManager.triggerExeEvent()
        }
    }

    init {
        layout = GridLayout(1, 0, uiManager.currScale().borderScale.insets, 0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        mStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        uiManager.scaleManager.addScaleChangeEvent {
            layout = GridLayout(1, 0, it.borderScale.insets, 0)
        }

        uiManager.themeManager.addThemeChangeListener {
            val exeStyle = it.exeStyle
            continuous.customColor = exeStyle.continuous
            singleStep.customColor = exeStyle.single
            mStep.button.customColor = exeStyle.multi
            skipSubroutine.customColor = exeStyle.skipSR
            returnSubroutine.customColor = exeStyle.returnSR
            reset.customColor = exeStyle.reassemble
        }

        add(continuous)
        add(singleStep)
        add(mStep)
        add(skipSubroutine)
        add(returnSubroutine)
        add(reset)

        val exeStyle = uiManager.currTheme().exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        mStep.button.customColor = exeStyle.multi
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}