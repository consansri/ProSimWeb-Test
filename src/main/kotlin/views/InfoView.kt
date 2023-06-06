package views

import AppLogic
import StyleConst
import csstype.ClassName
import csstype.Display
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import react.*
import react.dom.html.ButtonType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr

external interface InfoViewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit

}

data class ViewState(val key: String, val id: Int) {
    val useRefButton: MutableRefObject<HTMLButtonElement> = useRef()
    val useRefScreen: MutableRefObject<HTMLDivElement> = useRef()
    var visible = false
}

val InfoView = FC<InfoViewProps> { props ->

    val appLogic by useState(props.appLogic)

    val implInsSwitchRef = useRef<HTMLButtonElement>()
    val examplesSwitchRef = useRef<HTMLButtonElement>()

    val implInsRef = useRef<HTMLDivElement>()
    val examplesRef = useRef<HTMLDivElement>()

    val (implInsState, setImplInsState) = useState(false)
    val (examplesState, setExamplesState) = useState(false)

    div {
        className = ClassName(StyleConst.CLASS_INFOVIEW)


        div {
            className = ClassName(StyleConst.CLASS_INFOLABEL)
            a {
                +"Info"
            }
        }

        div {
            className = ClassName(StyleConst.CLASS_INFOBTNDIV)

            button {
                ref = implInsSwitchRef
                type = ButtonType.button
                +"Implemented Instructions"
                onClick = {
                    setImplInsState(!implInsState)

                }
            }
            button {
                ref = examplesSwitchRef
                type = ButtonType.button
                +"Examples"
                onClick = {
                    setExamplesState(!examplesState)

                }
            }

        }

        div {
            className = ClassName(StyleConst.CLASS_INFOSCREEN)

            if (implInsState) {
                div {
                    className = ClassName("dcf-overflow-x-auto")
                    tabIndex = 0

                    ReactHTML.table {
                        className = ClassName("dcf-table dcf-table-bordered dcf-table-striped dcf-w-100%")
                        ReactHTML.caption {
                            a {
                                +"Implemented Instructions"
                            }
                        }
                        thead {
                            tr {

                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "col"
                                    +"Name"
                                }

                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "col"
                                    +"Pseudo Code"
                                }
                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "col"
                                    +"Description"
                                }

                            }
                        }

                        tbody {

                            for (ins in appLogic.getArch().getInstructions()) {

                                ReactHTML.tr {
                                    ReactHTML.td {
                                        className = ClassName("dcf-txt-left")
                                        scope = "row"
                                        +ins.name
                                    }
                                    ReactHTML.td {
                                        className = ClassName("dcf-txt-left")
                                        +ins.pseudoCode
                                    }
                                    ReactHTML.td {
                                        className = ClassName("dcf-txt-left")
                                        +ins.description
                                    }

                                }

                            }
                        }
                    }
                }
            }

            if (examplesState) {
                div {

                    className = ClassName("dcf-overflow-x-auto")
                    tabIndex = 0

                    ReactHTML.table {
                        className = ClassName("dcf-table")
                        ReactHTML.caption {
                            a {
                                +"Architecture"
                            }
                        }

                        tbody {

                            ReactHTML.tr {
                                ReactHTML.th {
                                    className = ClassName("dcf-txt-left")
                                    scope = "row"
                                    +"Memory Address Length"
                                }
                                ReactHTML.td {
                                    className = ClassName("dcf-txt-left")
                                    +"64bit"
                                }

                            }
                        }
                    }
                }
            }
        }

    }
    useEffect(implInsState) {
        if (implInsState) {
            implInsSwitchRef.current?.classList?.add(StyleConst.CLASS_INFOACTIVE)
            setExamplesState(false)
        } else {
            implInsSwitchRef.current?.classList?.remove(StyleConst.CLASS_INFOACTIVE)
        }
    }
    useEffect(examplesState) {
        if (examplesState) {
            examplesSwitchRef.current?.classList?.add(StyleConst.CLASS_INFOACTIVE)
            setImplInsState(false)
        } else {
            examplesSwitchRef.current?.classList?.remove(StyleConst.CLASS_INFOACTIVE)
        }
    }
}