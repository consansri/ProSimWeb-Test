package views

import AppLogic
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
import tools.DebugTools
import views.ExecutionType.*
import views.components.FlagsCondsView
import views.components.MemoryView
import views.components.RegisterView

import web.html.HTMLAnchorElement
import web.html.HTMLInputElement

external interface ProcessorViewProps : Props {
    var appLogic: AppLogic
    var updateAppLogic: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: StateInstance<Boolean>
}


val ProcessorView = FC<ProcessorViewProps> { props ->

    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()
    val executionQueue = useRef<Timeout>(null)

    val (mStepAmount, setMStepAmount) = useState(localStorage.getItem(StorageKey.MSTEP_VALUE) ?: 10)

    val (allowExe, setAllowExe) = useState(true)
    val appLogic by useState(props.appLogic)
    val (change, setUpdate) = props.update

    fun queueExecution(executionType: ExecutionType, steps: Int = 1) {
        when (executionType) {
            Continuous -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeContinuous()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            SingleStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeSingleStep()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            MultiStep -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeMultiStep(steps)
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            SkipSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeSkipSubroutine()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            ReturnFromSubroutine -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeReturnFromSubroutine()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            Reset -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeReset()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)

            ClearAll -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeClear()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)
        }
    }

    div {
        className = ClassName(StyleConst.Main.Processor.CLASS_EXE)

        div {
            css {
                color = StyleConst.Main.Processor.FgColor.get()
                fontWeight = StyleConst.Main.Processor.fontWeight
                fontSize = StyleConst.Main.Processor.fontSizeTitle
                fontStyle = StyleConst.Main.Processor.fontStyle
            }
            a {
                ref = titleRef
                +appLogic.getArch().getName()
            }
        }

        div {
            css {
                color = StyleConst.Main.Processor.FgColor.get()
            }
            button {
                css {
                    background = StyleConst.Main.Processor.BtnBg.CONTINUOUS.get()
                }
                title = "Continuous Execution"
                disabled = !allowExe

                img {
                    src = StyleConst.Icons.continuous_exe
                }

                onClick = {
                    queueExecution(Continuous)
                }
            }

            button {
                css {
                    background = StyleConst.Main.Processor.BtnBg.SSTEP.get()
                }
                title = "Single Step"
                disabled = !allowExe

                p {
                    img {
                        src = StyleConst.Icons.single_exe
                    }
                }
                onClick = {
                    queueExecution(SingleStep)
                }
            }

            span {
                css {
                    background = StyleConst.Main.Processor.BtnBg.MSTEP.get()
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
                        background = StyleConst.Main.Processor.BgColorTransparent.get()
                        color = StyleConst.Main.Processor.FgColor.get()
                    }

                    onChange = {
                        setMStepAmount(it.currentTarget.valueAsNumber.toInt())
                    }
                }

                p {
                    img {
                        src = StyleConst.Icons.step_multiple
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
                    background = StyleConst.Main.Processor.BtnBg.SOVER.get()
                }
                title = "Skip Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.step_over
                    }
                }
                onClick = {
                    queueExecution(SkipSubroutine)
                }
            }

            button {
                css {
                    background = StyleConst.Main.Processor.BtnBg.ESUB.get()
                }
                title = "Return From Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.return_subroutine
                    }
                }
                onClick = {
                    queueExecution(ReturnFromSubroutine)
                }
            }

            button {
                css {
                    background = StyleConst.Main.Processor.BtnBg.RESET.get()
                }
                title = "Reset and Recompile"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.recompile
                    }
                }
                onClick = {
                    queueExecution(Reset)
                }
            }

            button {
                css {
                    background = StyleConst.Main.Processor.BtnBg.CLEAR.get()
                }
                title = "Clear"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.delete_black
                    }
                }
                onClick = {
                    queueExecution(ClearAll)
                }
            }
        }
    }

    div {
        className = ClassName(StyleConst.Main.Processor.CLASS_REG)

        RegisterView {
            this.name = "Register"
            this.appLogic = appLogic
            this.update = useState(change)
            this.updateParent = props.updateAppLogic
        }

        FlagsCondsView {
            this.name = "Flags & Conditions"
            this.appLogic = appLogic
            this.update = useState(change)
            this.updateParent = props.updateAppLogic
        }
    }

    div {
        className = ClassName(StyleConst.Main.Processor.CLASS_MEM)

        MemoryView {
            this.name = "Memory"
            this.appLogic = appLogic
            this.update = useState(change)
            this.length = localStorage.getItem(StorageKey.MEM_LENGTH)?.toInt() ?: 4
            this.updateParent = props.updateAppLogic
        }
    }

    useEffect(change) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) ProcessorView")
        }
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