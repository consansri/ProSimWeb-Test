package views.components

import AppLogic
import emotion.react.css
import extendable.ArchConst
import extendable.components.connected.Memory
import extendable.components.types.MutVal
import kotlinx.browser.document
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import tools.DebugTools

import web.html.*
import web.timers.*
import web.cssom.*

external interface MemViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var length: Int
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val MemoryView = FC<MemViewProps> { props ->

    val tbody = useRef<HTMLTableSectionElement>()
    val inputLengthRef = useRef<HTMLInputElement>()
    val calcMemTableTimeout = useRef<Timeout>()
    val pcIVRef = useRef<Timeout>()
    val asciiRef = useRef<HTMLElement>()

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update
    val (internalUpdate, setIUpdate) = useState(false)
    val (memLength, setMemLength) = useState<Int>(props.length)
    val (memEndianess, setEndianess) = useState<Memory.Endianess>()
    val (lowFirst, setLowFirst) = useState(true)
    val (memRows, setMemRows) = useState<MutableMap<String, MutableMap<Int, Memory.MemInstance>>>(mutableMapOf())
    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (showAddMem, setShowAddMem) = useState(false)
    val (showAddRow, setShowAddRow) = useState(false)
    val (currExeAddr, setCurrExeAddr) = useState<String>()

    pcIVRef.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        pcIVRef.current = setInterval({
            setCurrExeAddr(appLogic.getArch().getRegisterContainer().pc.value.get().toHex().getRawHexStr())
        }, 100)
    }

    fun calcMemTable(immediate: Boolean = false) {
        calcMemTableTimeout.current?.let {
            clearInterval(it)
        }
        if (!DebugTools.REACT_deactivateAutoRefreshs) {
            calcMemTableTimeout.current = setTimeout({
                val memRowsList: MutableMap<String, MutableMap<Int, Memory.MemInstance>> = mutableMapOf()
                for (entry in appLogic.getArch().getMemory().getMemMap()) {
                    val offset = (entry.value.address % MutVal.Value.Dec("$memLength", MutVal.Size.Bit8())).toHex().getRawHexStr().toInt(16)
                    val rowAddress = (entry.value.address - MutVal.Value.Dec("$offset", MutVal.Size.Bit8())).toHex()
                    val rowResult = memRowsList.get(rowAddress.getRawHexStr())

                    if (rowResult != null) {
                        rowResult[offset] = entry.value
                    } else {
                        val rowList = mutableMapOf<Int, Memory.MemInstance>()
                        rowList[offset] = entry.value
                        memRowsList[rowAddress.toHex().getRawHexStr()] = rowList
                    }
                }

                setMemRows(memRowsList)
            }, if (immediate) 0 else 1000)
        }
    }

    fun getEditableValues() {
        for (dValue in appLogic.getArch().getMemory().getEditableInstances()) {
            try {
                val input = document.getElementById("editval${dValue.address.getRawHexStr()}")
                input?.let {
                    (input as HTMLInputElement).blur()
                }

            } catch (e: ClassCastException) {
                console.warn("ClassCastException")
            }
        }
    }

    div {

        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            position = Position.relative
            /*overflowY = Overflow.scroll*/
            gap = StyleConst.paddingSize

            table {
                backgroundColor = StyleConst.Main.Processor.TableBgColor.get()
                color = StyleConst.Main.Processor.TableFgColor.get()

                caption {
                    color = StyleConst.Main.Processor.FgColor.get()
                }

                input {
                    color = StyleConst.Main.Processor.TableFgColor.get()
                }
            }
        }

        div {
            css {
                display = Display.flex
                width = 100.pct
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.stretch
                alignItems = AlignItems.center
                gap = StyleConst.paddingSize
                paddingLeft = 12.px
                paddingRight = 12.px

                "input[type=range]" {
                    display = Display.inlineBlock
                    cursor = Cursor.pointer
                    border = Border(0.px, LineStyle.hidden)
                    height = StyleConst.iconSize + 2 * StyleConst.iconPadding
                    flexGrow = number(1.0)
                    float = Float.left
                    minHeight = 1.em
                    borderRadius = StyleConst.iconBorderRadius
                    verticalAlign = VerticalAlign.middle
                    accentColor = StyleConst.Main.Processor.BtnBgColor.get()

                    webkitSliderThumb {
                        borderRadius = StyleConst.iconBorderRadius
                    }
                    mozRangeThumb {
                        borderRadius = StyleConst.iconBorderRadius
                    }
                }

                select {
                    background = StyleConst.Main.Processor.BtnBgColor.get()
                    color = StyleConst.Main.Processor.BtnFgColor.get()
                    height = StyleConst.iconSize + 2 * StyleConst.iconPadding
                    fontSize = important(StyleConst.Main.Table.FontSizeSelect)
                    fontWeight = FontWeight.lighter
                    cursor = Cursor.pointer

                }

                button {
                    display = Display.inlineBlock
                    cursor = Cursor.pointer
                    padding = StyleConst.Main.Table.IconPadding
                    float = Float.left
                    color = StyleConst.Main.Processor.BtnFgColor.get()
                    backgroundColor = StyleConst.Main.Processor.BtnBgColor.get()
                    borderRadius = StyleConst.iconBorderRadius
                    transition = Transition(TransitionProperty.all, 0.2.s, TransitionTimingFunction.ease)

                    a {
                        padding = StyleConst.paddingSize
                    }

                    img {
                        display = Display.block
                        height = StyleConst.Main.Table.IconSize
                        width = StyleConst.Main.Table.IconSize
                        filter = important(StyleConst.Main.Processor.BtnFgFilter.get())
                    }
                }
            }

            button {
                type = ButtonType.button

                onClick = {
                    setShowDefMemSettings(!showDefMemSettings)
                }

                img {
                    src = "icons/editable.svg"
                }
            }

            button {
                type = ButtonType.button

                onClick = {
                    calcMemTable(true)
                }

                img {
                    src = "icons/refresh.svg"
                }
            }

            select {

                defaultValue = appLogic.getArch().getMemory().getEndianess().name

                option {
                    disabled = true
                    +"Endianess"
                }

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
                type = InputType.range
                min = 1.0
                max = 16.0
                step = 1.0
                value = "$memLength"

                onInput = {
                    setMemLength(it.currentTarget.valueAsNumber.toInt())
                    localStorage.setItem(StorageKey.MEM_LENGTH, "${it.currentTarget.valueAsNumber.toInt()}")
                }
            }

            button {
                type = ButtonType.button
                css {
                    if (!lowFirst) {
                        filter = invert(0.7)
                    }
                }

                onClick = { event ->
                    setLowFirst(!lowFirst)
                }

                img {
                    src = "icons/direction.svg"
                }
            }


        }

        div {
            css {
                overflowY = Overflow.scroll
                maxHeight = StyleConst.Main.Processor.MaxHeightMem
                borderRadius = StyleConst.borderRadius
                paddingLeft = 12.px // center with scrollbar on the right
            }
            tabIndex = 0

            table {
                thead {
                    tr {
                        css {
                            th {
                                background = important(StyleConst.Main.Processor.TableBgColor.get())
                            }
                        }
                        th {
                            className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"Address"
                        }

                        for (columnID in 0 until memLength) {
                            th {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                scope = "col"
                                +"$columnID"
                            }
                        }
                        th {
                            className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"ASCII"
                        }

                    }
                }

                tbody {
                    ref = tbody

                    var nextAddress: MutVal.Value = appLogic.getArch().getMemory().getInitialBinary().setBin("0").get()
                    val memLengthValue = MutVal.Value.Dec("$memLength", MutVal.Size.Bit8()).toHex()

                    var sortedKeys = memRows.keys.sorted()

                    if (!lowFirst) {
                        sortedKeys = sortedKeys.reversed()
                        nextAddress = nextAddress.getBiggest() - memLengthValue
                    }

                    for (memRowKey in sortedKeys) {
                        val memRow = memRows[memRowKey]
                        if (nextAddress != MutVal.Value.Hex(memRowKey) && memRowKey != sortedKeys.first()) {
                            tr {
                                th {
                                    css {
                                        color = important(StyleConst.Main.Table.Mark.NOTUSED.get())
                                    }
                                    colSpan = 2 + memLength
                                    scope = "row"
                                    title = "only zeros in addresses between"
                                    +"..."
                                }
                            }
                        }

                        tr {
                            th {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER)
                                scope = "row"
                                +memRowKey
                            }

                            for (column in 0 until memLength) {
                                val memInstance = memRow?.get(column)
                                if (memInstance != null) {
                                    td {
                                        css {
                                            textAlign = TextAlign.center
                                            if (memInstance.address.getRawHexStr() == currExeAddr) {
                                                color = important(StyleConst.Main.Table.FgPC)
                                                fontWeight = important(FontWeight.bold)
                                            } else {
                                                color = important(memInstance.mark.get())
                                            }
                                        }
                                        id = "mem${memInstance.address.getRawHexStr()}"

                                        title = "[${memInstance.address.getRawHexStr()}] [${memInstance.mark.name}]"

                                        if (memInstance is Memory.MemInstance.EditableValue) {
                                            if (memInstance.name.isNotEmpty()) {
                                                p {
                                                    +memInstance.name
                                                }
                                            }
                                            +("[" + memInstance.mutVal.get().toHex().getRawHexStr() + "]")
                                        } else {
                                            +memInstance.mutVal.get().toHex().getRawHexStr()
                                        }

                                    }
                                } else {
                                    td {
                                        css {
                                            color = important(StyleConst.Main.Table.Mark.NOTUSED.get())
                                            fontWeight = important(FontWeight.lighter)
                                        }
                                        title = "unused"
                                        +appLogic.getArch().getMemory().getInitialBinary().get().toHex().getRawHexStr()
                                    }
                                }
                            }

                            td {
                                className = ClassName(StyleConst.Main.Table.CLASS_TXT_CENTER + " " + StyleConst.Main.Table.CLASS_MONOSPACE)
                                ref = asciiRef
                                var asciiString = ""
                                val emptyAscii = appLogic.getArch().getMemory().getInitialBinary().get().toASCII()
                                for (column in 0 until memLength) {
                                    val memInstance = memRow?.get(column)
                                    asciiString += if (memInstance != null) {
                                        memInstance.mutVal.get().toASCII()
                                    } else {
                                        emptyAscii
                                    }
                                }

                                +asciiString
                            }
                        }
                        nextAddress = if (lowFirst) {
                            (MutVal.Value.Hex(memRowKey) + memLengthValue)
                        } else {
                            (MutVal.Value.Hex(memRowKey) - memLengthValue)
                        }
                    }
                }
            }
        }
    }

    if (showDefMemSettings) {
        div {
            className = ClassName(StyleConst.Main.Window.CLASS)

            div {
                className = ClassName(StyleConst.Main.Window.CLASS_HEADER)
                button {
                    img {
                        src = "icons/clear.svg"
                    }

                    onClick = {
                        setShowDefMemSettings(false)
                        setShowAddMem(false)
                        setShowAddRow(false)
                    }
                }

                button {
                    img {
                        css {
                            filter = StyleConst.Main.DeleteFilter
                        }
                        src = StyleConst.Icons.delete
                    }

                    onClick = {
                        appLogic.getArch().getMemory().clearEditableValues()
                        setIUpdate(!internalUpdate)
                        setShowAddMem(false)
                        setShowAddRow(false)
                    }
                }

                a {
                    +"I/O Memory Section"
                }
            }

            div {
                className = ClassName(StyleConst.Main.Window.CLASS_INFO)
                p {
                    +"Values in this section will be written in Memory before compilation!"
                }
                p {
                    +"You can update these values on runtime!"
                }

            }

            div {
                className = ClassName(StyleConst.Main.Window.CLASS_CONTENT)

                for (dValue in appLogic.getArch().getMemory().getEditableInstances()) {
                    div {
                        if (dValue.name.isNotEmpty()) {
                            input {
                                contentEditable = false
                                type = InputType.text
                                readOnly = true
                                value = dValue.name
                            }
                        }

                        input {
                            contentEditable = false
                            type = InputType.text
                            readOnly = true
                            value = dValue.address.getRawHexStr()
                        }

                        input {
                            id = "editval${dValue.address.getRawHexStr()}"
                            placeholder = ArchConst.PRESTRING_HEX
                            maxLength = appLogic.getArch().getMemory().getWordSize().byteCount * 2
                            prefix = "value: "
                            defaultValue = dValue.mutVal.get().toHex().getRawHexStr()
                            onBlur = { event ->
                                try {
                                    dValue.mutVal.setHex(event.currentTarget.value)
                                    event.currentTarget.value = dValue.mutVal.get().toHex().getRawHexStr()
                                    appLogic.getArch().getMemory().refreshEditableValues()
                                } catch (e: NumberFormatException) {
                                    event.currentTarget.value = "0"
                                }
                            }

                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        button {
                            img {
                                css {
                                    filter = StyleConst.Main.DeleteFilter
                                }
                                src = StyleConst.Icons.delete
                            }

                            onClick = { event ->
                                setTimeout({
                                    appLogic.getArch().getMemory().removeEditableValue(dValue)
                                }, 0)
                            }
                        }

                        onClick = {
                            setShowAddMem(false)
                            setShowAddRow(false)
                        }
                    }
                }

                div {
                    if (showAddMem) {
                        onClick = {
                            setShowAddRow(false)
                        }
                        input {
                            id = "dv-name"
                            placeholder = "[name]"
                            prefix = "name: "
                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        input {
                            id = "dv-address"
                            placeholder = ArchConst.PRESTRING_HEX + "[address]"
                            maxLength = appLogic.getArch().getMemory().getAddressSize().byteCount * 2
                            prefix = "addr: "
                            defaultValue = "F".repeat(appLogic.getArch().getMemory().getAddressSize().byteCount * 2)
                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        input {
                            id = "dv-value"
                            placeholder = ArchConst.PRESTRING_HEX + "[value]"
                            maxLength = appLogic.getArch().getMemory().getWordSize().byteCount * 2
                            prefix = "value: "
                            defaultValue = "0".repeat(appLogic.getArch().getMemory().getWordSize().byteCount * 2)
                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        button {
                            onClick = { event ->
                                setTimeout({
                                    val dvname = (document.getElementById("dv-name") as HTMLInputElement?)?.value ?: ""
                                    val dvaddr = (document.getElementById("dv-address") as HTMLInputElement?)?.value ?: ""
                                    val dvvalue = (document.getElementById("dv-value") as HTMLInputElement?)?.value ?: ""
                                    try {
                                        appLogic.getArch().getMemory().addEditableValue(dvname, MutVal.Value.Hex(dvaddr, appLogic.getArch().getMemory().getAddressSize()), MutVal.Value.Hex(dvvalue, appLogic.getArch().getMemory().getWordSize()))
                                        appLogic.getArch().getMemory().refreshEditableValues()
                                        setShowAddMem(false)
                                    } catch (e: NumberFormatException) {
                                        console.warn("NumberFormatException!")
                                    }
                                }, 0)
                            }

                            img {
                                src = "icons/add-circle.svg"
                            }
                        }


                    } else {
                        a {
                            css {
                                cursor = Cursor.pointer
                            }
                            +"+"
                            onClick = {
                                setShowAddMem(true)
                                setShowAddRow(false)
                            }
                        }
                    }
                }

                div {
                    if (showAddRow) {
                        css {
                            cursor = Cursor.default
                        }
                        onClick = {
                            setShowAddMem(false)
                        }

                        input {
                            id = "dr-name"
                            placeholder = "[row name]"

                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        input {
                            id = "dr-address"
                            pattern = "[0-9A-Fa-f]+"
                            placeholder = ArchConst.PRESTRING_HEX + "[start address]"
                            maxLength = appLogic.getArch().getMemory().getAddressSize().byteCount * 2
                            defaultValue = "1".padEnd(appLogic.getArch().getMemory().getWordSize().bitWidth, '0')
                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        input {
                            id = "dr-value"
                            pattern = "[0-9A-Fa-f]+"
                            placeholder = ArchConst.PRESTRING_HEX + "[initial]"
                            maxLength = appLogic.getArch().getMemory().getWordSize().byteCount * 2
                            defaultValue = "0".repeat(appLogic.getArch().getMemory().getWordSize().byteCount * 2)

                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        input {
                            id = "dr-amount"
                            type = InputType.number
                            placeholder = "[amount]"
                            defaultValue = memLength.toString()

                            onKeyDown = { event ->
                                if (event.key == "Enter") {
                                    event.currentTarget.blur()
                                }
                            }
                        }

                        button {
                            onClick = { event ->
                                setTimeout({
                                    val drname = (document.getElementById("dr-name") as HTMLInputElement?)?.value ?: ""
                                    val draddr = (document.getElementById("dr-address") as HTMLInputElement?)?.value ?: ""
                                    val drvalue = (document.getElementById("dr-value") as HTMLInputElement?)?.value ?: ""
                                    val dramount = (document.getElementById("dr-amount") as HTMLInputElement?)?.value?.toIntOrNull() ?: memLength
                                    try {
                                        var address = MutVal.Value.Hex(draddr, appLogic.getArch().getMemory().getAddressSize())
                                        for (id in 0 until dramount) {
                                            appLogic.getArch().getMemory().addEditableValue(if (drname.isNotEmpty()) "$drname$id" else "", address, MutVal.Value.Hex(drvalue, appLogic.getArch().getMemory().getWordSize()))
                                            address = (address + MutVal.Value.Hex("1")).toHex()
                                        }
                                        appLogic.getArch().getMemory().refreshEditableValues()
                                        getEditableValues()
                                        setShowAddRow(false)
                                    } catch (e: NumberFormatException) {
                                        console.warn("NumberFormatException!")
                                    }
                                }, 0)
                            }

                            img {
                                src = "icons/add-circle.svg"
                            }
                        }

                    } else {

                        a {
                            css {
                                cursor = Cursor.pointer
                            }
                            +"add row"
                            onClick = {
                                setShowAddRow(true)
                                setShowAddMem(false)
                            }
                        }
                    }
                }
            }
        }
    }

    useEffect(update, memLength) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) MemoryView")
        }
        setEndianess(appLogic.getArch().getMemory().getEndianess())
        for (instance in appLogic.getArch().getMemory().getMemMap()) {
            val memInstance = instance.value
            val td = web.dom.document.getElementById("mem${memInstance.address.getRawHexStr()}") as HTMLTableCellElement?
            td?.innerText = memInstance.mutVal.get().toHex().getRawHexStr()
        }
    }

    useEffect(memLength) {
        calcMemTable()
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            appLogic.getArch().getMemory().setEndianess(it)
        }
    }

}


