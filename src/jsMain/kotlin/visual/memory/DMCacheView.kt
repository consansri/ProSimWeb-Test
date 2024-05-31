package visual.memory

import StyleAttr
import debug.DebugTools
import emotion.react.css
import emulator.kit.Architecture
import emulator.kit.common.memory.Cache
import emulator.kit.common.memory.DirectMappedCache
import emulator.kit.common.memory.MainMemory
import emulator.kit.common.memory.Memory
import emulator.kit.types.Variable
import react.*
import react.dom.html.ReactHTML
import visual.StyleExt.get
import web.cssom.*
import web.html.HTMLElement
import web.html.HTMLTableSectionElement

external interface DMCacheViewProps : Props {
    var exeEventState: StateInstance<Boolean>
    var archState: StateInstance<Architecture>
    var cache: DirectMappedCache
}

val DMCacheView = FC<DMCacheViewProps>() { props ->

    val tbody = useRef<HTMLTableSectionElement>()
    val asciiRef = useRef<HTMLElement>()

    val (rowList, setRowList) = useState(props.cache.block.data)
    val (currExeAddr, setCurrExeAddr) = useState<String>()

    ReactHTML.div {
        css {
            display = Display.block
            overflowY = Overflow.scroll
            flexGrow = number(1.0)
            borderRadius = StyleAttr.borderRadius
            paddingLeft = 12.px // center with scrollbar on the right
        }
        tabIndex = 0

        ReactHTML.table {
            css {
                minHeight = 100.pct
            }
            ReactHTML.thead {
                ReactHTML.tr {
                    css {
                        ReactHTML.th {
                            background = important(StyleAttr.Main.Processor.TableBgColor.get())
                        }
                    }

                    ReactHTML.th {
                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                        scope = "col"
                        +"i"
                    }

                    ReactHTML.th {
                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                        scope = "col"
                        +"v"
                    }

                    ReactHTML.th {
                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                        scope = "col"
                        +"d"
                    }

                    ReactHTML.th {
                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                        scope = "col"
                        +"tag"
                    }

                    for (columnID in 0..<props.cache.offsets) {
                        ReactHTML.th {
                            /* className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)*/
                            css {
                                textAlign = TextAlign.center
                                width = 4.ch
                            }
                            scope = "col"
                            +"$columnID"
                        }
                    }

                    ReactHTML.th {
                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                        scope = "col"
                        +"ASCII"
                    }
                }
            }

            ReactHTML.tbody {
                ref = tbody

                if (DebugTools.REACT_showUpdateInfo) {
                    console.log("REACT: Memory Map Updated!")
                }

                for (rowID in rowList.indices) {
                    val row = rowList[rowID] as? DirectMappedCache.DMRow ?: continue
                    val state = row.getRowState()
                    ReactHTML.tr {
                        css{
                            color = row.getRowState().get(StyleAttr.mode)
                        }
                        ReactHTML.th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "row"
                            +rowID.toString(16)
                        }

                        ReactHTML.td {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "row"
                            +if(row.valid) "1" else "0"
                        }

                        ReactHTML.td {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "row"
                            +if(row.dirty) "1" else "0"
                        }

                        ReactHTML.td {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "row"
                            +(row.tag?.toHex()?.toRawZeroTrimmedString() ?: "invalid")
                        }

                        row.data.forEach {
                            ReactHTML.td {
                                css {
                                    textAlign = TextAlign.center
                                    if (it.address?.getRawHexStr() == currExeAddr) {
                                        color = important(StyleAttr.Main.Table.FgPC)
                                        fontWeight = important(FontWeight.bold)
                                    } else {
                                        color = important(it.mark.get(StyleAttr.mode))
                                    }
                                }

                                //id = "mem${memInstance.address.getRawHexStr()}"
                                title = "value = ${it.value.toDec()} or ${it.value.toUDec()}"

                                +it.value.toHex().toRawZeroTrimmedString()
                            }
                        }

                        ReactHTML.td {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER + " " + StyleAttr.Main.Table.CLASS_MONOSPACE)
                            ref = asciiRef

                            +row.data.joinToString("") { it.value.toASCII() }
                        }
                    }
                }
                ReactHTML.tr {
                    ReactHTML.td {
                        colSpan = props.cache.offsets + 4
                        css {
                            paddingTop = 15.rem
                            paddingBottom = 15.rem
                        }
                        +"Direct Mapped Cache"
                    }
                }
            }
        }
    }

    /*useEffect(props.archState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Arch Changed!")
        }
    }*/

    useEffect(props.cache.block.data) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Cache Data Changed!")
        }
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().regContainer.pc.variable.get().toHex().getRawHexStr())
        setRowList(props.cache.block.data)
    }

}