package prosim.ui.components.processor

import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CIconInput
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.GridLayout
import java.lang.ref.WeakReference
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ExecutionControls() : CPanel(primary = false, BorderMode.SOUTH) {
    val continuous = CIconButton(UIStates.icon.get().continuousExe).apply {
        addActionListener {
            States.arch.get().exeContinuous()
            Events.exe.triggerEvent(States.arch.get())
        }
    }
    val singleStep = CIconButton(UIStates.icon.get().singleExe).apply {
        addActionListener {
            States.arch.get().exeSingleStep()
            Events.exe.triggerEvent(States.arch.get())
        }
    }
    val mStep = prosim.uilib.styled.CIconInput(UIStates.icon.get().stepMultiple, FontType.BASIC).apply {
        val inputRegex = Regex("\\d+")
        input.text = 10.toString()
        button.addActionListener {
            val steps = this.input.text.toLongOrNull()
            steps?.let {
                States.arch.get().exeMultiStep(steps)
                Events.exe.triggerEvent(States.arch.get())
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

    val skipSubroutine = CIconButton(UIStates.icon.get().stepOver).apply {
        addActionListener {
            States.arch.get().exeSkipSubroutine()
            Events.exe.triggerEvent(States.arch.get())
        }
    }
    val returnSubroutine = CIconButton(UIStates.icon.get().returnSubroutine).apply {
        addActionListener {
            States.arch.get().exeReturnFromSubroutine()
            Events.exe.triggerEvent(States.arch.get())
        }
    }
    val reset = CIconButton(UIStates.icon.get().recompile).apply {
        addActionListener {
            States.arch.get().exeReset()
            Events.exe.triggerEvent(States.arch.get())
        }
    }

    init {
        layout = GridLayout(1, 0, UIStates.scale.get().borderScale.insets, 0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        mStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        UIStates.scale.addEvent(WeakReference(this)) {
            layout = GridLayout(1, 0, it.borderScale.insets, 0)
        }

        UIStates.theme.addEvent(WeakReference(this)) {
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

        val exeStyle = UIStates.theme.get().exeStyle
        continuous.customColor = exeStyle.continuous
        singleStep.customColor = exeStyle.single
        mStep.button.customColor = exeStyle.multi
        skipSubroutine.customColor = exeStyle.skipSR
        returnSubroutine.customColor = exeStyle.returnSR
        reset.customColor = exeStyle.reassemble
    }

}