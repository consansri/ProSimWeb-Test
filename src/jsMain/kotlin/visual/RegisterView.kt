package visual

import Settings
import StyleAttr
import debug.DebugTools
import emotion.react.css
import cengine.util.integer.Value.Types
import cengine.util.integer.Value.Types.*
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
import web.cssom.*
import web.dom.document
import web.html.HTMLButtonElement
import web.html.HTMLInputElement
import web.html.HTMLTableSectionElement
import web.html.InputType
import web.timers.setTimeout
import kotlin.time.measureTime

external interface RegisterViewProps : Props {
    var name: String
    var archState: StateInstance<emulator.kit.Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var hideAdditionalInfo: StateInstance<Boolean>
    var isFirst: Boolean
}

val RegisterView = FC<RegisterViewProps> { props ->

    val bodyRef = useRef<HTMLTableSectionElement>()
    val pcRef = useRef<HTMLButtonElement>()

    val arch = props.archState.component1()

    val (allRegFiles, setAllRegFiles) = useState(arch.getAllRegFiles())
    val (currRegFileIndex, setCurrRegFileIndex) = useState(0)
    val (currRegType, setCurrRegTypeIndex) = useState(Hex)

    /* DOM */

    div {

        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            position = Position.relative
            flex = number(1.0)

            width = 100.pct
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
                display = Display.flex
                flexDirection = FlexDirection.row
                alignItems = AlignItems.end
                flexWrap = FlexWrap.wrap

                alignContent = AlignContent.center
                backgroundColor = StyleAttr.Main.Processor.BgColor.get()

                button {
                    paddingTop = 0.5.rem
                }
            }

            if (!props.hideAdditionalInfo.component1() || props.isFirst) {
                button {
                    css {
                        color = StyleAttr.Main.Processor.FgColor.get()
                        backgroundColor = StyleAttr.transparent
                        paddingRight = StyleAttr.scrollBarSize
                        border = Border(0.px, LineStyle.hidden)
                        paddingTop = 0.5.rem
                    }

                    +"Register"
                }
            }

            for (regFile in allRegFiles.filter { it.getRegisters(arch.features).isNotEmpty() }) {
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

                        if (currRegFileIndex == allRegFiles.indexOf(regFile)) {
                            backgroundColor = important(StyleAttr.Main.Processor.TableBgColor.get())
                            boxShadow = important(BoxShadow(0.px, 0.px, 0.px, Color("#FFF")))
                            padding = important(Padding(0.2.rem, 0.5.rem))
                            color = important(StyleAttr.Main.Processor.TableFgColor.get())
                        }
                    }

                    title = "Show RegFile ${regFile.name}"

                    +regFile.name
                    onClick = {
                        setCurrRegFileIndex(allRegFiles.indexOf(regFile))
                    }
                }
            }

            if (!props.hideAdditionalInfo.component1() || !props.isFirst) {
                button {
                    ref = pcRef

                    css {
                        color = StyleAttr.Main.Processor.FgColor.get()
                        backgroundColor = StyleAttr.transparent
                        paddingLeft = StyleAttr.scrollBarSize
                        float = Float.right
                        border = Border(0.px, LineStyle.hidden)
                        paddingTop = 0.5.rem
                    }

                    +"PC: ${arch.regContainer.pc.variable.get().toHex()}"

                    onClick = { event ->
                        pcRef.current?.let {
                            it.innerText = "PC: ${arch.regContainer.pc.variable.get().toHex()}"
                        }
                    }
                }
            }
        }

        div {
            /*className = ClassName(StyleConst.Main.Table.CLASS_OVERFLOWXSCROLL)*/
            css {
                overflowY = Overflow.scroll
                maxHeight = StyleAttr.Main.Processor.MaxHeightReg
                borderRadius = StyleAttr.borderRadius
                marginRight =  -StyleAttr.scrollBarSize // center with scrollbar on the right

                StyleAttr.layoutSwitchMediaQuery {
                    marginRight =  0.px
                }
            }
            tabIndex = 0

            table {
                val registerArray = arch.getAllRegFiles()[currRegFileIndex]
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
                                    background = StyleAttr.Main.Processor.BtnBgColor.get()
                                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                                }

                                span {
                                    +currRegType.visibleName
                                }

                                onClick = { event ->
                                    setTimeout({
                                        if (currRegType.ordinal < Types.entries.size - 1) {
                                            setCurrRegTypeIndex(Types.entries[currRegType.ordinal + 1])
                                        } else {
                                            setCurrRegTypeIndex(Types.entries[0])
                                        }
                                    }, 0)
                                }
                            }
                        }
                        if (registerArray.hasPrivileges) {
                            th {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                +"Priv"
                            }
                        }
                        if (!props.hideAdditionalInfo.component1()) {
                            th {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                +"CC"
                            }
                            th {
                                className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                scope = "col"
                                +"Description"
                            }
                        }
                    }
                }

                tbody {
                    ref = bodyRef

                    val measuredRegTypeChange = measureTime {
                        registerArray.let { regFile ->
                            for (reg in regFile.getRegisters(props.archState.component1().features)) {
                                val regID = regFile.getRegisters(props.archState.component1().features).indexOf(reg)

                                tr {
                                    td {
                                        css {
                                            textAlign = TextAlign.left
                                            paddingLeft = StyleAttr.paddingSize
                                        }
                                        +reg.names.joinToString("\\") { it }
                                    }
                                    td {
                                        css {
                                            textAlign = TextAlign.left
                                            paddingLeft = StyleAttr.paddingSize
                                        }
                                        +reg.aliases.joinToString("\\") { it }
                                    }
                                    td {
                                        className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)

                                        input {
                                            id = "${props.name}${regFile.name}${regID}"
                                            readOnly = false
                                            setTimeout({
                                                if (reg.containsFlags) {
                                                    type = InputType.text
                                                    pattern = "[01]+"
                                                    placeholder = Settings.PRESTRING_BINARY
                                                    maxLength = reg.variable.size.bitWidth
                                                    defaultValue = reg.variable.get().toBin().toRawString()
                                                } else {
                                                    when (currRegType) {
                                                        Hex -> {
                                                            type = InputType.text
                                                            pattern = "[0-9a-fA-F]+"
                                                            placeholder = Settings.PRESTRING_HEX
                                                            maxLength = reg.variable.size.getByteCount() * 2
                                                            defaultValue = reg.variable.get().toHex().toRawString()
                                                        }

                                                        Bin -> {
                                                            type = InputType.text
                                                            pattern = "[01]+"
                                                            placeholder = Settings.PRESTRING_BINARY
                                                            maxLength = reg.variable.size.bitWidth
                                                            defaultValue = reg.variable.get().toBin().toRawString()
                                                        }

                                                        Dec -> {
                                                            type = InputType.number
                                                            pattern = "-?\\d+"
                                                            placeholder = Settings.PRESTRING_DECIMAL
                                                            defaultValue = reg.variable.get().toDec().toRawString()
                                                        }

                                                        UDec -> {
                                                            type = InputType.number
                                                            placeholder = Settings.PRESTRING_DECIMAL
                                                            defaultValue = reg.variable.get().toUDec().toRawString()
                                                        }
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
                                                            if (reg.containsFlags) {
                                                                reg.variable.setBin(newValue)
                                                            } else {
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
                                                            }

                                                            console.info("Register setValue: [${reg.variable.get().toDec()}|${reg.variable.get().toUDec()}|${reg.variable.get().toHex()}|${reg.variable.get().toBin()}]")
                                                        } catch (e: NumberFormatException) {
                                                            console.warn("RegisterView reg onBlur: NumberFormatException")
                                                        }

                                                        // Get Actual Interpretation (for example padded binary number)
                                                        try {
                                                            if (reg.containsFlags) {
                                                                currentTarget.value = reg.variable.get().toBin().toRawString()
                                                            } else {
                                                                when (currRegType) {
                                                                    Hex -> {
                                                                        currentTarget.value = reg.variable.get().toHex().toRawString()
                                                                    }

                                                                    Bin -> {
                                                                        currentTarget.value = reg.variable.get().toBin().toRawString()
                                                                    }

                                                                    Dec -> {
                                                                        currentTarget.value = reg.variable.get().toDec().toRawString()
                                                                    }

                                                                    UDec -> {
                                                                        currentTarget.value = reg.variable.get().toUDec().toRawString()
                                                                    }
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
                                    if (registerArray.hasPrivileges) {
                                        td {
                                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                            if (reg.privilegeID != null) {
                                                +reg.privilegeID
                                            } else {
                                                +"-"
                                            }
                                        }
                                    }

                                    if (!props.hideAdditionalInfo.component1()) {
                                        td {
                                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_CENTER)
                                            +reg.callingConvention.displayName
                                        }
                                        td {
                                            className = ClassName(StyleAttr.Main.Table.CLASS_TXT_LEFT)
                                            +reg.description
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (DebugTools.REACT_showUpdateInfo) {
                        console.log("REACT: RegisterView RegTypeChange took ${measuredRegTypeChange.inWholeMilliseconds} ms!")
                    }
                }
            }
        }
    }

    useEffect(props.hideAdditionalInfo) {

    }

    useEffect(currRegFileIndex) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) RegisterView")
        }
        val registers = if (currRegFileIndex < allRegFiles.size) {
            allRegFiles[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            allRegFiles[0]
        }
        registers.let { regFile ->
            for (reg in regFile.getRegisters(props.archState.component1().features)) {
                val regID = regFile.getRegisters(props.archState.component1().features).indexOf(reg)
                try {
                    val regRef = document.getElementById("${props.name}${regFile.name}$regID") as HTMLInputElement?
                    regRef?.value = if (reg.containsFlags) reg.variable.get().toBin().toRawString() else {
                        when (currRegType) {
                            Hex -> {
                                reg.variable.get().toHex().toRawString()
                            }

                            Bin -> {
                                reg.variable.get().toBin().toRawString()
                            }

                            Dec -> {
                                reg.variable.get().toDec().toRawString()
                            }

                            UDec -> {
                                reg.variable.get().toUDec().toRawString()
                            }
                        }
                    }
                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegType): NumberFormatException")
                }
            }
        }
        pcRef.current?.let {
            it.innerText = "PC: ${arch.regContainer.pc.variable.get().toHex()}"
        }
    }

    useEffect(currRegType) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) RegisterView")
        }
        val registers = if (currRegFileIndex < allRegFiles.size) {
            allRegFiles[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            allRegFiles[0]
        }
        registers.let { regFile ->
            for (reg in regFile.getRegisters(props.archState.component1().features)) {
                val regID = regFile.getRegisters(props.archState.component1().features).indexOf(reg)
                try {
                    val regRef = document.getElementById("${props.name}${regFile.name}$regID") as HTMLInputElement?
                    regRef?.value = if (reg.containsFlags) reg.variable.get().toBin().toRawString() else {
                        when (currRegType) {
                            Hex -> {
                                reg.variable.get().toHex().toRawString()
                            }

                            Bin -> {
                                reg.variable.get().toBin().toRawString()
                            }

                            Dec -> {
                                reg.variable.get().toDec().toRawString()
                            }

                            UDec -> {
                                reg.variable.get().toUDec().toRawString()
                            }
                        }
                    }

                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegType): NumberFormatException")
                }
            }
        }
        pcRef.current?.let {
            it.innerText = "PC: ${arch.regContainer.pc.variable.get().toHex()}"
        }
    }

    useEffect(props.exeEventState) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        val registers = if (currRegFileIndex < allRegFiles.size) {
            allRegFiles[currRegFileIndex]
        } else {
            setCurrRegFileIndex(0)
            allRegFiles[0]
        }
        registers.let { regFile ->
            for (reg in regFile.getRegisters(props.archState.component1().features)) {
                val regID = regFile.getRegisters(props.archState.component1().features).indexOf(reg)
                try {
                    val regRef = document.getElementById("${props.name}${regFile.name}$regID") as HTMLInputElement?
                    regRef?.value = if(reg.containsFlags) reg.variable.get().toBin().toRawString() else {
                        when (currRegType) {
                            Hex -> {
                                reg.variable.get().toHex().toRawString()
                            }

                            Bin -> {
                                reg.variable.get().toBin().toRawString()
                            }

                            Dec -> {
                                reg.variable.get().toDec().toRawString()
                            }

                            UDec -> {
                                reg.variable.get().toUDec().toRawString()
                            }
                        }
                    }
                } catch (e: NumberFormatException) {
                    console.warn("RegisterView useEffect(currRegType): NumberFormatException")
                }
            }
        }
        pcRef.current?.let {
            it.innerText = "PC: ${arch.regContainer.pc.variable.get().toHex()}"
        }
    }

}

