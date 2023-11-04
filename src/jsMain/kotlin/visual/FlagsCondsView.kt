package visual

import emulator.Emulator
import StyleAttr
import emulator.kit.optional.FlagsConditions
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import debug.DebugTools
import web.html.*
import web.cssom.*

external interface FlagsCondsViewProps : Props {
    var name: String
    var emulator: Emulator
    var update: Boolean
    var updateParent: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)


}

val FlagsCondsView = FC<FlagsCondsViewProps>() { props ->
    val appLogic by useState(props.emulator)
    val name by useState(props.name)
    val update by useState(props.update)

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


    appLogic.getArch().getFlagsConditions()?.let {
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
                        if (cond.getValue()) {
                            className = ClassName(StyleAttr.CLASS_PROC_FC_COND + " " + StyleAttr.CLASS_PROC_FC_COND_ACTIVE)
                        } else {
                            className = ClassName(StyleAttr.CLASS_PROC_FC_COND)
                        }

                        +cond.name
                    }
                }
            }


        }
    }
    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) FlagsCondsView")
        }
    }


}