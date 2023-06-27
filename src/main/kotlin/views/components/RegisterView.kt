package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.ArchConst
import extendable.ArchConst.RegTypes.*
import extendable.components.connected.RegisterContainer
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr

external interface RegisterViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val regFileList = appLogic.getArch().getRegisterContainer().getRegisterFileList()
    val (currRegFileIndex, setCurrRegFileIndex) = useState<Int>(regFileList.size - 1)
    val (currRegTypeIndex, setCurrRegTypeIndex) = useState<Int>(1)
    val (update, setUpdate) = useState(false)
    val change = props.update
    val theaders = ArchConst.REGISTER_HEADERS

    val bodyRef = useRef<HTMLTableSectionElement>()

    val registerContainer = appLogic.getArch().getRegisterContainer()

    div {

        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 40.vh

        }

        div {

            className = ClassName("dcf-tabs")

            button {
                css {
                    color = Color("#FFFFFF")
                    backgroundColor = Color("#AAAAAA00")
                    paddingLeft = 1.rem
                    paddingRight = 1.rem
                    border = Border(0.px, LineStyle.hidden)
                }

                +"Register"
            }

            for (regFile in regFileList) {
                when (regFile.label) {
                    RegisterContainer.RegLabel.PC -> {

                    }

                    RegisterContainer.RegLabel.MAIN -> {
                        a {
                            if (currRegFileIndex == regFileList.indexOf(regFile)) {
                                className = ClassName("dcf-tab-active")
                            } else {
                                className = ClassName("")
                            }

                            title = "Show RegFile ${regFile.name}"

                            +regFile.name
                            onClick = {
                                setCurrRegFileIndex(regFileList.indexOf(regFile))
                            }
                        }
                    }

                    RegisterContainer.RegLabel.SYSTEM -> {
                        a {
                            if (currRegFileIndex == regFileList.indexOf(regFile)) {
                                className = ClassName("dcf-tab-active")
                            } else {
                                className = ClassName("")
                            }
                            title = "Show RegFile ${regFile.name}"
                            +regFile.name

                            onClick = {
                                setCurrRegFileIndex(regFileList.indexOf(regFile))
                            }
                        }
                    }

                    RegisterContainer.RegLabel.CUSTOM -> {
                        a {
                            if (currRegFileIndex == regFileList.indexOf(regFile)) {
                                className = ClassName("dcf-tab-active")
                            } else {
                                className = ClassName("")
                            }
                            title = "Show RegFile ${regFile.name}"

                            +regFile.name

                            onClick = {
                                setCurrRegFileIndex(regFileList.indexOf(regFile))
                            }
                        }
                    }
                }
            }
        }



        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0


            table {
                className = ClassName("dcf-table dcf-table-striped dcf-w-100%")

                val registerArray = registerContainer.getRegisterFileList()[currRegFileIndex]

                thead {
                    tr {
                        for (header in theaders) {
                            if (header != ArchConst.RegHeaders.VALUE) {
                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "col"
                                    +header.toString()
                                }
                            } else {
                                th {
                                    className = ClassName("dcf-txt-center dcf-button")
                                    scope = "col"

                                    span {
                                        +ArchConst.REGISTER_VALUETYPES[currRegTypeIndex].toString()
                                    }

                                    onClick = { event ->
                                        if (currRegTypeIndex < ArchConst.REGISTER_VALUETYPES.size - 1) {
                                            setCurrRegTypeIndex(currRegTypeIndex + 1)
                                        } else {
                                            setCurrRegTypeIndex(0)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }




                tbody {
                    ref = bodyRef

                    registerArray.let {
                        for (reg in it.registers) {
                            val regID = it.registers.indexOf(reg)

                            tr {
                                th {
                                    className = ClassName("dcf-txt-center")
                                    scope = "row"
                                    if (reg.address.getValue() == ArchConst.ADDRESS_NOVALUE) {
                                        +"-"
                                    } else {
                                        +"${reg.address.getValue()}"
                                    }
                                }
                                td {
                                    className = ClassName("dcf-txt-center")
                                    +reg.name
                                }
                                td {
                                    className = ClassName("value-col dcf-txt-center")

                                    input {
                                        id = "reg0${regID}"
                                        className = ClassName(StyleConst.CLASS_TABLE_INPUT)
                                        readOnly = false

                                        when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                            HEX -> {
                                                type = InputType.text
                                                pattern = "[0-9a-fA-F]+"
                                                placeholder = ArchConst.PRESTRING_HEX
                                                maxLength = reg.byteValue.size.byteCount * 2
                                                defaultValue = reg.byteValue.get().toHex().getRawHexStr()
                                            }

                                            BIN -> {
                                                type = InputType.text
                                                pattern = "[01]+"
                                                placeholder = ArchConst.PRESTRING_BINARY
                                                maxLength = reg.byteValue.size.bitWidth
                                                defaultValue = reg.byteValue.get().toBin().getRawBinaryStr()
                                            }

                                            DEC -> {
                                                type = InputType.number
                                                pattern = "-?\\d+"
                                                placeholder = ArchConst.PRESTRING_DECIMAL
                                                defaultValue = reg.byteValue.get().toDec().getRawDecStr()
                                            }

                                            UDEC -> {
                                                type = InputType.number
                                                placeholder = ArchConst.PRESTRING_DECIMAL
                                                defaultValue = reg.byteValue.get().toUDec().getRawUDecStr()
                                            }
                                        }

                                        onChange = { event ->

                                        }

                                        onBlur = { event ->

                                            // Set Value
                                            try {
                                                val newValue = event.currentTarget.value
                                                when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                                    HEX -> {
                                                        reg.byteValue.setHex(newValue)
                                                    }

                                                    BIN -> {
                                                        reg.byteValue.setBin(newValue)
                                                    }

                                                    DEC -> {
                                                        reg.byteValue.setDec(newValue)
                                                    }

                                                    UDEC -> {
                                                        reg.byteValue.setUDec(newValue)
                                                    }
                                                }
                                                setUpdate(!update)
                                                appLogic.getArch().getConsole().info("Register setValue: [${reg.byteValue.get().toDec().getDecStr()}|${reg.byteValue.get().toUDec().getUDecStr()}|${reg.byteValue.get().toHex().getHexStr()}|${reg.byteValue.get().toBin().getBinaryStr()}]")

                                            } catch (e: NumberFormatException) {
                                                console.warn("RegisterView reg onBlur: NumberFormatException")
                                            }

                                            // Get Actual Interpretation (for example padded binary number)
                                            try {
                                                when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                                    HEX -> {
                                                        event.currentTarget.value = reg.byteValue.get().toHex().getRawHexStr()
                                                    }

                                                    BIN -> {
                                                        event.currentTarget.value = reg.byteValue.get().toBin().getRawBinaryStr()
                                                    }

                                                    DEC -> {
                                                        event.currentTarget.value = reg.byteValue.get().toDec().getRawDecStr()
                                                    }

                                                    UDEC -> {
                                                        event.currentTarget.value = reg.byteValue.get().toDec().getRawDecStr()
                                                    }
                                                }
                                            } catch (e: NumberFormatException) {
                                                console.warn("RegisterView reg onBlur: NumberFormatException")
                                            }
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
                                    +reg.description
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    useEffect(currRegTypeIndex, change) {
        console.log("(part-update) RegisterView")
        val registers = if (currRegFileIndex < registerContainer.getRegisterFileList().size) {
            registerContainer.getRegisterFileList()[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            registerContainer.getRegisterFileList()[0]
        }
        registers.let {
            for (reg in it.registers) {
                val regID = it.registers.indexOf(reg)
                try {
                    val regRef = document.getElementById("reg0$regID") as HTMLInputElement
                    regRef.value = when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                        HEX -> {
                            reg.byteValue.get().toHex().getRawHexStr()
                        }

                        BIN -> {
                            reg.byteValue.get().toBin().getRawBinaryStr()
                        }

                        DEC -> {
                            reg.byteValue.get().toDec().getRawDecStr()
                        }

                        UDEC -> {
                            reg.byteValue.get().toUDec().getRawUDecStr()
                        }
                    }
                    regRef.style.width = when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                        HEX -> {
                            "${reg.byteValue.size.byteCount * 2}ch;"
                        }

                        BIN -> {
                            "${reg.byteValue.size.bitWidth}ch;"
                        }

                        DEC -> {
                            "auto;"
                        }

                        UDEC -> {
                            "auto;"
                        }
                    }

                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegTypeIndex): NumberFormatException")
                }
            }
        }

    }

    useEffect(change) {
        console.log("(update) RegisterView")
        val registerContainer = appLogic.getArch().getRegisterContainer()
        val registers = if (currRegFileIndex < registerContainer.getRegisterFileList().size) {
            registerContainer.getRegisterFileList()[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            registerContainer.getRegisterFileList()[0]
        }
        registers.let {
            for (reg in it.registers) {
                val regID = it.registers.indexOf(reg)
                try {
                    /*val regdec = document.getElementById("regdec${it.name}$regID") as HTMLInputElement
                    regdec.value = reg.byteValue.get().toDec().getRawDecStr()
                    val reghex = document.getElementById("reghex${it.name}$regID") as HTMLInputElement
                    reghex.value = reg.byteValue.get().toHex().getRawHexStr()*/
                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(change): NumberFormatException")
                }
            }
        }
    }
    useEffect(update) {
        console.log("(part-update) RegisterView")
    }

}

