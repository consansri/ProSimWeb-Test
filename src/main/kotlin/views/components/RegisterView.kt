package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.ArchConst
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import kotlin.math.pow

external interface RegisterViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val (update, setUpdate) = props.update
    val theaders = ArchConst.REGISTER_HEADERS

    div {

        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 40.vh
        }

        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0

            table {
                className = ClassName("dcf-table dcf-table-bordered dcf-table-striped dcf-w-100%")
                caption {
                    +name
                }

                thead {
                    tr {
                        for (header in theaders) {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "col"
                                +header
                            }
                        }
                    }
                }

                tbody {
                    val registers = appLogic.getArch().getRegister()
                    for (registerID in registers.indices) {


                        tr {
                            th {
                                className = ClassName("dcf-txt-center")
                                scope = "row"
                                if (registers[registerID].address.getValue() == ArchConst.ADDRESS_NOVALUE) {
                                    +"-"
                                } else {
                                    +"${registers[registerID].address.getValue()}"
                                }

                            }
                            td {
                                className = ClassName("dcf-txt-center")
                                +registers[registerID].name
                            }
                            td {
                                className = ClassName("dcf-txt-center")

                                input {
                                    id = "regdec$registerID"

                                    className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                    readOnly = false
                                    type = InputType.number
                                    pattern = ArchConst.PRESTRING_HEX + "[0-9]*"
                                    placeholder = ArchConst.PRESTRING_DECIMAL
                                    max = 2.0.pow(registers[registerID].widthBit) - 1
                                    min = 0.0

                                    defaultValue = registers[registerID].getValue().toString()

                                    onChange = { event ->
                                        try {
                                            val newValue = event.currentTarget.value.toLong()
                                            registers[registerID].setValue(newValue)
                                            setUpdate(!update)
                                            console.info("Register setValue: ${registers[registerID].getValue()}")
                                        } catch (e: NumberFormatException) {

                                        }
                                    }

                                    onBlur = { event ->
                                        event.currentTarget.value = registers[registerID].getValue().toString()
                                        val reghex = document.getElementById("reghex$registerID") as HTMLInputElement
                                        reghex.value = registers[registerID].getHexValue()
                                    }

                                    onKeyDown = { event ->
                                        if (event.key == "Enter") {
                                            event.currentTarget.blur()
                                        }
                                    }

                                }
                            }
                            td {
                                className = ClassName("dcf-txt-center")
                                input {
                                    id = "reghex$registerID"

                                    className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                    readOnly = false
                                    type = InputType.text
                                    pattern = ArchConst.PRESTRING_HEX + "[0-9a-fA-F]+"
                                    placeholder = ArchConst.PRESTRING_HEX
                                    maxLength = registers[registerID].widthBit / 4 + ArchConst.PRESTRING_HEX.length

                                    defaultValue = registers[registerID].getHexValue()

                                    onChange = { event ->
                                        val newValue = event.currentTarget.value
                                        registers[registerID].setHexValue(newValue)
                                        setUpdate(!update)
                                        console.log("Register setValue: ${registers[registerID].getValue()}")
                                    }

                                    onBlur = { event ->
                                        event.currentTarget.value = registers[registerID].getHexValue()
                                        val reghex = document.getElementById("regdec$registerID") as HTMLInputElement
                                        reghex.value = registers[registerID].getValue().toString()
                                    }

                                    onKeyDown = { event ->
                                        if (event.key == "Enter") {
                                            event.currentTarget.blur()
                                        }
                                    }

                                }
                            }
                            td {
                                className = ClassName("dcf-txt-left")
                                +registers[registerID].description
                            }

                        }
                    }
                }


            }

        }
    }

    useEffect(update) {
        console.log("(update) RegisterView")
    }

}

