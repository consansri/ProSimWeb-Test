package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.ArchConst
import extendable.components.connected.Memory
import extendable.components.types.MutVal
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearInterval
import kotlinx.js.timers.setInterval
import kotlinx.js.timers.setTimeout
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import react.*
import react.dom.html.ButtonType
import react.dom.html.InputType
import react.dom.html.ReactHTML
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
import react.dom.html.ReactHTML.tfoot
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import tools.DebugTools

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
    val contentIVRef = useRef<Timeout>()
    val asciiRef = useRef<HTMLElement>()

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update
    val (internalUpdate, setIUpdate) = useState(false)
    val (memLength, setMemLength) = useState<Int>(props.length)
    val (memEndianess, setEndianess) = useState<Memory.Endianess>()
    val (lowFirst, setLowFirst) = useState(true)
    val (memRows, setMemRows) = useState<MutableMap<String, MutableMap<Int, Memory.MemInstance>>>(mutableMapOf())
    val (showGlobalMemSettings, setShowGlobalMemSettings) = useState(false)
    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (showAddMem, setShowAddMem) = useState(false)
    val (showAddRow, setShowAddRow) = useState(false)

    contentIVRef.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        contentIVRef.current = setInterval({
            setIUpdate(!internalUpdate)
        }, 2000)
    }

    fun calcMemTable() {
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
            overflowY = Overflow.scroll
            maxHeight = 40.vh
            display = Display.block
            backgroundColor = Color("#EEEEEE")
        }

        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0

            table {
                className = ClassName("dcf-table dcf-table-striped dcf-w-100% dcf-darkbg")

                caption {
                    a {
                        +"Memory"
                    }
                }

                thead {

                    tr {

                        th {
                            className = ClassName("dcf-txt-center")
                            scope = "col"
                            +"Address"
                        }

                        for (columnID in 0 until memLength) {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "col"
                                +"$columnID"
                            }
                        }
                        th {
                            className = ClassName("dcf-txt-center")
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
                                    colSpan = 2 + memLength
                                    className = ClassName("dcf-txt-center dcf-mark-notused dcf-darkbg")
                                    scope = "row"
                                    title = "only zeros in addresses between"
                                    +"..."
                                }
                            }
                        }

                        tr {
                            th {
                                className = ClassName("dcf-txt-center dcf-darkbg dcf-mark-address")
                                scope = "row"
                                +memRowKey
                            }

                            for (column in 0 until memLength) {
                                val memInstance = memRow?.get(column)
                                if (memInstance != null) {
                                    td {
                                        className = ClassName("dcf-txt-center dcf-darkbg ${memInstance.mark}")
                                        title = "[${memInstance.address.getRawHexStr()}] [${memInstance.mark.removePrefix("dcf-mark-")}]"

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
                                        className = ClassName("dcf-txt-center dcf-mark-notused dcf-darkbg")
                                        title = "unused"
                                        +appLogic.getArch().getMemory().getInitialBinary().get().toHex().getRawHexStr()
                                    }
                                }
                            }

                            td {
                                className = ClassName("dcf-txt-center dcf-monospace dcf-mark-ascii dcf-darkbg")
                                ref = asciiRef
                                var asciiString = ""
                                val emptyAscii = appLogic.getArch().getMemory().getInitialBinary().get().toASCII()
                                for (column in 0 until memLength) {
                                    val memInstance = memRow?.get(column)
                                    if (memInstance != null) {
                                        asciiString += memInstance.mutVal.get().toASCII()
                                    } else {
                                        asciiString += emptyAscii
                                    }
                                }

                                +asciiString
                            }
                        }
                        if (lowFirst) {
                            nextAddress = (MutVal.Value.Hex(memRowKey) + memLengthValue)
                        } else {
                            nextAddress = (MutVal.Value.Hex(memRowKey) - memLengthValue)
                        }

                    }

                }

                tfoot {

                    tr {
                        td {
                            className = ClassName("dcf-control")
                            colSpan = 2 + memLength

                            button {
                                type = ButtonType.button

                                onClick = {
                                    setShowGlobalMemSettings(!showGlobalMemSettings)
                                }

                                img {
                                    src = "icons/settings.svg"
                                }
                            }

                            if (showGlobalMemSettings) {


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

                                button {
                                    type = ButtonType.button

                                    onClick = {
                                        setShowDefMemSettings(!showDefMemSettings)
                                    }

                                    img {
                                        src = "icons/editable.svg"
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
                                            +entry.name
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
                            }
                        }
                    }

                }
            }
        }
    }

    if (showDefMemSettings) {
        div {
            className = ClassName("window")

            div {

                css {
                    position = Position.relative
                    padding = 1.5.rem
                }

                button {
                    css {
                        position = Position.absolute
                        top = 0.5.rem
                        right = 0.5.rem
                    }

                    img {
                        css {
                            filter = invert(1)
                        }

                        src = "icons/clear.svg"
                    }

                    onClick = {
                        setShowDefMemSettings(false)
                        setShowAddMem(false)
                        setShowAddRow(false)
                    }
                }

                button {
                    css {
                        position = Position.absolute
                        top = 0.5.rem
                        right = 4.rem
                    }

                    img {
                        className = ClassName("delete")
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
                    css {
                        color = Color("#FFFFFF")
                        position = Position.absolute
                        top = 0.5.rem
                        left = 0.5.rem
                        whiteSpace = WhiteSpace.nowrap
                    }
                    +"I/O Memory Section"
                }
            }

            div {
                css {
                    padding = 1.rem
                }
                p {
                    +"Values in this section will be written in Memory before compilation!"
                }
                p {
                    +"You can update these values on runtime!"
                }

            }

            div {

                className = ClassName("window-scrollContainer")

                for (dValue in appLogic.getArch().getMemory().getEditableInstances()) {
                    div {

                        className = ClassName("window-element")

                        if (dValue.name.isNotEmpty()) {
                            input {
                                className = ClassName("noedit")
                                contentEditable = false
                                type = InputType.text
                                readOnly = true
                                value = dValue.name
                            }
                        }

                        input {
                            className = ClassName("noedit")
                            contentEditable = false
                            type = InputType.text
                            readOnly = true
                            value = dValue.address.getRawHexStr()
                        }

                        a {
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
                        }

                        a {
                            button {
                                img {
                                    className = ClassName("delete")
                                    src = StyleConst.Icons.delete
                                }

                                onClick = { event ->
                                    setTimeout({
                                        appLogic.getArch().getMemory().removeEditableValue(dValue)
                                    }, 0)
                                }
                            }
                        }
                        onClick = {
                            setShowAddMem(false)
                            setShowAddRow(false)
                        }

                    }
                }

                div {

                    className = ClassName("window-addelement")

                    onClick = {
                        setShowAddMem(true)
                        setShowAddRow(false)
                    }

                    if (showAddMem) {

                        a {
                            input {
                                id = "dv-name"
                                css {
                                    background = Color("#00000000")
                                    borderRadius = 1.rem
                                    color = Color("#FFFFFF")
                                    textAlign = TextAlign.center
                                    padding = 0.rem
                                }
                                placeholder = "[name]"
                                prefix = "name: "

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
                                    }
                                }
                            }
                        }

                        a {
                            input {
                                id = "dv-address"
                                pattern = "[0-9A-Fa-f]+"
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
                        }

                        a {
                            input {
                                id = "dv-value"
                                pattern = "[0-9A-Fa-f]+"
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
                        }

                        a {
                            button {
                                className = ClassName("apply")
                                onClick = { event ->
                                    setTimeout({
                                        val dvname = (document.getElementById("dv-name") as HTMLInputElement).value
                                        val dvaddr = (document.getElementById("dv-address") as HTMLInputElement).value
                                        val dvvalue = (document.getElementById("dv-value") as HTMLInputElement).value
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
                        }

                    } else {
                        +"+"
                    }
                }

                div {
                    className = ClassName("window-addelement")

                    onClick = {
                        setShowAddRow(true)
                        setShowAddMem(false)
                    }

                    if (showAddRow) {

                        a {
                            input {
                                id = "dr-name"
                                placeholder = "[row name]"
                                prefix = "name: "

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
                                    }
                                }
                            }
                        }

                        a {
                            input {
                                id = "dr-address"
                                pattern = "[0-9A-Fa-f]+"
                                placeholder = ArchConst.PRESTRING_HEX + "[start address]"
                                maxLength = appLogic.getArch().getMemory().getAddressSize().byteCount * 2
                                prefix = "addr: "
                                defaultValue = "1".padEnd(appLogic.getArch().getMemory().getWordSize().bitWidth, '0')

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
                                    }
                                }


                            }
                        }

                        a {
                            input {
                                id = "dr-value"
                                pattern = "[0-9A-Fa-f]+"
                                placeholder = ArchConst.PRESTRING_HEX + "[initial]"
                                maxLength = appLogic.getArch().getMemory().getWordSize().byteCount * 2
                                prefix = "value: "
                                defaultValue = "0".repeat(appLogic.getArch().getMemory().getWordSize().byteCount * 2)

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
                                    }
                                }

                            }
                        }

                        a {
                            input {
                                id = "dr-amount"
                                type = InputType.number
                                placeholder = "[amount]"
                                prefix = "amount: "

                                defaultValue = memLength.toString()

                                onKeyDown = { event ->
                                    if (event.key == "Enter") {
                                        event.currentTarget.blur()
                                    }
                                }
                            }
                        }

                        a {
                            button {
                                className = ClassName("apply")
                                onClick = { event ->
                                    setTimeout({
                                        val drname = (document.getElementById("dr-name") as HTMLInputElement).value
                                        val draddr = (document.getElementById("dr-address") as HTMLInputElement).value
                                        val drvalue = (document.getElementById("dr-value") as HTMLInputElement).value
                                        val dramount = (document.getElementById("dr-amount") as HTMLInputElement).value.toIntOrNull() ?: memLength
                                        try {
                                            var address = MutVal.Value.Hex(draddr, appLogic.getArch().getMemory().getAddressSize())
                                            for (id in 0 until dramount) {
                                                appLogic.getArch().getMemory().addEditableValue(if (drname.isNotEmpty()) "$drname$id" else "", address, MutVal.Value.Hex(drvalue, appLogic.getArch().getMemory().getWordSize()))
                                                address = (address + MutVal.Value.Hex("1")).toHex()
                                            }
                                            appLogic.getArch().getMemory().refreshEditableValues()
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
                        }

                    } else {
                        +"add row"
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
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            appLogic.getArch().getMemory().setEndianess(it)
        }
    }

    useEffect(internalUpdate, update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(internal-update) MemoryView")
        }
        calcMemTable()
    }


}


