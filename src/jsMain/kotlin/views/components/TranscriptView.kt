package views.components

import AppLogic
import StyleConst
import emotion.react.css
import extendable.components.connected.Transcript
import extendable.components.types.MutVal
import react.FC
import react.Props
import react.dom.aria.ariaRowSpan
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
    var updateParent: () -> Unit
}

val TranscriptView = FC<TranscriptProps> { props ->

    val executionPointInterval = useRef<Timeout>(null)

    val appLogic by useState(props.appLogic)
    val transcript by useState(props.transcript)
    val (currExeAddr, setCurrExeAddr) = useState<MutVal.Value.Hex>(MutVal.Value.Hex("0"))
    val (currType, setCurrType) = useState<Transcript.Type>(Transcript.Type.DISASSEMBLED)

    executionPointInterval.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        executionPointInterval.current = setInterval({
            val pcValue = appLogic.getArch().getRegisterContainer().pc.value.get()
            setCurrExeAddr(pcValue.toHex())
        }, 50)
    }

    fun switchCurrType() {
        val currIndex = currType.ordinal
        if (currIndex < Transcript.Type.entries.size - 1) {
            setCurrType(Transcript.Type.entries[currIndex + 1])
        } else {
            setCurrType(Transcript.Type.entries.first())
        }
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
                cursor = Cursor.pointer

                a {
                    flex = 100.pct
                    padding = StyleConst.paddingSize
                }
            }

            currType.name.forEach {
                a {
                    +it
                }
            }

            onClick = {
                switchCurrType()
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

                thead {

                    tr {
                        css {
                            th {
                                background = important(StyleConst.Main.Editor.BgColor.get())
                            }
                        }
                        for (header in transcript.getHeaders(currType)) {
                            th {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                scope = "col"
                                +header
                            }
                        }
                    }
                }
                tbody {
                    for (row in transcript.getContent(currType)) {
                        tr {
                            css {
                                cursor = Cursor.pointer
                                for (address in row.getAddresses()) {
                                    if (address == currExeAddr) {
                                        backgroundColor = important(StyleConst.Main.Table.BgPC)
                                    }
                                }
                            }

                            for (entry in row.getContent()) {
                                td {
                                    className = when (entry.orientation) {
                                        Transcript.Row.Orientation.LEFT -> ClassName(StyleConst.Main.Table.CLASS_TXT_LEFT)
                                        Transcript.Row.Orientation.CENTER -> ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                        Transcript.Row.Orientation.RIGHT -> ClassName(StyleConst.Main.Table.CLASS_TXT_RIGHT)
                                    }
                                    +entry.content
                                }
                            }

                            onClick = {
                                row.getAddresses().firstOrNull()?.let {
                                    appLogic.getArch().exeUntilAddress(it.toHex())
                                    props.updateParent()
                                }
                            }
                        }
                        repeat(row.getHeight() - 1) {
                            tr {
                                css {
                                    cursor = Cursor.pointer
                                    for (address in row.getAddresses()) {
                                        if (address == currExeAddr) {
                                            backgroundColor = important(StyleConst.Main.Table.BgPC)
                                        }
                                    }
                                }

                                repeat(row.getContent().size) {
                                    td {
                                        +" "
                                    }
                                }

                                onClick = {
                                    row.getAddresses().firstOrNull()?.let {
                                        appLogic.getArch().exeUntilAddress(it.toHex())
                                        props.updateParent()
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