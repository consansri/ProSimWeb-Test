package visual

import Keys
import StyleAttr
import emotion.react.css
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import visual.ExecutionType.*
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.important
import web.html.HTMLInputElement
import web.html.InputType
import web.timers.Timeout
import web.timers.setTimeout

external interface ProcessorViewProps : Props {
    var archState: StateInstance<emulator.kit.Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
}


val ProcessorView = FC<ProcessorViewProps> { props ->
    val mStepInputRef = useRef<HTMLInputElement>()
    val executionQueue = useRef<Timeout>(null)

    val (mStepAmount, setMStepAmount) = useState(localStorage.getItem(Keys.MSTEP_VALUE) ?: 10)

    val (allowExe, setAllowExe) = useState(true)
    val hideRegDescr = useState(false)
    val arch = props.archState.component1()

    fun queueExecution(executionType: ExecutionType, steps: Long = 1) {
        when (executionType) {
            Continuous -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeContinuous()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)

            SingleStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeSingleStep()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)

            MultiStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeMultiStep(steps)
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)

            SkipSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeSkipSubroutine()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)

            ReturnFromSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeReturnFromSubroutine()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)

            Reset -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeReset()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)
        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_EXE)

        div {
            css {
                color = StyleAttr.Main.FgColor.get()
            }
            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.CONTINUOUS.get()
                }
                title = "Continuous Execution"
                disabled = !allowExe

                img {
                    src = StyleAttr.Icons.continuous_exe
                }

                onClick = {
                    queueExecution(Continuous)
                }
            }

            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.SSTEP.get()
                }
                title = "Single Step"
                disabled = !allowExe

                p {
                    img {
                        src = StyleAttr.Icons.single_exe
                    }
                }
                onClick = {
                    queueExecution(SingleStep)
                }
            }

            span {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.MSTEP.get()
                }
                title = "Multi Step"

                input {
                    ref = mStepInputRef
                    placeholder = "steps"
                    type = InputType.number
                    name = "Steps"
                    min = 1.0
                    step = 1.0

                    value = mStepAmount

                    css {
                        background = StyleAttr.Main.Processor.BgColorTransparent.get()
                        color = important(Color("#FFF"))
                    }

                    onChange = {
                        setMStepAmount(it.currentTarget.valueAsNumber.toInt())
                    }
                }

                p {
                    img {
                        src = StyleAttr.Icons.step_multiple
                    }

                    onClick = {
                        if (allowExe) {
                            mStepInputRef.current?.let {
                                try {
                                    queueExecution(MultiStep, steps = it.value.toLong())
                                } catch (e: NumberFormatException) {
                                    console.log("(info) steps input value isn't valid!")
                                }
                            }
                        }
                    }
                }
            }

            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.SOVER.get()
                }
                title = "Skip Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = StyleAttr.Icons.step_over
                    }
                }
                onClick = {
                    queueExecution(SkipSubroutine)
                }
            }

            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.ESUB.get()
                }
                title = "Return From Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = StyleAttr.Icons.return_subroutine
                    }
                }
                onClick = {
                    queueExecution(ReturnFromSubroutine)
                }
            }

            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.RESET.get()
                }
                title = "Reset and Recompile"
                disabled = !allowExe
                p {
                    img {
                        src = StyleAttr.Icons.recompile
                    }
                }
                onClick = {
                    queueExecution(Reset)
                }
            }
        }
    }

    /* div {
         css {
             color = StyleAttr.Main.FgColor.get()
             fontWeight = StyleAttr.Main.Processor.fontWeight
             fontSize = StyleAttr.Main.Processor.fontSizeTitle
             fontStyle = StyleAttr.Main.Processor.fontStyle
         }
         a {
             ref = titleRef
             +arch.getDescription().name
         }
     }*/

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_REG)

        RegisterView {
            this.key = "reg1"
            this.name = "Reg1"
            this.archState = props.archState
            this.compileEventState = props.compileEventState
            this.exeEventState = props.exeEventState
            this.hideAdditionalInfo = hideRegDescr
            this.isFirst = true
        }

        if (hideRegDescr.component1()) {
            RegisterView {
                this.key = "reg2"
                this.name = "Reg2"
                this.archState = props.archState
                this.compileEventState = props.compileEventState
                this.exeEventState = props.exeEventState
                this.hideAdditionalInfo = hideRegDescr
                this.isFirst = false
            }
        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_MEM)

        MemoryView {
            this.name = "Memory"
            this.length = localStorage.getItem(Keys.MEM_LENGTH)?.toInt() ?: 4
            this.archState = props.archState
            this.compileEventState = props.compileEventState
            this.exeEventState = props.exeEventState
            this.hideRegDescr = hideRegDescr
        }
    }

    useEffect(mStepAmount) {
        localStorage.setItem(Keys.MSTEP_VALUE, mStepAmount.toString())
    }
}

enum class ExecutionType {
    Continuous,
    SingleStep,
    MultiStep,
    SkipSubroutine,
    ReturnFromSubroutine,
    Reset
}