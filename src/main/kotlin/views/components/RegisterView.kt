package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.ArchConst
import react.*
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr

external interface RegisterViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update
    val theaders = ArchConst.REGISTER_HEADERS

    div {

        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 40.vh
        }

        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0

            table {
                className = ClassName("dcf-table dcf-table-bordered dcf-table-striped dcf-w-100%")
                caption {
                    +name
                }

                thead {
                    tr{
                        for(header in theaders){
                            th{
                                className = ClassName("dcf-txt-center")
                                scope = "col"
                                +header
                            }
                        }
                    }
                }

                tbody {
                    for(register in appLogic.getArch().getRegister()){
                        tr{
                            th{
                                className = ClassName("dcf-txt-center")
                                scope = "row"
                                if(register.address == ArchConst.REGISTER_NOVALUE){
                                    +"-"
                                }else{
                                    +"${register.address}"
                                }

                            }
                            td{
                                className = ClassName("dcf-txt-center")
                                +register.name
                            }
                            td{
                                className = ClassName("dcf-txt-center")
                                +"${register.value}"
                            }
                            td{
                                className = ClassName("dcf-txt-left")
                                +register.description
                            }

                        }
                    }
                }


            }

        }
    }
    useEffect(update) {
        console.log("(update) RegisterView")
    }

}
