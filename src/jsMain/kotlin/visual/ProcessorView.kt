package visual

import StorageKey
import emotion.react.css
import emulator.kit.Architecture
import web.html.*
import web.timers.*
import web.cssom.*
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import visual.ExecutionType.*

import web.html.HTMLAnchorElement
import web.html.HTMLInputElement

external interface ProcessorViewProps : Props {
    var archState: StateInstance<Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
}


val ProcessorView = FC<ProcessorViewProps> { props ->
    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()
    val executionQueue = useRef<Timeout>(null)

    val (mStepAmount, setMStepAmount) = useState(localStorage.getItem(StorageKey.MSTEP_VALUE) ?: 10)

    val (allowExe, setAllowExe) = useState(true)
    val arch = props.archState.component1()

    fun queueExecution(executionType: ExecutionType, steps: Int = 1) {
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

            ClearAll -> executionQueue.current = setTimeout({
                setAllowExe(false)
                arch.exeClear()
                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                setAllowExe(true)
            }, 0)
        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_EXE)

        div {
            css {
                color = StyleAttr.Main.Processor.FgColor.get()
                fontWeight = StyleAttr.Main.Processor.fontWeight
                fontSize = StyleAttr.Main.Processor.fontSizeTitle
                fontStyle = StyleAttr.Main.Processor.fontStyle
            }
            a {
                ref = titleRef
                +arch.getDescription().name
            }
        }

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
                        color = StyleAttr.Main.Processor.FgColor.get()
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
                                    queueExecution(MultiStep, steps = it.value.toInt())
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

            button {
                css {
                    background = StyleAttr.Main.Processor.BtnBg.CLEAR.get()
                }
                title = "Clear"
                disabled = !allowExe
                p {
                    img {
                        src = StyleAttr.Icons.delete_black
                    }
                }
                onClick = {
                    queueExecution(ClearAll)
                }
            }
        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_REG)

        RegisterView {
            this.name = "Register"
            this.archState = props.archState
            this.compileEventState = props.compileEventState
            this.exeEventState = props.exeEventState
        }

        FlagsCondsView {
            this.name = "Flags & Conditions"
            this.archState = props.archState
            this.compileEventState = props.compileEventState
            this.exeEventState = props.exeEventState

        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_MEM)

        MemoryView {
            this.name = "Memory"
            this.length = localStorage.getItem(StorageKey.MEM_LENGTH)?.toInt() ?: 4
            this.archState = props.archState
            this.compileEventState = props.compileEventState
            this.exeEventState = props.exeEventState
        }
    }

    useEffect(mStepAmount) {
        localStorage.setItem(StorageKey.MSTEP_VALUE, mStepAmount.toString())
    }
}

enum class ExecutionType {
    Continuous,
    SingleStep,
    MultiStep,
    SkipSubroutine,
    ReturnFromSubroutine,
    Reset,
    ClearAll
}