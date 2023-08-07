package views

import AppLogic
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

            ExecutionType.ClearAll -> executionQueue.current = setTimeout({
                setAllowExe(false)
                appLogic.getArch().exeClear()
                setUpdate(!change)
                setAllowExe(true)
            }, 0)
        }
    }

    div {
        className = ClassName("exeControlDiv")

        div {
            a {
                ref = titleRef
                id = "arch-title"
                +appLogic.getArch().getName()
            }
        }

        div {

            button {
                className = ClassName("button")
                id = "continuous"
                title = "Continuous Execution"
                disabled = !allowExe
                p {
                    img {
                        src = "benicons/exec/continuous-exe.svg"
                    }
                }

                onClick = {
                    queueExecution(Continuous)
                }
            }

            button {
                className = ClassName("button")
                id = "sstep"
                title = "Single Step"
                disabled = !allowExe

                p {
                    img {
                        src = "benicons/exec/single_exe.svg"
                    }
                }
                onClick = {
                    queueExecution(SingleStep)
                }
            }

            span {
                className = ClassName("input-button")
                id = "mstep"
                title = "Multi Step"
                a {
                    href = "#"

                    p {
                        img {
                            src = "benicons/exec/step_multiple.svg"
                        }
                    }

                    input {
                        ref = mStepInputRef
                        placeholder = "Steps"
                        type = InputType.number
                        name = "Steps"
                        min = 1.0
                        step = 1.0

                        onChange = {
                            localStorage.setItem(StorageKey.MSTEP_VALUE, it.currentTarget.value)
                        }

                    }
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

            button {
                className = ClassName("button")
                id = "sover"
                title = "Skip Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = "benicons/exec/step_over.svg"
                    }
                }
                onClick = {
                    queueExecution(SkipSubroutine)
                }
            }

            button {
                className = ClassName("button")
                id = "esub"
                title = "Return From Subroutine"
                disabled = !allowExe
                p {
                    img {
                        src = "benicons/exec/step_into.svg"
                    }
                }
                onClick = {
                    queueExecution(ReturnFromSubroutine)
                }
            }

            button {
                className = ClassName("button")
                id = "reset"
                title = "Reset"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.backwards
                    }
                }
                onClick = {
                    queueExecution(Reset)
                }
            }

            button {
                className = ClassName("button")
                id = "clear"
                title = "Clear"
                disabled = !allowExe
                p {
                    img {
                        src = StyleConst.Icons.delete
                    }
                }
                onClick = {
                    queueExecution(ExecutionType.ClearAll)
                }
            }
        }
    }

    div {
        className = ClassName("processorDiv")

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
        className = ClassName("memoryDiv")

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