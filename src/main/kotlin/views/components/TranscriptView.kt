package views.components

import AppLogic
import StyleConst
import csstype.ClassName
import extendable.ArchConst
import extendable.components.connected.Transcript
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearInterval
import kotlinx.js.timers.setInterval
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
import react.useRef
import react.useState
import tools.DebugTools

external interface TranscriptProps : Props {

    var ta_val: String
    var transcript: Transcript
    var appLogic: AppLogic

}

val TranscriptView = FC<TranscriptProps> { props ->

    val executionPointInterval = useRef<Timeout>(null)

    val appLogic by useState(props.appLogic)
    val ta_val = props.ta_val
    val transcript by useState(props.transcript)
    val (currExeAddr, setCurrExeAddr) = useState<String>("0")

    executionPointInterval.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        executionPointInterval.current = setInterval({
            val pcValue = appLogic.getArch().getRegisterContainer().pc.value.get()
            setCurrExeAddr(pcValue.toHex().getRawHexStr())
        }, 50)
    }

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
                className = ClassName("dcf-table dcf-w-100%")

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
                            if(row.memoryAddress.getRawHexStr() == currExeAddr){
                                className = ClassName("dcf-mark-green")
                            }

                            for (header in ArchConst.TranscriptHeaders.values()) {
                                td {
                                    if(header == ArchConst.TranscriptHeaders.PARAMS){
                                        className = ClassName("dcf-txt-left")
                                    }else{
                                        className = ClassName("dcf-txt-center")
                                    }

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