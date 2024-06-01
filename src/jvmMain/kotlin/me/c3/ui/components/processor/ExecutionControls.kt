package me.c3.ui.components.processor

import me.c3.ui.manager.*
import me.c3.ui.styled.CIconButton
import me.c3.ui.styled.CPanel
import me.c3.ui.styled.CIconInput
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import java.awt.GridLayout
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ExecutionControls() : CPanel( primary = false, BorderMode.SOUTH) {
    val continuous = CIconButton( ResManager.icons.continuousExe).apply {
        addActionListener {
            ArchManager.curr.exeContinuous()
            EventManager.triggerExeEvent()
        }
    }
    val singleStep = CIconButton( ResManager.icons.singleExe).apply {
        addActionListener {
            ArchManager.curr.exeSingleStep()
            EventManager.triggerExeEvent()
        }
    }
    val mStep = CIconInput( ResManager.icons.stepMultiple, FontType.BASIC).apply {
        val inputRegex = Regex("\\d+")
        input.text = 10.toString()
        button.addActionListener {
            val steps = this.input.text.toLongOrNull()
            steps?.let {
                ArchManager.curr.exeMultiStep(steps)
                EventManager.triggerExeEvent()
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

    val skipSubroutine = CIconButton( ResManager.icons.stepOver).apply {
        addActionListener {
            ArchManager.curr.exeSkipSubroutine()
            EventManager.triggerExeEvent()
        }
    }
    val returnSubroutine = CIconButton( ResManager.icons.returnSubroutine).apply {
        addActionListener {
            ArchManager.curr.exeReturnFromSubroutine()
            EventManager.triggerExeEvent()
        }
    }
    val reset = CIconButton( ResManager.icons.recompile).apply {
        addActionListener {
            ArchManager.curr.exeReset()
            EventManager.triggerExeEvent()
        }
    }

    init {
        layout = GridLayout(1, 0,ScaleManager.curr.borderScale.insets, 0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        mStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        ScaleManager.addScaleChangeEvent {
            layout = GridLayout(1, 0, it.borderScale.insets, 0)
        }

        ThemeManager.addThemeChangeListener {
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

        val exeStyle =ThemeManager.curr.exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        mStep.button.customColor = exeStyle.multi
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}