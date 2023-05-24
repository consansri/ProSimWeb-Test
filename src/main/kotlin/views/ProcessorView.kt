package views

import AppData
import csstype.ClassName
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
import views.components.MemoryView
import views.components.RegisterView

external interface ProcessorViewProps : Props {
    var appData: AppData
    var updateParent: (newData: AppData) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
    var update: StateInstance<Boolean>
}

// CSS CLASSES
val CLASS_PROC_MAIN_CONTAINER = "processor"

// Local Storage Keys


val ProcessorView = FC<ProcessorViewProps> { props ->

    val data by useState(props.appData)
    val (change, setUpdate) = props.update
    val (mStepValue, setMStepValue) = useState<Double>()

    val titleRef = useRef<HTMLAnchorElement>()
    val mStepInputRef = useRef<HTMLInputElement>()

    div {
        className = ClassName("exeControlDiv")


        div {
            a {
                ref = titleRef
                +data.getArch().getName()
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
                    data.getArch().exeContinuous()
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
                    data.getArch().exeSingleStep()
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
                            localStorage.setItem(Consts.MSTEP_VALUE, it.currentTarget.value)
                        }

                    }
                }
                onClick = {
                    mStepInputRef.current?.let {
                        try {
                            data.getArch().exeMultiStep(it.value.toInt())
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
                    data.getArch().exeSkipSubroutines()
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
                    data.getArch().exeSubroutine()
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
                    data.getArch().exeClear()
                    setUpdate(!change)
                }
            }
        }
    }

    div {
        className = ClassName("processorDiv")

        RegisterView {
            name = "Register"
            appData = data
            update = useState(change)
            this.updateParent = props.updateParent
        }
    }

    div {
        className = ClassName("memoryDiv")

        MemoryView {
            name = "Memory"
            appData = data
            update = useState(change)
            length = localStorage.getItem(Consts.MEM_LENGTH)?.toInt() ?: 4
            this.updateParent = props.updateParent
        }

    }
    useEffect(change) {
        console.log("(update) ProcessorView")
        mStepInputRef.current?.let {
            val value = localStorage.getItem(Consts.MSTEP_VALUE) ?: ""
            it.value = value
        }
    }


}