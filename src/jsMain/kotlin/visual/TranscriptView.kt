package visual

import StyleAttr
import debug.DebugTools
import emotion.react.css
import emulator.kit.assembler.gas.GASParser
import emulator.kit.types.Variable
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import web.cssom.*

external interface TranscriptProps : Props {
    var taVal: String
    var arch: StateInstance<emulator.kit.Architecture>
    var sections: StateInstance<Array<GASParser.Section>>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
}

val TranscriptView = FC<TranscriptProps> { props ->

    val arch = props.arch.component1()

    val (currExeAddr, setCurrExeAddr) = useState(Variable.Value.Hex("0"))
    val (currSections, setCurrSections) = props.sections
    val (currSec, setCurrSec) = useState<GASParser.Section>()

    fun switchCurrSection() {
        val index = currSections.indexOf(currSec)
        if (index >= 0 && index < currSections.size - 1) {
            setCurrSec(currSections.getOrNull(index + 1))
        } else {
            setCurrSec(currSections.getOrNull(0))
        }
    }

    div {

        css(ClassName(StyleAttr.Main.Editor.Transcript.CLASS)) {
            color = StyleAttr.Main.Editor.Transcript.FgColor.get()
        }

        div {
            css(ClassName(StyleAttr.Main.Editor.Transcript.CLASS_TITLE)) {
                flexBasis = 5.pct
                height = 100.pct
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
                flexDirection = FlexDirection.column
                cursor = Cursor.pointer

                a {
                    flex = 100.pct
                    padding = StyleAttr.paddingSize
                }
            }

            currSec?.name?.forEach {
                a {
                    +it
                }
            }

            onClick = {
                switchCurrSection()
            }
        }

        div {
            css(ClassName(StyleAttr.Main.Editor.Transcript.CLASS_TABLE)) {
                flex = 95.pct
                overflowY = Overflow.scroll
                maxHeight = 100.pct
                borderRadius = StyleAttr.borderRadius
                boxShadow = BoxShadow(0.px, 2.px, 2.px, rgb(0, 0, 0, 0.12))
            }

            table {
                thead {
                    tr {
                        css {
                            th {
                                background = important(StyleAttr.Main.Editor.BgColor.get())
                            }
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"addr"
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"lbls"
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"content"
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"disassembled"
                        }
                    }
                }

                // ...

                tbody {
                    currSec?.getTSContent()?.forEach { content ->
                        tr {
                            css {
                                cursor = Cursor.pointer
                                if (content.address == currExeAddr) {
                                    backgroundColor = important(StyleAttr.Main.Table.BgPC)
                                }
                            }
                            val strings = content.getAddrLblBytesTranscript()
                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_RIGHT)
                                +strings[0]
                            }
                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_RIGHT)
                                +strings[1]
                            }
                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                +strings[2]
                            }

                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                +strings[3]
                            }

                            onClick = {
                                arch.exeUntilAddress(content.address)
                                props.exeEventState.component2().invoke(!props.exeEventState.component1())
                            }
                        }
                    }
                }
            }
        }
    }

    useEffect(props.sections.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Sections Changed!")
        }
        if (props.sections.component1().isNotEmpty()) setCurrSec(currSections.getOrNull(0))
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event Changed!")
        }

        val pcValue = arch.getRegContainer().pc.variable.get()
        setCurrExeAddr(pcValue.toHex())
    }


}