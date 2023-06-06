package views

import AppLogic
import csstype.ClassName
import emotion.css.css
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import views.components.FlagsCondsView
import views.components.MemoryView
import views.components.RegisterView

external interface ProcessorViewProps : Props {
    var appLogic: AppLogic
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: StateInstance<Boolean>
}

// CSS CLASSES
val CLASS_PROC_MAIN_CONTAINER = "processor"

// Local Storage Keys


val ProcessorView = FC<ProcessorViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val (change, setUpdate) = props.update
    val (mStepValue, setMStepValue) = useState<Double>()

    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()

    div {
        className = ClassName("exeControlDiv")


        div {
            a {
                ref = titleRef
                +appLogic.getArch().getName()
            }
        }

        div {

            button {
                className = ClassName("button")
                id = "continuous"
                title = "Continuous Execution"
                p {
                    img {
                        src = "icons/exec/play.svg"
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
                        src = "icons/exec/singlestep.svg"
                    }
                }
                onClick = {
                    appLogic.getArch().exeSingleStep()
                    setUpdate(!change)
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
                            src = "icons/exec/play.svg"
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
                        src = "icons/exec/skip.svg"
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
                        src = "icons/exec/align-vertical-center.svg"
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
                        src = "icons/exec/clear.svg"
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
            this.updateParent = props.updateParent
        }

        FlagsCondsView{
            this.name = "Flags and Conditions"
            this.appLogic = appLogic
            this.update = useState(change)
            this.updateParent = props.updateParent
        }

    }

    div {
        className = ClassName("memoryDiv")

        MemoryView {
            this.name = "Memory"
            this.appLogic = appLogic
            this.update = useState(change)
            this.length = localStorage.getItem(StorageKey.MEM_LENGTH)?.toInt() ?: 4
            this.updateParent = props.updateParent
        }

    }
    useEffect(change) {
        console.log("(update) ProcessorView")
        mStepInputRef.current?.let {
            val value = localStorage.getItem(StorageKey.MSTEP_VALUE) ?: ""
            it.value = value
        }
    }


}