package views.components

import StyleConst
import csstype.ClassName
import react.FC
import react.Props
import react.StateInstance
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr

external interface TranscriptProps : Props {

    var ta_val: String

}

val TranscriptView = FC<TranscriptProps> { props ->


    val ta_val = props.ta_val



    div {
        className = ClassName(StyleConst.CLASS_TRANSCRIPT)


        div {
            className = ClassName(StyleConst.CLASS_TRANSCRIPT_TITLE)
            a {
                +"T"
            }
            a {
                +"R"
            }
            a {
                +"A"
            }
            a {
                +"N"
            }
            a {
                +"S"
            }
            a {
                +"C"
            }
            a {
                +"R"
            }
            a {
                +"I"
            }
            a {
                +"P"
            }
            a {
                +"T"
            }
        }


        div {
            className = ClassName(StyleConst.CLASS_TRANSCRIPT_TABLE)

            table {
                className = ClassName("dcf-table dcf-table-striped dcf-w-100%")

                thead {
                    tr {
                        th {
                            className = ClassName("dcf-txt-center")
                            scope = "col"
                            +"Address"

                        }
                        th {
                            className = ClassName("dcf-txt-center")
                            scope = "col"
                            +"Code"

                        }
                        th {
                            className = ClassName("dcf-txt-center")
                            scope = "col"
                            +"Labels"

                        }
                        th {
                            className = ClassName("dcf-txt-center")
                            scope = "col"
                            +"Instruction"

                        }
                    }
                }
                tbody {
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                    tr {
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Addresse"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Code"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Labels"
                        }
                        td {
                            className = ClassName("dcf-txt-left")
                            +"Instruction"
                        }
                    }
                }


            }

        }


    }


}