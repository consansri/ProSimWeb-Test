package views.components

import AppLogic
import csstype.*
import emotion.react.css
import extendable.ArchConst
import extendable.ArchConst.RegTypes.*
import extendable.components.connected.RegisterContainer
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.get
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
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
    var currentRegFileIndex: Int
}

val RegisterView = FC<RegisterViewProps> { props ->

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val (currentRegFileIndex, setCurrentRegFileIndex) = useState<Int>(props.currentRegFileIndex)
    val (currentRegTypeIndex, setCurrentRegType) = useState(0)
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

            css {
                backgroundColor = Color("#333333")
                position = Position.relative
                display = Display.block
                alignContent = AlignContent.center
                paddingTop = 0.2.rem
            }

            button {
                css {
                    color = Color("#AAAAAA")
                    backgroundColor = Color("#AAAAAA00")
                    paddingLeft = 1.rem
                    paddingRight = 1.rem
                    border = Border(0.px, LineStyle.hidden)
                }

                +"Register"
            }

            val regFileList = appLogic.getArch().getRegisterContainer().getRegisterFileList()

            for (regFile in regFileList) {
                when (regFile.label) {
                    RegisterContainer.RegLabel.PC -> {

                    }

                    RegisterContainer.RegLabel.MAIN -> {
                        a {
                            css {
                                cursor = Cursor.pointer
                                color = Color("#000000")
                                appearance = Appearance.menulistButton
                                paddingLeft = 0.5.rem
                                paddingRight = 0.5.rem
                                borderTopLeftRadius = 0.2.rem
                                borderTopRightRadius = 0.2.rem
                                borderBottomLeftRadius = 0.rem
                                borderBottomRightRadius = 0.rem
                                borderTop = Border(2.px, LineStyle.solid, Color("#000"))
                                borderRight = Border(2.px, LineStyle.solid, Color("#000"))

                                backgroundColor = if (currentRegFileIndex == regFileList.indexOf(regFile)) {
                                    Color("#EEEEEE")
                                } else {
                                    Color("#999999")
                                }
                            }
                            title = "Show RegFile ${regFile.name}"
                            +regFile.name
                            onClick = {
                                setCurrentRegFileIndex(regFileList.indexOf(regFile))
                            }
                        }
                    }

                    RegisterContainer.RegLabel.SYSTEM -> {
                        a {
                            css {
                                cursor = Cursor.pointer
                                color = Color("#000000")
                                appearance = Appearance.menulistButton
                                paddingLeft = 0.5.rem
                                paddingRight = 0.5.rem
                                borderTopLeftRadius = 0.2.rem
                                borderTopRightRadius = 0.2.rem
                                borderBottomLeftRadius = 0.rem
                                borderBottomRightRadius = 0.rem
                                borderTop = Border(2.px, LineStyle.solid, Color("#000"))
                                borderRight = Border(2.px, LineStyle.solid, Color("#000"))
                                backgroundColor = if (currentRegFileIndex == regFileList.indexOf(regFile)) {
                                    Color("#EEEEEE")
                                } else {
                                    Color("#999999")
                                }
                            }
                            title = "Show RegFile ${regFile.name}"
                            +regFile.name

                            onClick = {
                                setCurrentRegFileIndex(regFileList.indexOf(regFile))
                            }
                        }
                    }

                    RegisterContainer.RegLabel.CUSTOM -> {
                        a {
                            css {
                                cursor = Cursor.pointer
                                color = Color("#000000")
                                appearance = Appearance.menulistButton
                                paddingLeft = 0.5.rem
                                paddingRight = 0.5.rem
                                borderTopLeftRadius = 0.2.rem
                                borderTopRightRadius = 0.2.rem
                                borderBottomLeftRadius = 0.rem
                                borderBottomRightRadius = 0.rem
                                borderTop = Border(2.px, LineStyle.solid, Color("#000"))
                                borderRight = Border(2.px, LineStyle.solid, Color("#000"))
                                backgroundColor = if (currentRegFileIndex == regFileList.indexOf(regFile)) {
                                    Color("#EEEEEE")
                                } else {
                                    Color("#999999")
                                }
                            }
                            title = "Show RegFile ${regFile.name}"

                            +regFile.name

                            onClick = {
                                setCurrentRegFileIndex(regFileList.indexOf(regFile))
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
                                    className = ClassName("dcf-txt-center")
                                    scope = "col"

                                    +ArchConst.REGISTER_VALUETYPES[currentRegTypeIndex].toString()

                                    onClick = { event ->
                                        if (currentRegTypeIndex < ArchConst.REGISTER_VALUETYPES.size - 1) {
                                            setCurrentRegType(currentRegTypeIndex + 1)
                                        } else {
                                            setCurrentRegType(0)
                                        }

                                        bodyRef.current?.blur()

                                        val valueCollection = document.getElementsByClassName("value-col")
                                        for(valueID in 0 until valueCollection.length){
                                            val valueTD = valueCollection[valueID]
                                            (valueTD?.firstChild as HTMLInputElement).blur()

                                        }


                                    }


                                }
                            }


                        }
                    }
                }


                val registerArray = registerContainer.getRegisterFileList()[currentRegFileIndex]

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

                                    val column = 0

                                    when (ArchConst.REGISTER_VALUETYPES[currentRegTypeIndex]) {
                                        HEX -> {
                                            input {

                                                id = "reg${column}${it.name}$regID"

                                                className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                                readOnly = false
                                                type = InputType.text
                                                pattern = "[0-9a-fA-F]+"
                                                placeholder = ArchConst.PRESTRING_HEX
                                                maxLength = reg.byteValue.size.byteCount * 2

                                                defaultValue = reg.byteValue.get().toHex().getRawHexStr()

                                                onChange = { event ->
                                                    try {
                                                        val newValue = event.currentTarget.value
                                                        reg.byteValue.setHex(newValue)
                                                        setUpdate(!update)
                                                        appLogic.getArch().getConsole().info("Register setValue: [${reg.byteValue.get().toDec().getDecStr()}|${reg.byteValue.get().toUDec().getUDecStr()}|${reg.byteValue.get().toHex().getHexStr()}|${reg.byteValue.get().toBinary().getBinaryStr()}]")

                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView reghex onChange: NumberFormatException")
                                                    }
                                                }

                                                onBlur = { event ->
                                                    try {
                                                        event.currentTarget.value = reg.byteValue.get().toHex().getRawHexStr()
                                                        /*val regdec = document.getElementById("regdec${it.name}$regID") as HTMLInputElement
                                                        regdec.value = reg.byteValue.get().toDec().getRawDecStr()*/
                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView reghex onBlur: NumberFormatException")
                                                    }
                                                }

                                                onKeyDown = { event ->
                                                    if (event.key == "Enter") {
                                                        event.currentTarget.blur()
                                                    }
                                                }


                                            }
                                        }

                                        BIN -> {
                                            input {

                                                id = "reg${column}${it.name}$regID"

                                                className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                                readOnly = false
                                                type = InputType.text
                                                pattern = "[01]+"
                                                placeholder = ArchConst.PRESTRING_BINARY
                                                maxLength = reg.byteValue.size.bitWidth

                                                defaultValue = reg.byteValue.get().toBinary().getRawBinaryStr()

                                                onChange = { event ->
                                                    try {
                                                        val newValue = event.currentTarget.value
                                                        reg.byteValue.setBin(newValue)
                                                        setUpdate(!update)
                                                        appLogic.getArch().getConsole().info("Register setValue: [${reg.byteValue.get().toDec().getDecStr()}|${reg.byteValue.get().toUDec().getUDecStr()}|${reg.byteValue.get().toHex().getHexStr()}|${reg.byteValue.get().toBinary().getBinaryStr()}]")

                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView reghex onChange: NumberFormatException")
                                                    }
                                                }

                                                onBlur = { event ->
                                                    try {
                                                        event.currentTarget.value = reg.byteValue.get().toBinary().getRawBinaryStr()
                                                        /*val regdec = document.getElementById("regdec${it.name}$regID") as HTMLInputElement
                                                        regdec.value = reg.byteValue.get().toDec().getRawDecStr()*/
                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView reghex onBlur: NumberFormatException")
                                                    }
                                                }

                                                onKeyDown = { event ->
                                                    if (event.key == "Enter") {
                                                        event.currentTarget.blur()
                                                    }
                                                }
                                            }
                                        }

                                        DEC -> {
                                            input {
                                                id = "reg${column}${it.name}$regID"

                                                className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                                readOnly = false
                                                type = InputType.number
                                                placeholder = ArchConst.PRESTRING_DECIMAL
                                                pattern = "-?\\d+"

                                                defaultValue = reg.byteValue.get().toDec().getRawDecStr()

                                                onChange = { event ->
                                                    try {
                                                        val newValue = event.currentTarget.value
                                                        reg.byteValue.setDec(newValue)
                                                        setUpdate(!update)

                                                        appLogic.getArch().getConsole().info("Register setValue: [${reg.byteValue.get().toDec().getDecStr()}|${reg.byteValue.get().toUDec().getUDecStr()}|${reg.byteValue.get().toHex().getHexStr()}|${reg.byteValue.get().toBinary().getBinaryStr()}]")

                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView regdec onChange: NumberFormatException")
                                                    }

                                                }

                                                onBlur = { event ->
                                                    try {
                                                        event.currentTarget.value = reg.byteValue.get().toDec().getRawDecStr()
                                                        /*val reghex = document.getElementById("reghex${it.name}$regID") as HTMLInputElement
                                                        reghex.value = reg.byteValue.get().toHex().getRawHexStr()*/
                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView regdec onBlur: NumberFormatException")
                                                    }
                                                }

                                                onKeyDown = { event ->
                                                    if (event.key == "Enter") {
                                                        event.currentTarget.blur()
                                                    }
                                                }

                                            }
                                        }

                                        UDEC -> {
                                            input {
                                                id = "reg${column}${it.name}$regID"

                                                className = ClassName(StyleConst.CLASS_TABLE_INPUT)

                                                readOnly = false
                                                type = InputType.number
                                                placeholder = ArchConst.PRESTRING_DECIMAL

                                                defaultValue = reg.byteValue.get().toUDec().getRawUDecStr()

                                                onChange = { event ->
                                                    try {
                                                        val newValue = event.currentTarget.value
                                                        reg.byteValue.setUDec(newValue)
                                                        setUpdate(!update)

                                                        appLogic.getArch().getConsole().info("Register setValue: [${reg.byteValue.get().toDec().getDecStr()}|${reg.byteValue.get().toUDec().getUDecStr()}|${reg.byteValue.get().toHex().getHexStr()}|${reg.byteValue.get().toBinary().getBinaryStr()}]")

                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView regdec onChange: NumberFormatException")
                                                    }

                                                }

                                                onBlur = { event ->
                                                    try {
                                                        event.currentTarget.value = reg.byteValue.get().toDec().getRawDecStr()
                                                        /*val reghex = document.getElementById("reghex${it.name}$regID") as HTMLInputElement
                                                        reghex.value = reg.byteValue.get().toHex().getRawHexStr()*/
                                                    } catch (e: NumberFormatException) {
                                                        console.warn("RegisterView regdec onBlur: NumberFormatException")
                                                    }
                                                }

                                                onKeyDown = { event ->
                                                    if (event.key == "Enter") {
                                                        event.currentTarget.blur()
                                                    }
                                                }

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

    useEffect(currentRegTypeIndex) {

    }

    useEffect(change) {
        console.log("(update) RegisterView")
        val registerContainer = appLogic.getArch().getRegisterContainer()
        val registers = if (currentRegFileIndex < registerContainer.getRegisterFileList().size) {
            registerContainer.getRegisterFileList()[currentRegFileIndex]
        } else {
            setCurrentRegFileIndex(0)
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

