package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.components.DataMemory
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.caption
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
    var dataMemory: DataMemory
    var length: Int
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

class MemRow(val id: Double, val address: Double)

val MemoryView = FC<MemViewProps> { props ->

    val dataMemory by useState(props.dataMemory)
    val name by useState(props.name)
    val update = props.update
    val (memLength, setMemLength) = useState<Int>(props.length)
    val (memRows, setMemRows) = useState<MutableList<MemRow>>(mutableListOf<MemRow>())

    val tbody = useRef<HTMLTableSectionElement>()

    val inputLengthRef = useRef<HTMLInputElement>()

    fun calcRowAddress(address: Double): Double {
        val rowID: Double = floor(address / memLength)
        return memLength * rowID
    }

    fun calcRowID(address: Double): Double {
        return floor(address / memLength)
    }

    fun dataForAddress(address: Double): Int? {
        val memList = dataMemory.getMemList()
        for (memInstance in memList) {
            if (memInstance.address == address) {
                return memInstance.value
            }
        }
        return null
    }

    fun calcMemTable() {
        val memRowsList: MutableList<MemRow> = mutableListOf()
        for (memInstance in dataMemory.getMemList()) {
            val rowID = calcRowID(memInstance.address)
            var found = false
            for (memRow in memRowsList) {
                if (memRow.id == rowID) {
                    found = true
                }
            }
            if (!found) {
                memRowsList.add(MemRow(rowID, calcRowAddress(memInstance.address)))
            }
        }
        memRowsList.sortBy { row -> row.id }
        setMemRows(memRowsList)
    }

    div {
        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 50.vh
        }

        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0

            table {
                className = ClassName("dcf-table dcf-table-bordered dcf-table-striped dcf-w-100%")
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

                    }
                }

                tbody {
                    ref = tbody

                    var prevID: Double? = null

                    for (memRow in memRows) {
                        prevID?.let {
                            if(it < memRow.id - 1){
                                tr{
                                    th{
                                        className = ClassName("dcf-txt-center")
                                        scope = "row"
                                        title = "only zeros in addresses between"
                                        +"..."
                                    }
                                }
                            }
                        }

                        tr {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "row"
                                title = "Decimal: ${memRow.id.toLong()}"
                                +memRow.id.toLong().toString(16).uppercase()
                            }

                            for (column in 0 until memLength) {
                                val address = memRow.address + column
                                val hexAddress = address.toLong().toString(16).uppercase()
                                val value = dataForAddress(address) ?: 0
                                val hexValue = value.toString(16).uppercase()
                                td {
                                    className = ClassName("dcf-txt-center")
                                    title = "Address: $hexAddress, Value: $value"
                                    +hexValue
                                }
                            }
                        }
                        prevID = memRow.id
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


