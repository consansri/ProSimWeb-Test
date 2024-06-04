package visual

import Keys
import Settings
import StyleAttr
import emotion.react.css
import emulator.kit.MicroSetup
import emulator.kit.common.memory.DMCache
import emulator.kit.common.memory.FACache
import emulator.kit.common.memory.MainMemory
import emulator.kit.common.memory.Memory
import emulator.kit.types.Variable.Value.Hex
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.caption
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.table
import visual.memory.DMCacheView
import visual.memory.FACacheView
import visual.memory.MainMemoryView
import web.cssom.*
import web.html.ButtonType
import web.html.HTMLInputElement
import web.html.InputType

external interface MemViewProps : Props {
    var name: String
    var archState: StateInstance<emulator.kit.Architecture>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var hideRegDescr: StateInstance<Boolean>
    var length: Int
}

val MemoryView = FC<MemViewProps> { props ->

    val inputLengthRef = useRef<HTMLInputElement>()
    val editRef = useRef<HTMLInputElement>()

    val (memEndianess, setEndianess) = useState<Memory.Endianess>()
    val (lowFirst, setLowFirst) = useState(true)

    val (showDefMemSettings, setShowDefMemSettings) = useState(false)
    val (currMem, setCurrMem) = useState(MicroSetup.getMemoryInstances().firstOrNull())

    val (useBounds, setUseBounds) = useState(localStorage.getItem("${Keys.MIO_ACTIVE}-${props.archState.component1().description.name}")?.toBooleanStrictOrNull() ?: (props.archState.component1().memory.ioBounds != null))
    val (startAddr, setStartAddr) = useState(localStorage.getItem("${Keys.MIO_START}-${props.archState.component1().description.name}")?.let { Hex(it, props.archState.component1().memory.addressSize) } ?: props.archState.component1().memory
        .ioBounds?.lowerAddr?.toHex())
    val (amount, setAmount) = useState(localStorage.getItem("${Keys.MIO_AMOUNT}-${props.archState.component1().description.name}")?.toLongOrNull() ?: props.archState.component1().memory.ioBounds?.amount ?: 32)

    val (editVar, setEditVar) = useState<MainMemory.MemInstance.EditableValue>()

    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            justifyContent = JustifyContent.stretch
            alignItems = AlignItems.stretch
            position = Position.relative
            gap = StyleAttr.paddingSize
            height = 100.pct

            table {
                backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()
                color = StyleAttr.Main.Processor.TableFgColor.get()

                caption {
                    color = StyleAttr.Main.Processor.TableFgColor.get()
                }

                input {
                    color = StyleAttr.Main.Processor.TableFgColor.get()
                }
            }
        }

        div {
            css {
                display = Display.flex
                width = 100.pct
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.flexEnd
                alignItems = AlignItems.center
                gap = 1.px
                paddingLeft = 12.px
                paddingRight = 12.px
                flexGrow = number(0.0)

                "input[type=number]" {
                    display = Display.inlineBlock
                    border = Border(0.px, LineStyle.hidden)
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    flexGrow = number(1.0)
                    float = Float.left
                    verticalAlign = VerticalAlign.middle
                    borderRadius = 0.px
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                }

                select {
                    background = StyleAttr.Main.Processor.TableBgColor.get()
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    fontSize = important(StyleAttr.Main.Table.FontSizeSelect)
                    fontWeight = FontWeight.lighter
                    borderRadius = 0.px
                    cursor = Cursor.pointer
                }

                button {
                    display = Display.inlineBlock
                    cursor = Cursor.pointer
                    padding = StyleAttr.iconPadding
                    float = Float.left
                    color = StyleAttr.Main.Processor.BtnFgColor.get()
                    backgroundColor = StyleAttr.Main.Processor.TableBgColor.get()

                    a {
                        padding = StyleAttr.paddingSize
                    }

                    img {
                        display = Display.block
                        height = StyleAttr.iconSize
                        filter = important(StyleAttr.Main.Processor.BtnFgFilter.get())
                    }
                }
            }

            button {
                css {
                    borderTopLeftRadius = StyleAttr.iconBorderRadius
                    borderBottomLeftRadius = StyleAttr.iconBorderRadius
                }
                type = ButtonType.button

                onClick = {
                    setShowDefMemSettings(!showDefMemSettings)
                }

                img {
                    src = StyleAttr.Icons.edit
                }
            }

            /*button {
                type = ButtonType.button

                onClick = {
                    setMemList(props.archState.component1().memory.getMemList())
                }

                img {
                    src = StyleAttr.Icons.refresh
                }
            }*/

            button {
                type = ButtonType.button
                onClick = {
                    props.hideRegDescr.component2().invoke(!props.hideRegDescr.component1())
                }

                img {
                    src = if (props.hideRegDescr.component1()) {
                        StyleAttr.Icons.combine_view
                    } else {
                        StyleAttr.Icons.split_view
                    }
                }
            }

            select {

                defaultValue = props.archState.component1().memory.endianess.name

                for (entry in Memory.Endianess.entries) {
                    option {
                        value = entry.name
                        +entry.uiName
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
                type = InputType.number
                min = 1.0
                max = 16.0
                step = 1.0
                defaultValue = props.archState.component1().memory.entrysInRow

                onInput = { event ->
                    MicroSetup.getMemoryInstances().forEach {
                        if (it is MainMemory) it.entrysInRow = event.currentTarget.valueAsNumber.toInt()
                    }
                    localStorage.setItem(Keys.MEM_LENGTH, "${event.currentTarget.valueAsNumber.toInt()}")
                }
            }

            button {
                type = ButtonType.button
                css {
                    borderTopRightRadius = StyleAttr.iconBorderRadius
                    borderBottomRightRadius = StyleAttr.iconBorderRadius
                }

                onClick = { _ ->
                    setLowFirst(!lowFirst)
                }

                img {
                    src = StyleAttr.Icons.reverse
                }
            }
        }

        // TABS
        div {
            css {
                background = StyleAttr.Main.Processor.TableBgColor.get()
                borderRadius = StyleAttr.borderRadius
                display = Display.flex
                width = 100.pct - 24.px
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.flexStart
                alignItems = AlignItems.center
                gap = 1.px
                marginLeft = 12.px
                marginRight = 12.px
                flexGrow = number(0.0)
            }

            MicroSetup.getMemoryInstances().forEachIndexed { i, mem ->
                a {
                    css {
                        padding = StyleAttr.paddingSize
                        cursor = Cursor.pointer
                        if (i == 0) {
                            borderTopLeftRadius = StyleAttr.borderRadius
                            borderBottomLeftRadius = StyleAttr.borderRadius
                        }
                        if (i == MicroSetup.getMemoryInstances().size - 1) {
                            borderTopRightRadius = StyleAttr.borderRadius
                            borderBottomRightRadius = StyleAttr.borderRadius
                        }
                        if (mem == currMem) textDecoration = TextDecoration.underline

                        hover {
                            textDecoration = TextDecoration.underline
                        }
                    }
                    +mem.name

                    onClick = {
                        setCurrMem(mem)
                    }
                }
            }
        }

        when (currMem) {
            is DMCache -> {
                DMCacheView {
                    this.key = "${currMem::class.simpleName}"
                    this.cache = currMem
                    this.exeEventState = props.exeEventState
                    this.archState = props.archState
                }
            }

            is MainMemory -> {
                MainMemoryView {
                    this.key = "${currMem::class.simpleName}"
                    this.memory = currMem
                    this.archState = props.archState
                    this.exeEventState = props.exeEventState
                    this.lowFirst = lowFirst
                }
            }
            is FACache -> {
                FACacheView {
                    this.key = "${currMem::class.simpleName}"
                    this.cache = currMem
                    this.exeEventState = props.exeEventState
                    this.archState = props.archState
                }
            }

            null -> {}
        }
    }

    if (editVar != null) {
        div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setEditVar(null)
                }
            }
            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                ReactHTML.label {
                    htmlFor = "editField"
                    +"New Value"
                }

                input {
                    ref = editRef
                    id = "editField"
                    type = InputType.text
                    pattern = "[0-9a-fA-F]+"
                    placeholder = Settings.PRESTRING_HEX
                    defaultValue = editVar.variable.get().toHex().getRawHexStr()

                    onChange = {
                        val hex = Hex(it.currentTarget.value, props.archState.component1().memory.instanceSize)
                        if (hex.checkResult.valid) {
                            editVar.variable.set(hex)
                        } else {
                            it.currentTarget.value = editVar.variable.get().toHex().getRawHexStr()
                        }
                    }
                    onKeyDown = {
                        if (it.key == "Enter") {
                            setEditVar(null)
                        }
                    }
                }
            }

        }
    }

    if (showDefMemSettings) {
        div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setShowDefMemSettings(false)
                }
            }

            h1 {
                +"IO"
            }

            div {
                className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                ReactHTML.label {
                    htmlFor = "enableIO"
                    +"Use IO Section"
                }
                input {
                    id = "enableIO"
                    type = InputType.checkbox
                    checked = useBounds

                    onChange = {
                        setUseBounds(it.currentTarget.checked)
                    }
                }
            }
            if (useBounds) {
                div {
                    className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                    ReactHTML.label {
                        htmlFor = "ioStart"
                        +"Starting Address"
                    }

                    input {
                        id = "ioStart"
                        type = InputType.text
                        pattern = "[0-9a-fA-F]+"
                        placeholder = Settings.PRESTRING_HEX
                        defaultValue = startAddr?.getRawHexStr() ?: ""

                        onChange = {
                            setStartAddr(if (it.currentTarget.value != "") Hex(it.currentTarget.value, props.archState.component1().memory.addressSize) else null)
                        }
                    }
                }

                div {
                    className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                    ReactHTML.label {
                        htmlFor = "ioAmount"
                        +"Amount"
                    }

                    input {
                        id = "ioAmount"
                        type = InputType.number
                        defaultValue = amount

                        onChange = {
                            setAmount(it.currentTarget.valueAsNumber.toLong())
                        }
                    }
                }
            }
        }

    }

    /*useEffect(memList) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Refresh Memory Table!")
        }
    }*/

    useEffect(useBounds, startAddr, amount) {
        if (useBounds) {
            startAddr?.let {
                if (it.checkResult.valid) props.archState.component1().memory.ioBounds = MainMemory.IOBounds(it, amount) else props.archState.component1().console.error("IO Definition: Address isn't valid!")
            } ?: props.archState.component1().console.error("IO Definition: Address isn't valid!")

        } else {
            props.archState.component1().memory.ioBounds = null
        }
        localStorage.setItem("${Keys.MIO_ACTIVE}-${props.archState.component1().description.name}", useBounds.toString())
        startAddr?.let {
            localStorage.setItem("${Keys.MIO_START}-${props.archState.component1().description.name}", startAddr.getHexStr())
        }
        localStorage.setItem("${Keys.MIO_AMOUNT}-${props.archState.component1().description.name}", amount.toString())
    }

    /*useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().regContainer.pc.variable.get().toHex().getRawHexStr())
        setMemList(props.archState.component1().memory.memList)
    }*/

    useEffect(editVar) {
        if (editVar != null) {
            editRef.current?.focus()
        }
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            props.archState.component1().memory.endianess = it
        }
    }

}


