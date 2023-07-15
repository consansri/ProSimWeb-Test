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
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import react.*
import react.dom.html.ButtonType
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.p
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
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
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
    val (lowFirst, setLowFirst) = useState(true)
    val (memRows, setMemRows) = useState<MutableMap<String, MutableMap<Int, Memory.DMemInstance>>>(mutableMapOf())
    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (showAddMem, setShowAddMem) = useState(false)

    contentIVRef.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        contentIVRef.current = setInterval({
            setIUpdate(!internalUpdate)
        }, 500)
    }

    fun calcMemTable() {
        val memRowsList: MutableMap<String, MutableMap<Int, Memory.DMemInstance>> = mutableMapOf()
        for (entry in appLogic.getArch().getMemory().getMemMap()) {
            val offset = (entry.value.address % MutVal.Value.Dec("$memLength", MutVal.Size.Bit8())).toHex().getRawHexStr().toInt(16)
            val rowAddress = (entry.value.address - MutVal.Value.Dec("$offset", MutVal.Size.Bit8())).toHex()
            val rowResult = memRowsList.get(rowAddress.getRawHexStr())

            if (rowResult != null) {
                rowResult[offset] = entry.value
            } else {
                val rowList = mutableMapOf<Int, Memory.DMemInstance>()
                rowList[offset] = entry.value
                memRowsList[rowAddress.toHex().getRawHexStr()] = rowList
            }
        }

        setMemRows(memRowsList)
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
                        +name
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
                                        title = "Address: ${memInstance.address.getRawHexStr()}"
                                        if (memInstance is Memory.DMemInstance.EditableValue) {
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
                        /*td {
                            className = ClassName("dcf-control")

                        }*/
                        td {
                            colSpan = 2 + memLength
                            className = ClassName("dcf-control")

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
                                    src = "icons/settings.svg"
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
                        appearance = null
                        border = 0.px
                        background = Color("#00000000")
                        color = Color("#FFFFFF")
                        position = Position.absolute
                        top = 0.5.rem
                        right = 0.5.rem
                        cursor = Cursor.pointer
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
                    padding = 3.rem
                    overflowY = Overflow.scroll
                    boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 0.px, 10.px, Color("#000"))
                }



                for (dValue in appLogic.getArch().getMemory().getDefaultInstances()) {
                    div {

                        css {
                            marginBottom = 1.rem
                            backgroundColor = Color("#313131")
                            borderRadius = 1.rem
                            padding = 1.rem
                        }

                        a {
                            +dValue.name
                        }

                        a {
                            css {
                                marginLeft = 3.rem
                            }

                            +dValue.address.getRawHexStr()
                        }

                        a {
                            css {
                                marginLeft = 3.rem
                            }

                            input {
                                css {
                                    marginLeft = 1.rem
                                    background = Color("#00000000")
                                    borderRadius = 1.rem
                                    color = Color("#FFFFFF")
                                    textAlign = TextAlign.center
                                }
                                placeholder = ArchConst.PRESTRING_BINARY
                                maxLength = appLogic.getArch().getMemory().getWordSize().bitWidth
                                prefix = "value: "
                                defaultValue = dValue.mutVal.get().toBin().getRawBinaryStr()

                                onBlur = { event ->
                                    try {
                                        dValue.mutVal.setBin(event.currentTarget.value)
                                        event.currentTarget.value = dValue.mutVal.get().toBin().getRawBinaryStr()
                                        appLogic.getArch().getMemory().refreshEditableValues()
                                    } catch (e: NumberFormatException) {
                                        event.currentTarget.value = "0"
                                    }
                                }

                            }


                        }

                        a {
                            css {
                                marginLeft = 1.rem
                            }

                            button {
                                css {
                                    float = Float.right
                                    background = Color("#00000000")
                                    color = Color("#FFFFFF")
                                    border = 0.px
                                    cursor = Cursor.pointer
                                }

                                img {

                                    className = ClassName("delete")

                                    src = StyleConst.Icons.delete
                                }

                                onClick = { event ->
                                    appLogic.getArch().getMemory().removeEditableValue(dValue)
                                }
                            }
                        }
                        onClick = {
                            setShowAddMem(false)
                        }

                    }
                }

                div {
                    css {
                        position = Position.relative
                        textAlign = TextAlign.center
                        backgroundColor = Color("#515151")
                        padding = 1.rem
                        borderRadius = 1.rem
                        cursor = Cursor.pointer
                        transitionDuration = 200.ms
                        transitionProperty = TransitionProperty.all
                        transitionTimingFunction = TransitionTimingFunction.ease

                        if (!showAddMem) {
                            hover {
                                backgroundColor = Color("#717171")
                            }
                        }
                    }

                    onClick = {
                        setShowAddMem(true)
                    }

                    if (showAddMem) {

                        var name = ""
                        var address: MutVal.Value.Hex = appLogic.getArch().getMemory().getAddressMax().toHex()
                        var value: MutVal.Value.Binary = MutVal.Value.Binary("0")

                        a {
                            input {
                                id = "dv-name"
                                css {
                                    marginLeft = 1.rem
                                    background = Color("#00000000")
                                    borderRadius = 1.rem
                                    color = Color("#FFFFFF")
                                    textAlign = TextAlign.center
                                }
                                placeholder = "[name]"
                                prefix = "name: "

                                onBlur = {
                                    name = it.currentTarget.value
                                }

                            }
                        }

                        a {
                            css {
                                marginLeft = 1.rem
                            }

                            input {
                                id = "dv-address"
                                css {
                                    marginLeft = 1.rem
                                    background = Color("#00000000")
                                    borderRadius = 1.rem
                                    color = Color("#FFFFFF")
                                    textAlign = TextAlign.center
                                }
                                pattern = "[0-9A-Fa-f]+"
                                placeholder = ArchConst.PRESTRING_HEX + "[address]"
                                maxLength = appLogic.getArch().getMemory().getAddressSize().byteCount * 2
                                prefix = "addr: "

                                onBlur = {
                                    address = MutVal.Value.Hex(it.currentTarget.value)
                                    it.currentTarget.value = address.getRawHexStr()
                                }

                            }
                        }

                        a {
                            css {
                                marginLeft = 1.rem
                            }

                            input {
                                id = "dv-value"
                                css {
                                    marginLeft = 1.rem
                                    background = Color("#00000000")
                                    borderRadius = 1.rem
                                    color = Color("#FFFFFF")
                                    textAlign = TextAlign.center
                                }
                                pattern = "[01]+"
                                placeholder = ArchConst.PRESTRING_BINARY + "[value]"
                                maxLength = appLogic.getArch().getMemory().getWordSize().bitWidth
                                prefix = "value: "

                                onBlur = {
                                    value = MutVal.Value.Binary(it.currentTarget.value)
                                    it.currentTarget.value = value.getRawBinaryStr()
                                }

                            }
                        }

                        a {
                            css {
                                marginLeft = 1.rem
                            }

                            button {
                                css {
                                    float = Float.right
                                    background = Color("#00000000")
                                    color = Color("#FFFFFF")
                                    border = 0.px
                                    cursor = Cursor.pointer
                                    fontWeight = integer(700)

                                    hover {
                                        filter = brightness(2)
                                    }
                                }

                                onClick = { event ->
                                    val name = (document.getElementById("dv-name") as HTMLInputElement).value
                                    val addr = (document.getElementById("dv-address") as HTMLInputElement).value
                                    val value = (document.getElementById("dv-value") as HTMLInputElement).value
                                    try {
                                        appLogic.getArch().getMemory().addEditableValue(name, MutVal.Value.Hex(addr, appLogic.getArch().getMemory().getAddressSize()), MutVal.Value.Binary(value, appLogic.getArch().getMemory().getWordSize()))
                                        appLogic.getArch().getMemory().refreshEditableValues()
                                        setShowAddMem(false)
                                    } catch (e: NumberFormatException) {

                                    }
                                }

                                img {
                                    css {
                                        filter = invert(1)
                                    }
                                    src = "icons/add-circle.svg"
                                }
                            }
                        }

                    } else {
                        +"+"
                    }
                }
            }

            div {
                css {
                    position = Position.absolute
                    bottom = 0.px
                    padding = 1.rem
                }
                p {
                    +"Default Values will be written in Memory before compilation!"
                }
                p {
                    +"You can update default values on runtime!"
                }

            }

        }


    }


    useEffect(update, memLength) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) MemoryView")
        }
    }

    useEffect(internalUpdate, update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(internal-update) MemoryView")
        }
        calcMemTable()
    }


}


