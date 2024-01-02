package visual

import StyleAttr
import emulator.kit.optional.FlagsConditions
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import emulator.kit.Architecture
import web.html.*
import web.cssom.*

external interface FlagsCondsViewProps : Props {
    var name: String
    var archState: StateInstance<Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
}

val FlagsCondsView = FC<FlagsCondsViewProps> { props ->
    val arch = props.archState.component1()

    fun refresh(element: HTMLButtonElement, flag: FlagsConditions.Flag) {
        if (flag.getValue()) {
            element.classList.add(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
        } else {
            element.classList.remove(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
        }
    }

    fun refresh(element: HTMLButtonElement, condition: FlagsConditions.Condition) {
        if (condition.getValue()) {
            element.classList.add(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
        } else {
            element.classList.remove(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
        }
    }


    arch.getFlagsConditions()?.let {
        div {
            className = ClassName(StyleAttr.CLASS_PROC_FC_CONTAINER)
            div {
                className = ClassName(StyleAttr.CLASS_PROC_FC_CONTAINER)

                for (flag in it.flags) {
                    ReactHTML.button {
                        className = ClassName(StyleAttr.CLASS_PROC_FC_FLAG)
                        type = ButtonType.button
                        +flag.name

                        onClick = { event ->
                            it.setFlag(flag, !flag.getValue())
                            if (flag.getValue()) {
                                event.currentTarget.classList.add(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
                            } else {
                                event.currentTarget.classList.remove(StyleAttr.CLASS_PROC_FC_FLAG_ACTIVE)
                            }
                        }
                    }

                }


            }

            div {

                for (cond in it.conditions) {
                    button {
                        className = if (cond.getValue()) {
                            ClassName(StyleAttr.CLASS_PROC_FC_COND + " " + StyleAttr.CLASS_PROC_FC_COND_ACTIVE)
                        } else {
                            ClassName(StyleAttr.CLASS_PROC_FC_COND)
                        }

                        +cond.name
                    }
                }
            }


        }
    }


}