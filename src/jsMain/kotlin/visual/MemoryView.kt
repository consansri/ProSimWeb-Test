package visual

import Keys
import Settings
import StyleAttr
import emotion.react.css
import emulator.kit.MicroSetup
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import cengine.util.integer.Hex
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
import visual.memory.CacheView
import visual.memory.MainMemoryView
import web.cssom.*
import web.cssom.Auto.Companion.auto
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
    val (currMem, setCurrMem) = useState<Int?>(localStorage.getItem(Keys.MEM_SELECTED)?.toIntOrNull())

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
            paddingLeft = StyleAttr.scrollBarSize
            paddingRight = StyleAttr.scrollBarSize
            paddingBottom = StyleAttr.paddingSize

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

                flexGrow = number(0.0)

                "input[type=number]" {
                    display = Display.inlineBlock
                    border = Border(0.px, LineStyle.hidden)
                    height = StyleAttr.iconSize + 2 * StyleAttr.iconPadding
                    flexGrow = number(1.0)
                    float = Float.left
                    width = auto
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

                defaultValue = props.archState.component1().memory.endianness.name

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
                width = 100.pct
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                justifyContent = JustifyContent.flexStart
                alignItems = AlignItems.center
                gap = 1.px
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
                        if (i == currMem) textDecoration = TextDecoration.underline

                        hover {
                            textDecoration = TextDecoration.underline
                        }
                    }
                    +mem.name

                    onClick = {
                        setCurrMem(MicroSetup.getMemoryInstances().indexOf(mem))
                    }
                }
            }
        }

        val selected = MicroSetup.getMemoryInstances().getOrNull(currMem ?: -1)
        when (selected) {
            is Cache -> {
                CacheView {
                    this.key = "${selected::class.simpleName}"
                    this.cache = selected
                    this.exeEventState = props.exeEventState
                    this.archState = props.archState
                }
            }

            is MainMemory -> {
                MainMemoryView {
                    this.key = "${selected::class.simpleName}"
                    this.memory = selected
                    this.archState = props.archState
                    this.exeEventState = props.exeEventState
                    this.lowFirst = lowFirst
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
                    defaultValue = editVar.variable.get().toHex().toRawString()

                    onChange = {
                        val hex = Hex(it.currentTarget.value, props.archState.component1().memory.instanceSize)
                        if (hex.valid) {
                            editVar.variable.set(hex)
                        } else {
                            it.currentTarget.value = editVar.variable.get().toHex().toRawString()
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
                        defaultValue = startAddr?.toRawString() ?: ""

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

    useEffect(useBounds, startAddr, amount) {
        if (useBounds) {
            startAddr?.let {
                if (it.valid) props.archState.component1().memory.ioBounds = MainMemory.IOBounds(it, amount) else props.archState.component1().console.error("IO Definition: Address isn't valid!")
            } ?: props.archState.component1().console.error("IO Definition: Address isn't valid!")

        } else {
            props.archState.component1().memory.ioBounds = null
        }
        localStorage.setItem("${Keys.MIO_ACTIVE}-${props.archState.component1().description.name}", useBounds.toString())
        startAddr?.let {
            localStorage.setItem("${Keys.MIO_START}-${props.archState.component1().description.name}", startAddr.toString())
        }
        localStorage.setItem("${Keys.MIO_AMOUNT}-${props.archState.component1().description.name}", amount.toString())
    }

    useEffect(currMem){
        localStorage.setItem(Keys.MEM_SELECTED, currMem.toString())
    }

    useEffect(editVar) {
        if (editVar != null) {
            editRef.current?.focus()
        }
    }

    useEffect(memEndianess) {
        memEndianess?.let {
            props.archState.component1().memory.endianness = it
        }
    }

}


