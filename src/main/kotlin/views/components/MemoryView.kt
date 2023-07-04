package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.components.connected.Memory
import extendable.components.types.ByteValue
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.body
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.col
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import kotlin.math.floor

external interface MemViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var length: Int
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val MemoryView = FC<MemViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update
    val (memLength, setMemLength) = useState<Int>(props.length)
    val (memRows, setMemRows) = useState<MutableMap<String, MutableMap<Int, Memory.DMemInstance>>>(mutableMapOf())

    val tbody = useRef<HTMLTableSectionElement>()

    val inputLengthRef = useRef<HTMLInputElement>()


    fun calcMemTable() {

        val memRowsList: MutableMap<String, MutableMap<Int, Memory.DMemInstance>> = mutableMapOf()
        for (entry in appLogic.getArch().getMemory().getMemMap()) {
            val offset = (entry.value.address % ByteValue.Type.Dec("$memLength", ByteValue.Size.Bit8())).toHex().getRawHexStr().toInt(16)
            val rowAddress = (entry.value.address - ByteValue.Type.Dec("$offset", ByteValue.Size.Bit8())).toHex()
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
                className = ClassName("dcf-table dcf-table-striped dcf-w-100%")
                caption {
                    a {
                        +name
                    }
                }
                caption {
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

                    var nextAddress: ByteValue.Type = appLogic.getArch().getMemory().getInitialBinary().setBin("0").get()
                    val memLengthValue = ByteValue.Type.Dec("$memLength", ByteValue.Size.Bit8()).toHex()

                    for (memRowKey in memRows.keys.sorted()) {
                        val memRow = memRows[memRowKey]
                        if (nextAddress != ByteValue.Type.Hex(memRowKey)) {
                            tr {
                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "row"
                                    title = "only zeros in addresses between"
                                    +"..."
                                }
                                for (column in 0..memLength) {
                                    td {
                                        className = ClassName("dcf-txt-center")
                                        title = "only zeros in addresses between"
                                    }
                                }
                            }
                        }

                        tr {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "row"
                                +memRowKey
                            }

                            for (column in 0 until memLength) {
                                val memInstance = memRow?.get(column)
                                if (memInstance != null) {
                                    td {
                                        className = ClassName("dcf-txt-center")
                                        title = "Address: ${memInstance.address.getRawHexStr()}"
                                        +memInstance.byteValue.get().toHex().getRawHexStr()
                                    }
                                } else {
                                    td {
                                        className = ClassName("dcf-txt-center")
                                        title = "unused"
                                        +appLogic.getArch().getMemory().getInitialBinary().get().toHex().getRawHexStr()
                                    }
                                }
                            }

                            td {
                                className = ClassName("dcf-txt-left" + " " + "dcf-monospace")
                                var asciiString = ""
                                val emptyAscii = appLogic.getArch().getMemory().getInitialBinary().get().toASCII()
                                for (column in 0 until memLength) {
                                    val memInstance = memRow?.get(column)
                                    if (memInstance != null) {
                                        asciiString += memInstance.byteValue.get().toASCII()
                                    } else {
                                        asciiString += emptyAscii
                                    }
                                }
                                +asciiString
                            }
                        }
                        nextAddress = (ByteValue.Type.Hex(memRowKey) + memLengthValue)
                    }
                }
            }
        }
    }
    useEffect(update, memLength) {
        calcMemTable()
        console.log("(update) MemoryView")
    }


}


