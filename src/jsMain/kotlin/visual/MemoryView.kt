package visual

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

    val arch = props.archState.component1()

    val name by useState(props.name)
    val (memEndianess, setEndianess) = useState<Memory.Endianess>()
    val (lowFirst, setLowFirst) = useState(true)
    val (memList, setMemList) = useState<List<Memory.MemInstance>>(props.archState.component1().getMemory().getMemList())
    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (currExeAddr, setCurrExeAddr) = useState<String>()

    val (useBounds, setUseBounds) = useState(arch.getMemory().getIOBounds() != null)
    val (startAddr, setStartAddr) = useState(arch.getMemory().getIOBounds()?.lowerAddr?.toHex())
    val (amount, setAmount) = useState(arch.getMemory().getIOBounds()?.amount ?: 32)

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
                gap = StyleAttr.paddingSize
                paddingLeft = 12.px
                paddingRight = 12.px
                flexGrow = number(0.0)

                "input[type=number]" {
                    display = Display.inlineBlock
                    border = Border(0.px, LineStyle.hidden)
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    flexGrow = number(1.0)
                    float = Float.left
                    borderRadius = StyleAttr.iconBorderRadius
                    verticalAlign = VerticalAlign.middle
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                }

                select {
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    fontSize = important(StyleAttr.Main.Table.FontSizeSelect)
                    fontWeight = FontWeight.lighter
                    cursor = Cursor.pointer

                }

                button {
                    display = Display.inlineBlock
                    cursor = Cursor.pointer
                    padding = StyleAttr.Main.Table.IconPadding
                    float = Float.left
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()
                    borderRadius = StyleAttr.iconBorderRadius
                    transition = Transition(TransitionProperty.all, 0.2.s, TransitionTimingFunction.ease)

                    a {
                        padding = StyleAttr.paddingSize
                    }

                    img {
                        display = Display.block
                        height = StyleAttr.Main.Table.IconSize
                        width = StyleAttr.Main.Table.IconSize
                        filter = important(StyleAttr.Main.Processor.BtnFgFilter.get())
                    }
                }
            }

            button {
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
                    setMemList(arch.getMemory().getMemList())
                }

                img {
                    src = StyleAttr.Icons.refresh
                }
            }*/

            select {

                defaultValue = arch.getMemory().getEndianess().name

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
                defaultValue = arch.getMemory().getEntrysInRow()

                onInput = {
                    arch.getMemory().setEntrysInRow(it.currentTarget.valueAsNumber.toInt())
                    localStorage.setItem(StorageKey.MEM_LENGTH, "${it.currentTarget.valueAsNumber.toInt()}")
                    setMemList(arch.getMemory().getMemList())
                }
            }

            button {
                type = ButtonType.button
                css {
                    if (!lowFirst) {
                        background = important(StyleAttr.Main.Processor.BtnBgColorDeActivated.get())
                    }
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

                        for (columnID in 0 until arch.getMemory().getEntrysInRow()) {
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
                    val memRows = memList.sortedBy { it.address.getRawHexStr() }.groupBy { it.row.getRawHexStr() }
                    val reversedMemRows = memList.sortedBy { it.offset }.sortedByDescending { it.row.getRawHexStr() }.groupBy { it.row.getRawHexStr() }
                    for (memRow in if (lowFirst) memRows else reversedMemRows) {
                        if (previousAddress != null && Hex(memRow.key) - previousAddress > Hex("1", Variable.Size.Bit1())) {
                            tr {
                                th {
                                    css {
                                        color = important(StyleAttr.Main.Table.Mark.NOTUSED.get())
                                    }
                                    colSpan = 2 + arch.getMemory().getEntrysInRow()
                                    scope = "row"
                                    title = "only zeros in addresses between"
                                    +"..."
                                }
                            }
                        }
                        tr {
                            th {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +memRow.key
                            }

                            for (id in 0 until arch.getMemory().getEntrysInRow()) {
                                val memInstance = memRow.value.firstOrNull { it.offset == id }
                                if (memInstance == null) {
                                    td {
                                        css {
                                            color = important(StyleAttr.Main.Table.Mark.NOTUSED.get())
                                            fontWeight = important(FontWeight.lighter)
                                        }
                                        title = "unused"
                                        +arch.getMemory().getInitialBinary().get().toHex().getRawHexStr()
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


                                        if (memInstance is Memory.MemInstance.EditableValue) {
                                            input {
                                                readOnly = false

                                                type = InputType.text
                                                pattern = "[0-9a-fA-F]+"
                                                placeholder = Settings.PRESTRING_HEX
                                                maxLength = arch.getMemory().getWordSize().getByteCount() * 2
                                                defaultValue = memInstance.variable.get().toHex().getRawHexStr()

                                                onBlur = { event ->

                                                    // Set Value
                                                    val newValue = event.currentTarget.value
                                                    val currentTarget = event.currentTarget

                                                    setTimeout({
                                                        try {
                                                            memInstance.variable.setHex(newValue)
                                                            //setIUpdate(!internalUpdate)
                                                            arch.getConsole()
                                                                .info("Memory IO: [${memInstance.variable.get().toDec().getDecStr()}|${memInstance.variable.get().toUDec().getUDecStr()}|${memInstance.variable.get().toHex().getHexStr()}|${memInstance.variable.get().toBin().getBinaryStr()}]")
                                                        } catch (e: NumberFormatException) {
                                                            arch.getConsole().warn("MemoryView io onBlur: NumberFormatException")
                                                        }

                                                        // Get Actual Interpretation (for example padded binary number)
                                                        try {
                                                            currentTarget.value = memInstance.variable.get().toHex().getRawHexStr()
                                                        } catch (e: NumberFormatException) {
                                                            arch.getConsole().warn("RegisterView reg onBlur: NumberFormatException")
                                                        }

                                                    }, 0)

                                                }
                                                onKeyDown = { event ->
                                                    if (event.key == "Enter") {
                                                        event.currentTarget.blur()
                                                    }
                                                }
                                            }
                                        } else {
                                            +memInstance.variable.get().toHex().getRawHexStr()
                                        }
                                    }
                                }
                            }


                            td {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER + " " + StyleAttr.Main.Table.CLASS_MONOSPACE)
                                ref = asciiRef
                                var asciiString = ""
                                val emptyAscii = arch.getMemory().getInitialBinary().get().toASCII()
                                for (column in 0 until arch.getMemory().getEntrysInRow()) {
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
                            colSpan = arch.getMemory().getEntrysInRow() + 2
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
                            setStartAddr(if (it.currentTarget.value != "") Hex(it.currentTarget.value, arch.getMemory().getAddressSize()) else null)
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
                if (it.checkResult.valid) arch.getMemory().useIOBounds(it, amount) else arch.getConsole().error("IO Definition: Address isn't valid!")
            } ?: arch.getConsole().error("IO Definition: Address isn't valid!")

        } else {
            arch.getMemory().removeIOBounds()
        }
    }

    useEffect(props.exeEventState) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(arch.getRegContainer().pc.variable.get().toHex().getRawHexStr())
        setMemList(arch.getMemory().getMemList())
    }

    useEffect(arch.getMemory().memList, arch.getState().state) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Memory Map or Code State Changed!")
        }
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            arch.getMemory().setEndianess(it)
        }
    }

}


