package views.components

import StyleConst
import csstype.ClassName
import extendable.components.connected.Transcript
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import react.useState

external interface TranscriptProps : Props {

    var ta_val: String
    var transcript: Transcript

}

val TranscriptView = FC<TranscriptProps> { props ->


    val ta_val = props.ta_val

    val transcript by useState(props.transcript)


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
                        for (header in transcript.getHeaders()) {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "col"
                                +header
                            }
                        }
                    }
                }
                tbody {
                    for (row in transcript.getContent()) {
                        tr {
                            for (header in transcript.getHeaders()) {
                                td {
                                    className = ClassName("dcf-txt-left")
                                    +(row.content[header] ?: "")
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}