package visual

import emulator.Emulator
import emotion.react.css
import emulator.kit.Settings
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
import debug.DebugTools
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.Types.*
import kotlin.time.measureTime
import web.html.*
import web.timers.*
import web.cssom.*

external interface RegisterViewProps : Props {
    var name: String
    var emulator: Emulator
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)
}

val RegisterView = FC<RegisterViewProps> { props ->

    val bodyRef = useRef<HTMLTableSectionElement>()
    val pcRef = useRef<HTMLButtonElement>()
    val pcRefreshInterval = useRef<Timeout>()

    val appLogic by useState(props.emulator)
    val name by useState(props.name)
    val regFileList = appLogic.getArch().getRegisterContainer().getRegisterFileList()
    val (currRegFileIndex, setCurrRegFileIndex) = useState<Int>(regFileList.size - 1)
    val (currRegType, setCurrRegTypeIndex) = useState<Variable.Value.Types>(Variable.Value.Types.Hex)
    val (update, setUpdate) = useState(false)
    val change = props.update

    val registerContainer = appLogic.getArch().getRegisterContainer()

    // INTERVALS

    pcRefreshInterval.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        pcRefreshInterval.current = setInterval({
            pcRef.current?.let {
                it.innerText = "PC: ${registerContainer.pc.value.get().toHex().getHexStr()}"
            }
        }, 100)
    }


    /* DOM */

    div {

        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            position = Position.relative
            /*overflowY = Overflow.scroll*/

            table {
                backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()
                color = StyleAttr.Main.Processor.TableFgColor.get()

                input {
                    color = StyleAttr.Main.Processor.TableFgColor.get()
                }
            }
        }

        div {
            css(ClassName("dcf-tabs")) {
                border = Border(0.px, LineStyle.hidden)
                position = Position.relative
                display = Display.block
                alignContent = AlignContent.center
                backgroundColor = StyleAttr.Main.Processor.BgColor.get()

                button {
                    paddingTop = 0.5.rem
                }
            }


            button {
                css {
                    color = StyleAttr.Main.Processor.FgColor.get()
                    backgroundColor = StyleAttr.transparent
                    paddingLeft = 1.rem
                    paddingRight = 1.rem
                    border = Border(0.px, LineStyle.hidden)
                    paddingTop = 0.5.rem
                }

                +"Register"
            }

            for (regFile in regFileList) {
                a {

                    css {
                        display = Display.inlineBlock
                        cursor = Cursor.pointer
                        borderTopLeftRadius = StyleAttr.borderRadius
                        borderTopRightRadius = StyleAttr.borderRadius
                        marginRight = 0.5.rem
                        transition = Transition(TransitionProperty.all, 0.05.s, TransitionTimingFunction.ease)

                        backgroundColor = StyleAttr.Main.Processor.TabBgColor.get()
                        boxShadow = BoxShadow(0.px, 0.1.rem, 0.1.rem, 0.px, StyleAttr.Main.Processor.TabBgColor.get())
                        padding = Padding(0.1.rem, 0.5.rem)
                        color = StyleAttr.Main.Processor.TabFgColor.get()

                        if (currRegFileIndex == regFileList.indexOf(regFile)) {
                            backgroundColor = important(StyleAttr.Main.Processor.TableBgColor.get())
                            boxShadow = important(BoxShadow(0.px, 0.px, 0.px, Color("#FFF")))
                            padding = important(Padding(0.2.rem, 0.5.rem))
                            color = important(StyleAttr.Main.Processor.TableFgColor.get())
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
                    color = StyleAttr.Main.Processor.FgColor.get()
                    backgroundColor = StyleAttr.transparent
                    paddingLeft = 1.rem
                    paddingRight = 1.rem
                    float = Float.right
                    border = Border(0.px, LineStyle.hidden)
                    paddingTop = 0.5.rem
                }

                +"PC: ${registerContainer.pc.value.get().toHex().getHexStr()}"

                onClick = { event ->
                    appLogic.getArch().getRegisterContainer().pc
                    setUpdate(!update)
                }
            }
        }

        div {
            /*className = ClassName(StyleConst.Main.Table.CLASS_OVERFLOWXSCROLL)*/
            css {
                overflowY = Overflow.scroll
                maxHeight = StyleAttr.Main.Processor.MaxHeightReg
                borderRadius = StyleAttr.borderRadius
                paddingLeft = 12.px // center with scrollbar on the right
            }
            tabIndex = 0

            table {
                val registerArray = registerContainer.getRegisterFileList()[currRegFileIndex]
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
                            colSpan = 2
                            +"Registers"
                        }

                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"

                            button {
                                css {
                                    background = StyleAttr.Main.Processor.BgColor.get()
                                    color = StyleAttr.Main.Processor.FgColor.get()
                                }

                                span {
                                    +currRegType.visibleName
                                }

                                onClick = { event ->
                                    setTimeout({
                                        if (currRegType.ordinal < Variable.Value.Types.entries.size - 1) {
                                            setCurrRegTypeIndex(Variable.Value.Types.entries[currRegType.ordinal + 1])
                                        } else {
                                            setCurrRegTypeIndex(Variable.Value.Types.entries[0])
                                        }
                                    }, 0)
                                }
                            }

                        }
                        th {
                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                            scope = "col"
                            +"Description"
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
                                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                        +reg.names.joinToString("\\") { it }
                                    }
                                    td {
                                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                        +reg.aliases.joinToString("\\") { it }
                                    }
                                    td {
                                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)

                                        input {
                                            id = "reg0${regID}"
                                            readOnly = false
                                            setTimeout({
                                                when (currRegType) {
                                                    Hex -> {
                                                        type = InputType.text
                                                        pattern = "[0-9a-fA-F]+"
                                                        placeholder = Settings.PRESTRING_HEX
                                                        maxLength = reg.variable.size.getByteCount() * 2
                                                        defaultValue = reg.variable.get().toHex().getRawHexStr()
                                                    }

                                                    Bin -> {
                                                        type = InputType.text
                                                        pattern = "[01]+"
                                                        placeholder = Settings.PRESTRING_BINARY
                                                        maxLength = reg.variable.size.bitWidth
                                                        defaultValue = reg.variable.get().toBin().getRawBinaryStr()
                                                    }

                                                    Dec -> {
                                                        type = InputType.number
                                                        pattern = "-?\\d+"
                                                        placeholder = Settings.PRESTRING_DECIMAL
                                                        defaultValue = reg.variable.get().toDec().getRawDecStr()
                                                    }

                                                    UDec -> {
                                                        type = InputType.number
                                                        placeholder = Settings.PRESTRING_DECIMAL
                                                        defaultValue = reg.variable.get().toUDec().getRawUDecStr()
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

                                                            when (currRegType) {
                                                                Hex -> {
                                                                    reg.variable.setHex(newValue)
                                                                }

                                                                Bin -> {
                                                                    reg.variable.setBin(newValue)
                                                                }

                                                                Dec -> {
                                                                    reg.variable.setDec(newValue)
                                                                }

                                                                UDec -> {
                                                                    reg.variable.setUDec(newValue)
                                                                }
                                                            }
                                                            setUpdate(!update)
                                                            appLogic.getArch().getConsole().info("Register setValue: [${reg.variable.get().toDec().getDecStr()}|${reg.variable.get().toUDec().getUDecStr()}|${reg.variable.get().toHex().getHexStr()}|${reg.variable.get().toBin().getBinaryStr()}]")
                                                        } catch (e: NumberFormatException) {
                                                            console.warn("RegisterView reg onBlur: NumberFormatException")
                                                        }

                                                        // Get Actual Interpretation (for example padded binary number)
                                                        try {
                                                            when (currRegType) {
                                                                Hex -> {
                                                                    currentTarget.value = reg.variable.get().toHex().getRawHexStr()
                                                                }

                                                                Bin -> {
                                                                    currentTarget.value = reg.variable.get().toBin().getRawBinaryStr()
                                                                }

                                                                Dec -> {
                                                                    currentTarget.value = reg.variable.get().toDec().getRawDecStr()
                                                                }

                                                                UDec -> {
                                                                    currentTarget.value = reg.variable.get().toUDec().getRawUDecStr()
                                                                }
                                                            }
                                                        } catch (e: NumberFormatException) {
                                                            console.warn("RegisterView reg onBlur: NumberFormatException")
                                                        }


                                                    }
                                                    console.log("Blur Event took ${measuredTime.inWholeMilliseconds} ms editing in ${currRegType.name} type")
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
                                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
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

    useEffect(currRegType, change) {
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
                    val regRef = document.getElementById("reg0$regID") as HTMLInputElement?
                    regRef?.value = when (currRegType) {
                        Hex -> {
                            reg.variable.get().toHex().getRawHexStr()
                        }

                        Bin -> {
                            reg.variable.get().toBin().getRawBinaryStr()
                        }

                        Dec -> {
                            reg.variable.get().toDec().getRawDecStr()
                        }

                        UDec -> {
                            reg.variable.get().toUDec().getRawUDecStr()
                        }
                    }
                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegType): NumberFormatException")
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

