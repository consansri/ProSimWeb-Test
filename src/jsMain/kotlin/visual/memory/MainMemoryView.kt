package visual.memory

import Settings
import StyleAttr
import cengine.util.integer.Hex
import debug.DebugTools
import emotion.react.css
import emulator.kit.Architecture
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import react.*
import react.dom.html.ReactHTML
import visual.StyleExt.get
import web.cssom.*
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLTableSectionElement
import web.html.InputType

external interface MainMemViewProps : Props{
    var archState: StateInstance<Architecture>
    var exeEventState: StateInstance<Boolean>
    var memory: MainMemory
    var lowFirst: Boolean
}


val MainMemoryView = FC<MainMemViewProps> {props ->

    val tbody = useRef<HTMLTableSectionElement>()
    val asciiRef = useRef<HTMLElement>()
    val editRef = useRef<HTMLInputElement>()

    val (memList, setMemList) = useState(props.memory.memList)
    val (currExeAddr, setCurrExeAddr) = useState<String>()
    val (editVar, setEditVar) = useState<MainMemory.MemInstance.EditableValue>()

    ReactHTML.div {
        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 30.vh
            flexGrow = number(1.0)
            borderRadius = StyleAttr.borderRadius
            marginRight = (-12).px // center with scrollbar on the right

            StyleAttr.layoutSwitchMediaQuery {
                marginRight = 0.px
            }
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
                        +"addr"
                    }

                    for (columnID in 0..<props.memory.entrysInRow) {
                        ReactHTML.th {
                            /* className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)*/
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
                        +"ascii"
                    }

                }
            }

            ReactHTML.tbody {
                ref = tbody

                var previousAddress: Hex? = null
                val tempMemRows = memList.sortedBy { it.address.toRawString() }.groupBy { it.row.toRawString() }
                val tempRevMemRows = memList.sortedBy { it.offset }.sortedByDescending { it.row.toRawString() }.groupBy { it.row.toRawString() }
                if (DebugTools.REACT_showUpdateInfo) {
                    console.log("REACT: Memory Map Updated!")
                }
                for (memRow in if (props.lowFirst) tempMemRows else tempRevMemRows) {
                    val rowsBetween = if (previousAddress != null) {
                        if (props.lowFirst) {
                            Hex(memRow.key) - previousAddress > Hex(props.memory.entrysInRow.toString(16), props.memory.addressSize)
                        } else {
                            previousAddress - Hex(memRow.key) > Hex(props.memory.entrysInRow.toString(16), props.memory.addressSize)
                        }
                    } else false

                    if (rowsBetween) {
                        ReactHTML.tr {
                            ReactHTML.th {
                                css {
                                    color = important(Memory.InstanceType.NOTUSED.get(StyleAttr.mode))
                                }
                                colSpan = 2 + props.memory.entrysInRow
                                scope = "row"
                                title = "only zeros in addresses between"
                                +"..."
                            }
                        }
                    }
                    previousAddress = Hex(memRow.key, props.memory.addressSize)
                    ReactHTML.tr {
                        ReactHTML.th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "row"
                            +memRow.key
                        }

                        for (id in 0..<props.memory.entrysInRow) {
                            val memInstance = memRow.value.firstOrNull { it.offset == id }
                            if (memInstance == null) {
                                ReactHTML.td {
                                    css {
                                        color = important(Memory.InstanceType.NOTUSED.get(StyleAttr.mode))
                                        fontWeight = important(FontWeight.lighter)
                                    }
                                    title = "unused"
                                    +props.memory.getInitialBinary().get().toHex().toRawString()
                                }
                            } else {
                                ReactHTML.td {
                                    css {
                                        textAlign = TextAlign.center
                                        if (memInstance.address.toRawString() == currExeAddr) {
                                            color = important(StyleAttr.Main.Table.FgPC)
                                            fontWeight = important(FontWeight.bold)
                                        } else {
                                            color = important(memInstance.mark.get(StyleAttr.mode))
                                        }
                                    }

                                    //id = "mem${memInstance.address.toRawString()}"
                                    title = "addr = ${memInstance.address.toRawString()}\nvalue = ${memInstance.variable.get().toDec()} or ${memInstance.variable.get().toUDec()}\ntag = [${memInstance.mark.name}]"

                                    +memInstance.variable.get().toHex().toRawString()

                                    if (memInstance is MainMemory.MemInstance.EditableValue) {
                                        onClick = {
                                            setEditVar(memInstance)
                                        }
                                    }
                                }
                            }
                        }


                        ReactHTML.td {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER + " " + StyleAttr.Main.Table.CLASS_MONOSPACE)
                            ref = asciiRef
                            var asciiString = ""
                            val emptyAscii = props.memory.getInitialBinary().get().toASCII()
                            for (column in 0..<props.memory.entrysInRow) {
                                val memInstance = memRow.value.firstOrNull { it.offset == column }
                                asciiString += memInstance?.variable?.get()?.toASCII() ?: emptyAscii
                            }

                            +asciiString
                        }
                    }
                }
                ReactHTML.tr {
                    ReactHTML.td {
                        colSpan = props.memory.entrysInRow + 2
                        css {
                            paddingTop = 5.rem
                            paddingBottom = 5.rem
                        }
                        +"Memory"
                    }
                }
            }
        }
    }

    if (editVar != null) {
        ReactHTML.div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            ReactHTML.img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setEditVar(null)
                }
            }
            ReactHTML.div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                ReactHTML.label {
                    htmlFor = "editField"
                    +"New Value"
                }

                ReactHTML.input {
                    ref = editRef
                    id = "editField"
                    type = InputType.text
                    pattern = "[0-9a-fA-F]+"
                    placeholder = Settings.PRESTRING_HEX
                    defaultValue = editVar.variable.get().toHex().toRawString()

                    onChange = {
                        val hex = Hex(it.currentTarget.value, props.archState.component1().memory.instanceSize)
                        if (hex.valid) {
                            editVar.variable.set(hex)
                        } else {
                            it.currentTarget.value = editVar.variable.get().toHex().toRawString()
                        }
                    }
                    onKeyDown = {
                        if (it.key == "Enter") {
                            setEditVar(null)
                        }
                    }
                }
            }
        }
    }

    useEffect(memList) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Refresh Memory Table!")
        }
    }

    useEffect(props.memory.memList) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Memory Map or Code State Changed!")
        }
    }

    useEffect(editVar) {
        if (editVar != null) {
            editRef.current?.focus()
        }
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().regContainer.pc.variable.get().toHex().toRawString())
        setMemList(props.memory.memList)
    }

}