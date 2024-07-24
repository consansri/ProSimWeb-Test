package prosim.ui.components.processor

import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.theme.core.Theme
import java.awt.GridLayout
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class ExecutionControls() : CPanel(primary = false, BorderMode.SOUTH) {

    val scaleListener = object : StateListener<Scaling> {
        override suspend fun onStateChange(newVal: Scaling) {
            layout = GridLayout(1, 0, newVal.SIZE_INSET_MEDIUM, 0)
        }
    }

    val themeListener = object : StateListener<Theme> {
        override suspend fun onStateChange(newVal: Theme) {
            continuous.customColor = newVal.COLOR_GREEN
            singleStep.customColor = newVal.COLOR_GREEN_LIGHT
            mStep.button.customColor = newVal.COLOR_YELLOW
            skipSubroutine.customColor = newVal.COLOR_BLUE
            returnSubroutine.customColor = newVal.COLOR_ORANGE
            reset.customColor = newVal.COLOR_RED
        }
    }

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
        layout = GridLayout(1, 0, UIStates.scale.get().SIZE_INSET_MEDIUM, 0)

        continuous.alignmentY = CENTER_ALIGNMENT
        singleStep.alignmentY = CENTER_ALIGNMENT
        mStep.alignmentY = CENTER_ALIGNMENT
        skipSubroutine.alignmentY = CENTER_ALIGNMENT
        returnSubroutine.alignmentY = CENTER_ALIGNMENT
        reset.alignmentY = CENTER_ALIGNMENT

        // Listeners
        UIStates.scale.addEvent(scaleListener)
        UIStates.theme.addEvent(themeListener)

        add(continuous)
        add(singleStep)
        add(mStep)
        add(skipSubroutine)
        add(returnSubroutine)
        add(reset)

        val theme = UIStates.theme.get()
        continuous.customColor = theme.COLOR_GREEN
        singleStep.customColor = theme.COLOR_GREEN_LIGHT
        mStep.button.customColor = theme.COLOR_YELLOW
        skipSubroutine.customColor = theme.COLOR_BLUE
        returnSubroutine.customColor = theme.COLOR_ORANGE
        reset.customColor = theme.COLOR_RED
    }

}