package visual.memory

import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.common.memory.Cache
import react.*
import visual.virtual.VirtualTable
import web.html.HTMLDivElement
import web.html.HTMLElement
import web.html.HTMLTableSectionElement

external interface CacheViewProps : Props {
    var exeEventState: StateInstance<Boolean>
    var archState: StateInstance<Architecture>
    var cache: Cache
}

val CacheView = FC<CacheViewProps>() { props ->

    val containerRef = useRef<HTMLDivElement>()
    val tbody = useRef<HTMLTableSectionElement>()
    val asciiRef = useRef<HTMLElement>()

    val (rowList, setRowList) = useState(props.cache.model.rows)
    val (currExeAddr, setCurrExeAddr) = useState<String>()

    VirtualTable {
        this.headers = arrayOf("i", "m", "v", "d", "tag", *Array(props.cache.model.offsetCount) { it.toString(16) }, "ascii")
        this.colCount = props.cache.model.offsetCount + 6
        this.rowCount = props.cache.model.rows.size * props.cache.model.blockCount
        this.visibleRows = 8
        this.cellContent = { rowID ->
            val rowIndex = rowID / props.cache.model.blockCount
            val blockIndex = rowID % props.cache.model.blockCount
            val block = props.cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex)

            arrayOf(
                rowIndex.toString(16),
                blockIndex.toString(16),
                if (block?.valid == true) "1" else "0",
                if (block?.dirty == true) "1" else "0",
                block?.tag?.toHex()?.toRawZeroTrimmedString() ?: "invalid",
                *block?.data?.map { it.value.toHex().toRawZeroTrimmedString() }?.toTypedArray() ?: arrayOf(),
                block?.data?.joinToString("") { it.value.toASCII() } ?: ""
            )
        }
    }
/*

    ReactHTML.div {
        css {
            display = Display.block
            overflowY = Overflow.scroll
            flexGrow = number(1.0)
            borderRadius = StyleAttr.borderRadius
            paddingLeft = StyleAttr.scrollBarSize // center with scrollbar on the right
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
                        +"m"
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

                    for (columnID in 0..<props.cache.model.offsetCount) {
                        ReactHTML.th {
                            */
/* className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)*//*

                            css {
                                textAlign = TextAlign.center
                                width = 4.ch
                            }
                            scope = "col"
                            +columnID.toString(16)
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

                rowList.forEachIndexed { rowID, row ->

                    row.blocks.forEachIndexed { blockID, block ->
                        ReactHTML.tr {
                            css {
                                color = block.getState().get(StyleAttr.mode)
                            }
                            th {
                                if (blockID == 0) {
                                    +rowID.toString(16)
                                }
                            }
                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +blockID.toString(16)
                            }

                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +if (block.valid) "1" else "0"
                            }

                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +if (block.dirty) "1" else "0"
                            }

                            ReactHTML.td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +(block.tag?.toHex()?.toRawZeroTrimmedString() ?: "invalid")
                            }

                            block.data.forEach {
                                ReactHTML.td {
                                    css {
                                        textAlign = TextAlign.center
                                        if (it.address?.getRawHexStr() == currExeAddr) {
                                            color = important(StyleAttr.Main.Table.FgPC)
                                            fontWeight = important(FontWeight.bold)
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

                                +block.data.joinToString("") { it.value.toASCII() }
                            }
                        }
                    }
                }
            }
        }
    }
*/

    useEffect(props.cache.model.rows) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Cache Data Changed!")
        }
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().regContainer.pc.variable.get().toHex().getRawHexStr())
    }
}