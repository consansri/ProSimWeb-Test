package visual

import StorageKey
import StyleAttr

import emotion.react.css
import emulator.kit.Settings
import emulator.kit.common.Memory
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import debug.DebugTools
import emulator.kit.Architecture
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.h1

import web.html.*
import web.timers.*
import web.cssom.*

external interface MemViewProps : Props {
    var name: String
    var archState: StateInstance<Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var length: Int
}

val MemoryView = FC<MemViewProps> { props ->

    val tbody = useRef<HTMLTableSectionElement>()
    val inputLengthRef = useRef<HTMLInputElement>()
    val asciiRef = useRef<HTMLElement>()
    val editRef = useRef<HTMLInputElement>()

    val name by useState(props.name)
    val (memEndianess, setEndianess) = useState<Memory.Endianess>()
    val (lowFirst, setLowFirst) = useState(true)
    val (memList, setMemList) = useState<List<Memory.MemInstance>>(props.archState.component1().getMemory().getMemList())
    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (currExeAddr, setCurrExeAddr) = useState<String>()

    val (useBounds, setUseBounds) = useState(localStorage.getItem("${StorageKey.MIO_ACTIVE}-${props.archState.component1().getDescription().name}")?.toBooleanStrictOrNull() ?: (props.archState.component1().getMemory().getIOBounds() != null))
    val (startAddr, setStartAddr) = useState(localStorage.getItem("${StorageKey.MIO_START}-${props.archState.component1().getDescription().name}")?.let { Hex(it, props.archState.component1().getMemory().getAddressSize()) } ?: props.archState.component1().getMemory()
        .getIOBounds()?.lowerAddr?.toHex())
    val (amount, setAmount) = useState(localStorage.getItem("${StorageKey.MIO_AMOUNT}-${props.archState.component1().getDescription().name}")?.toLongOrNull() ?: props.archState.component1().getMemory().getIOBounds()?.amount ?: 32)

    val (editVar, setEditVar) = useState<Memory.MemInstance.EditableValue>()


    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            justifyContent = JustifyContent.stretch
            alignItems = AlignItems.stretch
            position = Position.relative
            gap = StyleAttr.paddingSize
            height = 100.pct

            table {
                backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()
                color = StyleAttr.Main.Processor.TableFgColor.get()

                caption {
                    color = StyleAttr.Main.Processor.TableFgColor.get()
                }

                input {
                    color = StyleAttr.Main.Processor.TableFgColor.get()
                }
            }
        }

        div {
            css {
                display = Display.flex
                width = 100.pct
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.flexEnd
                alignItems = AlignItems.center
                gap = 1.px
                paddingLeft = 12.px
                paddingRight = 12.px
                flexGrow = number(0.0)

                "input[type=number]" {
                    display = Display.inlineBlock
                    border = Border(0.px, LineStyle.hidden)
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    flexGrow = number(1.0)
                    float = Float.left
                    verticalAlign = VerticalAlign.middle
                    borderRadius = 0.px
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                }

                select {
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    fontSize = important(StyleAttr.Main.Table.FontSizeSelect)
                    fontWeight = FontWeight.lighter
                    borderRadius = 0.px
                    cursor = Cursor.pointer
                }

                button {
                    display = Display.inlineBlock
                    cursor = Cursor.pointer
                    padding = StyleAttr.iconPadding
                    float = Float.left
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()

                    a {
                        padding = StyleAttr.paddingSize
                    }

                    img {
                        display = Display.block
                        height = StyleAttr.iconSize
                        filter = important(StyleAttr.Main.Processor.BtnFgFilter.get())
                    }
                }
            }

            button {
                css {
                    borderTopLeftRadius = StyleAttr.iconBorderRadius
                    borderBottomLeftRadius = StyleAttr.iconBorderRadius
                }
                type = ButtonType.button

                onClick = {
                    setShowDefMemSettings(!showDefMemSettings)
                }

                img {
                    src = StyleAttr.Icons.edit
                }
            }

            /*button {
                type = ButtonType.button

                onClick = {
                    setMemList(props.archState.component1().getMemory().getMemList())
                }

                img {
                    src = StyleAttr.Icons.refresh
                }
            }*/

            select {

                defaultValue = props.archState.component1().getMemory().getEndianess().name

                for (entry in Memory.Endianess.entries) {
                    option {
                        value = entry.name
                        +entry.uiName
                    }
                }

                onChange = {
                    for (entry in Memory.Endianess.entries) {
                        if (it.currentTarget.value == entry.name) {
                            setEndianess(entry)
                        }
                    }
                }
            }
            input {
                ref = inputLengthRef
                placeholder = "values per row"
                type = InputType.number
                min = 1.0
                max = 16.0
                step = 1.0
                defaultValue = props.archState.component1().getMemory().getEntrysInRow()

                onInput = {
                    props.archState.component1().getMemory().setEntrysInRow(it.currentTarget.valueAsNumber.toInt())
                    localStorage.setItem(StorageKey.MEM_LENGTH, "${it.currentTarget.valueAsNumber.toInt()}")
                    setMemList(props.archState.component1().getMemory().getMemList())
                }
            }

            button {
                type = ButtonType.button
                css {
                    borderTopRightRadius = StyleAttr.iconBorderRadius
                    borderBottomRightRadius = StyleAttr.iconBorderRadius
                }

                onClick = { event ->
                    setLowFirst(!lowFirst)
                }

                img {
                    src = StyleAttr.Icons.reverse
                }
            }


        }

        div {
            css {
                display = Display.block
                overflowY = Overflow.scroll
                flexGrow = number(1.0)
                maxHeight = 50.vh
                borderRadius = StyleAttr.borderRadius
                paddingLeft = 12.px // center with scrollbar on the right
            }
            tabIndex = 0

            table {
                thead {
                    tr {
                        css {
                            th {
                                background = important(StyleAttr.Main.Processor.TableBgColor.get())
                            }
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"Address"
                        }

                        for (columnID in 0 until props.archState.component1().getMemory().getEntrysInRow()) {
                            th {
                                /* className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)*/
                                css {
                                    textAlign = TextAlign.center
                                    width = 4.ch
                                }
                                scope = "col"
                                +"$columnID"
                            }
                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"ASCII"
                        }

                    }
                }

                tbody {
                    ref = tbody


                    var previousAddress: Hex? = null
                    val tempMemRows = memList.sortedBy { it.address.getRawHexStr() }.groupBy { it.row.getRawHexStr() }
                    val tempRevMemRows = memList.sortedBy { it.offset }.sortedByDescending { it.row.getRawHexStr() }.groupBy { it.row.getRawHexStr() }
                    if (DebugTools.REACT_showUpdateInfo) {
                        console.log("REACT: Memory Map Updated!")
                    }
                    for (memRow in if (lowFirst) tempMemRows else tempRevMemRows) {
                        if (previousAddress != null && Hex(memRow.key) - previousAddress > Hex(props.archState.component1().getMemory().getEntrysInRow().toString(16), props.archState.component1().getMemory().getAddressSize())) {
                            tr {
                                th {
                                    css {
                                        color = important(StyleAttr.Main.Table.Mark.NOTUSED.get())
                                    }
                                    colSpan = 2 + props.archState.component1().getMemory().getEntrysInRow()
                                    scope = "row"
                                    title = "only zeros in addresses between"
                                    +"..."
                                }
                            }
                        }
                        previousAddress = Hex(memRow.key, props.archState.component1().getMemory().getAddressSize())
                        tr {
                            th {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +memRow.key
                            }

                            for (id in 0..<props.archState.component1().getMemory().getEntrysInRow()) {
                                val memInstance = memRow.value.firstOrNull { it.offset == id }
                                if (memInstance == null) {
                                    td {
                                        css {
                                            color = important(StyleAttr.Main.Table.Mark.NOTUSED.get())
                                            fontWeight = important(FontWeight.lighter)
                                        }
                                        title = "unused"
                                        +props.archState.component1().getMemory().getInitialBinary().get().toHex().getRawHexStr()
                                    }
                                } else {
                                    td {
                                        css {
                                            textAlign = TextAlign.center
                                            if (memInstance.address.getRawHexStr() == currExeAddr) {
                                                color = important(StyleAttr.Main.Table.FgPC)
                                                fontWeight = important(FontWeight.bold)
                                            } else {
                                                color = important(memInstance.mark.get())
                                            }
                                        }

                                        //id = "mem${memInstance.address.getRawHexStr()}"
                                        title = "addr = ${memInstance.address.getRawHexStr()}\nvalue = ${memInstance.variable.get().toDec()} or ${memInstance.variable.get().toUDec()}\ntag = [${memInstance.mark.name}]"

                                        +memInstance.variable.get().toHex().getRawHexStr()

                                        if (memInstance is Memory.MemInstance.EditableValue) {
                                            onClick = {
                                                setEditVar(memInstance)
                                            }
                                        }
                                    }
                                }
                            }


                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER + " " + StyleAttr.Main.Table.CLASS_MONOSPACE)
                                ref = asciiRef
                                var asciiString = ""
                                val emptyAscii = props.archState.component1().getMemory().getInitialBinary().get().toASCII()
                                for (column in 0 until props.archState.component1().getMemory().getEntrysInRow()) {
                                    val memInstance = memRow.value.firstOrNull { it.offset == column }
                                    asciiString += if (memInstance != null) {
                                        memInstance.variable.get().toASCII()
                                    } else {
                                        emptyAscii
                                    }
                                }

                                +asciiString
                            }
                        }
                    }
                    tr {
                        td {
                            colSpan = props.archState.component1().getMemory().getEntrysInRow() + 2
                            css {
                                paddingTop = 15.rem
                                paddingBottom = 15.rem
                            }
                            +"Memory"
                        }
                    }
                }
            }
        }
    }

    if (editVar != null) {
        div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setEditVar(null)
                }
            }
            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                ReactHTML.label {
                    htmlFor = "editField"
                    +"New Value"
                }

                input {
                    ref = editRef
                    id = "editField"
                    type = InputType.text
                    pattern = "[0-9a-fA-F]+"
                    placeholder = Settings.PRESTRING_HEX
                    defaultValue = editVar.variable.get().toHex().getRawHexStr()

                    onChange = {
                        val hex = Hex(it.currentTarget.value, props.archState.component1().getMemory().getWordSize())
                        if (hex.checkResult.valid) {
                            editVar.variable.set(hex)
                        } else {
                            it.currentTarget.value = editVar.variable.get().toHex().getRawHexStr()
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

    if (showDefMemSettings) {
        div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setShowDefMemSettings(false)
                }
            }

            h1 {
                +"IO"
            }

            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                ReactHTML.label {
                    htmlFor = "enableIO"
                    +"Use IO Section"
                }
                input {
                    id = "enableIO"
                    type = InputType.checkbox
                    checked = useBounds

                    onChange = {
                        setUseBounds(it.currentTarget.checked)
                    }
                }
            }
            if (useBounds) {
                div {
                    className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                    ReactHTML.label {
                        htmlFor = "ioStart"
                        +"Starting Address"
                    }

                    input {
                        id = "ioStart"
                        type = InputType.text
                        pattern = "[0-9a-fA-F]+"
                        placeholder = Settings.PRESTRING_HEX
                        defaultValue = startAddr?.getRawHexStr() ?: ""

                        onChange = {
                            setStartAddr(if (it.currentTarget.value != "") Hex(it.currentTarget.value, props.archState.component1().getMemory().getAddressSize()) else null)
                        }
                    }
                }

                div {
                    className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                    ReactHTML.label {
                        htmlFor = "ioAmount"
                        +"Amount"
                    }

                    input {
                        id = "ioAmount"
                        type = InputType.number
                        defaultValue = amount

                        onChange = {
                            setAmount(it.currentTarget.valueAsNumber.toLong())
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

    useEffect(useBounds, startAddr, amount) {
        if (useBounds) {
            startAddr?.let {
                if (it.checkResult.valid) props.archState.component1().getMemory().useIOBounds(it, amount) else props.archState.component1().getConsole().error("IO Definition: Address isn't valid!")
            } ?: props.archState.component1().getConsole().error("IO Definition: Address isn't valid!")

        } else {
            props.archState.component1().getMemory().removeIOBounds()
        }
        localStorage.setItem("${StorageKey.MIO_ACTIVE}-${props.archState.component1().getDescription().name}", useBounds.toString())
        startAddr?.let {
            localStorage.setItem("${StorageKey.MIO_START}-${props.archState.component1().getDescription().name}", startAddr.getHexStr())
        }
        localStorage.setItem("${StorageKey.MIO_AMOUNT}-${props.archState.component1().getDescription().name}", amount.toString())
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().getRegContainer().pc.variable.get().toHex().getRawHexStr())
        setMemList(props.archState.component1().getMemory().getMemList())
    }

    useEffect(props.archState.component1().getMemory().memList, props.archState.component1().getState().state) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Memory Map or Code State Changed!")
        }
    }

    useEffect(editVar) {
        if (editVar != null) {
            editRef.current?.focus()
        }
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            props.archState.component1().getMemory().setEndianess(it)
        }
    }

}


