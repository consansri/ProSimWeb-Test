package views

import AppLogic
import csstype.ClassName
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import react.*
import react.dom.aria.AriaRole
import react.dom.aria.ariaValueMax
import react.dom.aria.ariaValueMin
import react.dom.aria.ariaValueNow
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import tools.DebugTools
import views.components.FlagsCondsView
import views.components.MemoryView
import views.components.RegisterView

external interface ProcessorViewProps : Props {
    var appLogic: AppLogic
    var updateAppLogic: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: StateInstance<Boolean>
}


val ProcessorView = FC<ProcessorViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val (change, setUpdate) = props.update
    val (mStepValue, setMStepValue) = useState<Double>()
    val (progress, setProgress) = useState<Double>(0.0)

    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()
    val progressBarRef = useRef<HTMLDivElement>()
    val progressPCRef = useRef<HTMLParagraphElement>()

    val (currRegIndex, setCurrRegIndex) = useState(1)

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
            p {
                ref = progressPCRef
            }
        }

        div {
            div {
                className = ClassName(StyleConst.CLASS_EXEC_PROGRESS)
                div {
                    ref = progressBarRef

                    className = ClassName(StyleConst.CLASS_EXEC_PROGRESS_BAR)
                    role = AriaRole.progressbar
                    ariaValueNow = 10.0
                    ariaValueMin = 0.0
                    ariaValueMax = 100.0

                }
            }
        }

        div {

            button {
                className = ClassName("button")
                id = "continuous"
                title = "Continuous Execution"
                p {
                    img {
                        src = "benicons/exec/continuous-exe.svg"
                    }

                }

                onClick = {
                    appLogic.getArch().exeContinuous()
                    setUpdate(!change)
                }
            }

            button {
                className = ClassName("button")
                id = "sstep"
                title = "Single Step"
                p {
                    img {
                        src = "benicons/exec/single_exe.svg"
                    }
                }
                onClick = {
                    appLogic.getArch().exeSingleStep()
                    setUpdate(!change)
                    setProgress(progress + 21.23)
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
                    mStepInputRef.current?.let {
                        try {
                            appLogic.getArch().exeMultiStep(it.value.toInt())
                        } catch (e: NumberFormatException) {
                            console.log("(info) steps input value isn't valid!")
                        }
                    }
                    setUpdate(!change)
                }
            }



            button {
                className = ClassName("button")
                id = "sover"
                title = "Skip Subroutines"
                p {
                    img {
                        src = "benicons/exec/step_over.svg"
                    }
                }
                onClick = {
                    appLogic.getArch().exeSkipSubroutines()
                    setUpdate(!change)
                }
            }
            button {
                className = ClassName("button")
                id = "esub"
                title = "Execute Subroutine"
                p {
                    img {
                        src = "benicons/exec/step_into.svg"
                    }
                }
                onClick = {
                    appLogic.getArch().exeSubroutine()
                    setUpdate(!change)
                }
            }

            button {
                className = ClassName("button")
                id = "reset"
                title = "Reset"
                p {
                    img {
                        src = StyleConst.Icons.delete
                    }
                }
                onClick = {
                    appLogic.getArch().exeClear()
                    setUpdate(!change)
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

    useEffect(progress) {
        if (progress in 0.0..100.0) {
            progressPCRef.current?.let {
                it.innerText = "${progress}%"
            }
            progressBarRef.current?.let {
                it.style.width = "${progress}%"
            }
        } else {
            if (progress > 100.0) {
                setProgress(100.0)
            } else {
                setProgress(0.0)
            }
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