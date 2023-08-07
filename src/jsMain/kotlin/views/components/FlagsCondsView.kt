package views.components

import AppLogic
import StyleConst
import extendable.components.connected.FlagsConditions
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import tools.DebugTools
import web.html.*
import web.cssom.*

external interface FlagsCondsViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)


}

val FlagsCondsView = FC<FlagsCondsViewProps>() { props ->
    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update

    fun refresh(element: HTMLButtonElement, flag: FlagsConditions.Flag) {
        if (flag.getValue()) {
            element.classList.add(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
        } else {
            element.classList.remove(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
        }
    }

    fun refresh(element: HTMLButtonElement, condition: FlagsConditions.Condition) {
        if (condition.getValue()) {
            element.classList.add(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
        } else {
            element.classList.remove(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
        }
    }


    appLogic.getArch().getFlagsConditions()?.let {
        div {
            className = ClassName(StyleConst.CLASS_PROC_FC_CONTAINER)
            div {
                className = ClassName(StyleConst.CLASS_PROC_FC_CONTAINER)

                for (flag in it.flags) {
                    ReactHTML.button {
                        className = ClassName(StyleConst.CLASS_PROC_FC_FLAG)
                        type = ButtonType.button
                        +flag.name

                        onClick = { event ->
                            it.setFlag(flag, !flag.getValue())
                            if (flag.getValue()) {
                                event.currentTarget.classList.add(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
                            } else {
                                event.currentTarget.classList.remove(StyleConst.CLASS_PROC_FC_FLAG_ACTIVE)
                            }
                        }
                    }

                }


            }

            div {

                for (cond in it.conditions) {
                    button {
                        if (cond.getValue()) {
                            className = ClassName(StyleConst.CLASS_PROC_FC_COND + " " + StyleConst.CLASS_PROC_FC_COND_ACTIVE)
                        } else {
                            className = ClassName(StyleConst.CLASS_PROC_FC_COND)
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