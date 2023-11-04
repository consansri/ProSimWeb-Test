package visual

import emulator.Emulator
import StorageKey
import emotion.react.css
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
import debug.DebugTools
import visual.ExecutionType.*

import web.html.HTMLAnchorElement
import web.html.HTMLInputElement

external interface ProcessorViewProps : Props {
    var emulator: Emulator
    var updateAppLogic: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: StateInstance<Boolean>
}


val ProcessorView = FC<ProcessorViewProps> { props ->

    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()
    val executionQueue = useRef<Timeout>(null)

    val (mStepAmount, setMStepAmount) = useState(localStorage.getItem(StorageKey.MSTEP_VALUE) ?: 10)

    val iUpdate = useState(false)
    val (allowExe, setAllowExe) = useState(true)
    val appLogic by useState(props.emulator)
    val (change, setUpdate) = props.update

    fun queueExecution(executionType: ExecutionType, steps: Int = 1) {
        when (executionType) {
            Continuous -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeContinuous()
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            SingleStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeSingleStep()
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            MultiStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeMultiStep(steps)
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            SkipSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeSkipSubroutine()
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            ReturnFromSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeReturnFromSubroutine()
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            Reset -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeReset()
                iUpdate.component2().invoke(!iUpdate.component1())
                setAllowExe(true)
            }, 0)

            ClearAll -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeClear()
                iUpdate.component2().invoke(!iUpdate.component1())
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
                +appLogic.getArch().getDescription().name
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
            this.emulator = appLogic
            this.update = iUpdate
            this.updateParent = props.updateAppLogic
        }

        FlagsCondsView {
            this.name = "Flags & Conditions"
            this.emulator = appLogic
            this.update = iUpdate
            this.updateParent = props.updateAppLogic
        }
    }

    div {
        className = ClassName(StyleAttr.Main.Processor.CLASS_MEM)

        MemoryView {
            this.name = "Memory"
            this.emulator = appLogic
            this.update = iUpdate
            this.length = localStorage.getItem(StorageKey.MEM_LENGTH)?.toInt() ?: 4
            this.updateParent = props.updateAppLogic
        }
    }

    useEffect(change) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) ProcessorView")
        }
        iUpdate.component2().invoke(!iUpdate.component1())
        mStepInputRef.current?.let {
            val value = localStorage.getItem(StorageKey.MSTEP_VALUE) ?: ""
            it.value = value
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