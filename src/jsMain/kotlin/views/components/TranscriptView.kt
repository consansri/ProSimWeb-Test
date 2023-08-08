package views.components

import AppLogic
import StyleConst
import emotion.react.css
import extendable.ArchConst
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
import react.useRef
import react.useState
import tools.DebugTools

import web.timers.*
import web.cssom.*

external interface TranscriptProps : Props {
    var ta_val: String
    var transcript: Transcript
    var appLogic: AppLogic
}

val TranscriptView = FC<TranscriptProps> { props ->

    val executionPointInterval = useRef<Timeout>(null)

    val appLogic by useState(props.appLogic)
    val transcript by useState(props.transcript)
    val (currExeAddr, setCurrExeAddr) = useState("0")

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

        css(ClassName(StyleConst.Main.Editor.Transcript.CLASS)) {
            color = StyleConst.Main.Editor.Transcript.FgColor.get()
        }

        div {
            css(ClassName(StyleConst.Main.Editor.Transcript.CLASS_TITLE)) {
                flexBasis = 5.pct
                height = 100.pct
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
                flexDirection = FlexDirection.column

                a {
                    flex = 100.pct
                    padding = StyleConst.paddingSize
                }
            }
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
            css(ClassName(StyleConst.Main.Editor.Transcript.CLASS_TABLE)) {
                flex = 95.pct
                overflowY = Overflow.scroll
                maxHeight = 100.pct
                borderRadius = StyleConst.borderRadius
                boxShadow = BoxShadow(0.px, 2.px, 2.px, rgb(0, 0, 0, 0.12))
            }

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
                            if (row.memoryAddress.getRawHexStr() == currExeAddr) {
                                className = ClassName("dcf-mark-green")
                            }

                            for (header in ArchConst.TranscriptHeaders.entries) {
                                if (header == ArchConst.TranscriptHeaders.PARAMS) {
                                    val content = row.content[header]
                                    content?.let {
                                        for (param in it.split(ArchConst.TRANSCRIPT_PARAMSPLIT)) {
                                            td {
                                                className = ClassName("dcf-txt-left")
                                                +param
                                            }
                                        }
                                    }
                                } else {
                                    td {
                                        className = ClassName("dcf-txt-center")
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
}