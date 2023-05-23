package views.components

import AppData
import csstype.ClassName
import csstype.Display
import csstype.Overflow
import csstype.vh
import emotion.react.css
import react.*
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.col
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr

external interface RegisterViewProps : Props {
    var name: String
    var appData: AppData
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppData) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val data by useState(props.appData)
    val name by useState(props.name)
    val update = props.update
    val theaders = arrayOf("Address", "Name", "Data", "Description")

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
                    for(register in data.getArch().getRegister()){
                        tr{
                            th{
                                className = ClassName("dcf-txt-center")
                                scope = "row"
                                +"${register.address}"
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

