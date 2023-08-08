package views.components

import AppLogic
import emotion.react.css
import extendable.ArchConst
import extendable.ArchConst.RegTypes.*
import kotlinx.browser.document
import react.*
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
import tools.DebugTools
import kotlin.time.measureTime
import web.html.*
import web.timers.*
import web.cssom.*

external interface RegisterViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val bodyRef = useRef<HTMLTableSectionElement>()
    val pcRef = useRef<HTMLButtonElement>()

    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val regFileList = appLogic.getArch().getRegisterContainer().getRegisterFileList()
    val (currRegFileIndex, setCurrRegFileIndex) = useState<Int>(regFileList.size - 1)
    val (currRegTypeIndex, setCurrRegTypeIndex) = useState<Int>(1)
    val (update, setUpdate) = useState(false)
    val change = props.update
    val theaders = ArchConst.REGISTER_HEADERS

    val registerContainer = appLogic.getArch().getRegisterContainer()

    /* DOM */

    div {

        css {
            display = Display.block
            overflowY = Overflow.scroll
            maxHeight = 40.vh

            table {
                backgroundColor = StyleConst.Main.tableRegBgColor.get()

                input {
                    color = StyleConst.Main.FgColor.get()
                }

                "dcf-tabs" {
                    border = Border(0.px, LineStyle.hidden)
                }


            }
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
                a {
                    if (currRegFileIndex == regFileList.indexOf(regFile)) {
                        css {
                            backgroundColor = important(StyleConst.Main.tableRegBgColor.get())
                            boxShadow = important(BoxShadow(0.px, 0.px, 0.px, Color("#FFF")))
                            padding = important(Padding(0.2.rem,0.5.rem))
                            color = important(StyleConst.Main.FgColor.get())
                        }
                    }

                    title = "Show RegFile ${regFile.name}"

                    +regFile.name
                    onClick = {
                        setCurrRegFileIndex(regFileList.indexOf(regFile))
                    }
                }
            }

            button {
                ref = pcRef

                css {
                    color = Color("#FFFFFF")
                    backgroundColor = Color("#AAAAAA00")
                    paddingLeft = 1.rem
                    paddingRight = 1.rem
                    float = Float.right
                    border = Border(0.px, LineStyle.hidden)
                }

                +"PC: ${registerContainer.pc.value.get().toHex().getHexStr()}"

                onClick = { event ->
                    appLogic.getArch().getRegisterContainer().pc
                    setUpdate(!update)
                }
            }
        }

        div {
            className = ClassName("dcf-overflow-x-auto")
            tabIndex = 0

            table {
                className = ClassName("dcf-table dcf-w-100%")

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
                                        setTimeout({
                                            if (currRegTypeIndex < ArchConst.REGISTER_VALUETYPES.size - 1) {
                                                setCurrRegTypeIndex(currRegTypeIndex + 1)
                                            } else {
                                                setCurrRegTypeIndex(0)
                                            }
                                        }, 0)
                                    }

                                }
                            }
                        }
                    }
                }

                tbody {
                    ref = bodyRef

                    val measuredRegTypeChange = measureTime {
                        registerArray.let {
                            for (reg in it.registers) {
                                val regID = it.registers.indexOf(reg)

                                tr {

                                    td {
                                        className = ClassName("dcf-txt-left")
                                        +(" " + reg.names.joinToString("\t") { it })
                                    }
                                    td {
                                        className = ClassName("value-col dcf-txt-center")

                                        input {
                                            id = "reg0${regID}"
                                            className = ClassName(StyleConst.CLASS_TABLE_INPUT)
                                            readOnly = false
                                            setTimeout({
                                                when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                                    HEX -> {
                                                        type = InputType.text
                                                        pattern = "[0-9a-fA-F]+"
                                                        placeholder = ArchConst.PRESTRING_HEX
                                                        maxLength = reg.mutVal.size.byteCount * 2
                                                        defaultValue = reg.mutVal.get().toHex().getRawHexStr()
                                                    }

                                                    BIN -> {
                                                        type = InputType.text
                                                        pattern = "[01]+"
                                                        placeholder = ArchConst.PRESTRING_BINARY
                                                        maxLength = reg.mutVal.size.bitWidth
                                                        defaultValue = reg.mutVal.get().toBin().getRawBinaryStr()
                                                    }

                                                    DEC -> {
                                                        type = InputType.number
                                                        pattern = "-?\\d+"
                                                        placeholder = ArchConst.PRESTRING_DECIMAL
                                                        defaultValue = reg.mutVal.get().toDec().getRawDecStr()
                                                    }

                                                    UDEC -> {
                                                        type = InputType.number
                                                        placeholder = ArchConst.PRESTRING_DECIMAL
                                                        defaultValue = reg.mutVal.get().toUDec().getRawUDecStr()
                                                    }
                                                }
                                            }, 0)

                                            onBlur = { event ->

                                                // Set Value
                                                val newValue = event.currentTarget.value
                                                val currentTarget = event.currentTarget

                                                setTimeout({
                                                    val measuredTime = measureTime {
                                                        try {

                                                            when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                                                HEX -> {
                                                                    reg.mutVal.setHex(newValue)
                                                                }

                                                                BIN -> {
                                                                    reg.mutVal.setBin(newValue)
                                                                }

                                                                DEC -> {
                                                                    reg.mutVal.setDec(newValue)
                                                                }

                                                                UDEC -> {
                                                                    reg.mutVal.setUDec(newValue)
                                                                }
                                                            }
                                                            setUpdate(!update)
                                                            appLogic.getArch().getConsole().info("Register setValue: [${reg.mutVal.get().toDec().getDecStr()}|${reg.mutVal.get().toUDec().getUDecStr()}|${reg.mutVal.get().toHex().getHexStr()}|${reg.mutVal.get().toBin().getBinaryStr()}]")
                                                        } catch (e: NumberFormatException) {
                                                            console.warn("RegisterView reg onBlur: NumberFormatException")
                                                        }

                                                        // Get Actual Interpretation (for example padded binary number)
                                                        try {
                                                            when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                                                                HEX -> {
                                                                    currentTarget.value = reg.mutVal.get().toHex().getRawHexStr()
                                                                }

                                                                BIN -> {
                                                                    currentTarget.value = reg.mutVal.get().toBin().getRawBinaryStr()
                                                                }

                                                                DEC -> {
                                                                    currentTarget.value = reg.mutVal.get().toDec().getRawDecStr()
                                                                }

                                                                UDEC -> {
                                                                    currentTarget.value = reg.mutVal.get().toUDec().getRawUDecStr()
                                                                }
                                                            }
                                                        } catch (e: NumberFormatException) {
                                                            console.warn("RegisterView reg onBlur: NumberFormatException")
                                                        }


                                                    }
                                                    console.log("Blur Event took ${measuredTime.inWholeMilliseconds} ms editing in ${ArchConst.REGISTER_VALUETYPES[currRegTypeIndex].name} type")
                                                }, 0)

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
                    if (DebugTools.REACT_showUpdateInfo) {
                        console.log("RegisterView RegTypeChange took ${measuredRegTypeChange.inWholeMilliseconds} ms")
                    }
                }
            }
        }
    }

    useEffect(currRegTypeIndex, change) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(part-update) RegisterView")
        }
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
                            reg.mutVal.get().toHex().getRawHexStr()
                        }

                        BIN -> {
                            reg.mutVal.get().toBin().getRawBinaryStr()
                        }

                        DEC -> {
                            reg.mutVal.get().toDec().getRawDecStr()
                        }

                        UDEC -> {
                            reg.mutVal.get().toUDec().getRawUDecStr()
                        }
                    }
                    /*regRef.style.width = when (ArchConst.REGISTER_VALUETYPES[currRegTypeIndex]) {
                        HEX -> {
                            "${reg.mutVal.size.byteCount * 2}ch;"
                        }

                        BIN -> {
                            "${reg.mutVal.size.bitWidth}ch;"
                        }

                        DEC -> {
                            "auto;"
                        }

                        UDEC -> {
                            "auto;"
                        }
                    }*/

                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegTypeIndex): NumberFormatException")
                }
            }
        }

    }

    useEffect(change) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) RegisterView")
        }
        val regCont = appLogic.getArch().getRegisterContainer()
        val registers = if (currRegFileIndex < regCont.getRegisterFileList().size) {
            regCont.getRegisterFileList()[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            regCont.getRegisterFileList()[0]
        }
    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(part-update) RegisterView")
        }
        pcRef.current?.let {
            it.innerText = "PC: ${registerContainer.pc.value.get().toHex().getHexStr()}"
        }
    }

}

