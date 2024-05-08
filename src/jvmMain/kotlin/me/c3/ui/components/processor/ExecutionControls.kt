package me.c3.ui.components.processor

import me.c3.ui.MainManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.styled.CIconInput
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridLayout
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ExecutionControls(mainManager: MainManager) : CPanel(mainManager.themeManager, mainManager.scaleManager, primary = false, BorderMode.SOUTH) {
    val continuous = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.continuousExe).apply {
        addActionListener {
            mainManager.currArch().exeContinuous()
            mainManager.eventManager.triggerExeEvent()
        }
    }
    val singleStep = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.singleExe).apply {
        addActionListener {
            mainManager.currArch().exeSingleStep()
            mainManager.eventManager.triggerExeEvent()
        }
    }
    val mStep = CIconInput(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.stepMultiple, FontType.BASIC).apply {
        val inputRegex = Regex("\\d+")
        input.text = 10.toString()
        button.addActionListener {
            val steps = this.input.text.toIntOrNull()
            steps?.let {
                mainManager.currArch().exeMultiStep(steps)
                mainManager.eventManager.triggerExeEvent()
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

    val skipSubroutine = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.stepOver).apply {
        addActionListener {
            mainManager.currArch().exeSkipSubroutine()
            mainManager.eventManager.triggerExeEvent()
        }
    }
    val returnSubroutine = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.returnSubroutine).apply {
        addActionListener {
            mainManager.currArch().exeReturnFromSubroutine()
            mainManager.eventManager.triggerExeEvent()
        }
    }
    val reset = CIconButton(mainManager.themeManager, mainManager.scaleManager, mainManager.icons.recompile).apply {
        addActionListener {
            mainManager.currArch().exeReset()
            mainManager.eventManager.triggerExeEvent()
        }
    }

    init {
        layout = GridLayout(1, 0, mainManager.currScale().borderScale.insets, 0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        mStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        mainManager.scaleManager.addScaleChangeEvent {
            layout = GridLayout(1, 0, it.borderScale.insets, 0)
        }

        mainManager.themeManager.addThemeChangeListener {
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

        val exeStyle = mainManager.currTheme().exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        mStep.button.customColor = exeStyle.multi
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}