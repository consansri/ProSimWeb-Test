import emotion.react.css
import kotlinx.browser.localStorage
import kotlinx.browser.window
import web.html.*
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import debug.DebugTools
import emulator.Link
import emulator.archs.riscv64.RV64
import emulator.kit.Settings
import emulator.kit.optional.ArchSetting
import emulator.kit.types.Variable
import visual.*
import visual.ProcessorView
import web.cssom.*


val App = FC<Props> {

    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    val (mode, setMode) = useState(StyleAttr.Mode.entries.getOrNull(localStorage.getItem(StorageKey.THEME)?.toIntOrNull() ?: -1) ?: StyleAttr.mode)

    val (lPercentage, setLPct) = useState(40)
    val (showMenu, setShowMenu) = useState(true)

    val archState = useState(Link.entries[localStorage.getItem(StorageKey.ARCH_TYPE)?.toIntOrNull() ?: 0].architecture)
    val (visibleFeatures, setVisibleFeatures) = useState(archState.component1().getAllFeatures().filter { !it.invisible })
    val (showSettings, setShowSettings) = useState(false)

    val compileEventState = useState(false)
    val exeEventState = useState(false)
    val fileChangeEvent = useState(false)


    AppStyle {}

    if (DebugTools.REACT_showUpdateInfo) {
        console.log("REACT: render App")
    }

    if (showMenu) {
        Menu {
            this.archState = archState
            this.fileChangeEvent = fileChangeEvent
        }
    }

    ReactHTML.main {
        ref = mainRef
        css {
            backgroundColor = StyleAttr.Main.BgColor.get()
            color = StyleAttr.Main.FgColor.get()
        }

        div {
            id = "tcontainer"
            css {
                display = Display.flex
                position = Position.relative
                height = StyleAttr.Main.TContainerSize
                //gap = StyleAttr.paddingSize
                flexWrap = FlexWrap.nowrap
                // padding = StyleAttr.paddingSize
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.stretch
                alignItems = AlignItems.stretch
                borderTop = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                StyleAttr.layoutSwitchMediaQuery {
                    flexDirection = FlexDirection.column
                    height = 2 * StyleAttr.Main.TContainerSize
                }
            }

            div {
                id = "lcontainer"
                css {
                    flex = lPercentage.pct
                    position = Position.relative
                    if (lPercentage == 0) {
                        visibility = Visibility.hidden
                    }
                    StyleAttr.layoutSwitchMediaQuery {
                        flex = 100.pct
                        display = Display.block
                        height = StyleAttr.Main.TContainerSize / 2
                    }
                }
                if (lPercentage != 0) {
                    CodeEditor {
                        this.archState = archState
                        this.compileEventState = compileEventState
                        this.exeEventState = exeEventState
                        this.fileChangeEvent = fileChangeEvent
                    }
                }
            }

            div {
                id = "rcontainer"
                css {
                    flex = (100 - lPercentage).pct
                    if (lPercentage == 100) {
                        flexGrow = number(0.0)
                        visibility = Visibility.hidden
                    }
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = StyleAttr.paddingSize
                    //padding = StyleAttr.paddingSize
                    position = Position.relative
                    minHeight = 0.px
                    background = StyleAttr.Main.Processor.BgColor.get()

                    StyleAttr.layoutSwitchMediaQuery {
                        flex = 100.pct
                    }
                }
                if (lPercentage != 100) {
                    ProcessorView {
                        this.archState = archState
                        this.compileEventState = compileEventState
                        this.exeEventState = exeEventState
                    }
                }
            }


            div {
                id = "controlsContainer"

                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    justifyContent = JustifyContent.start
                    alignItems = AlignItems.start
                    borderLeft = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                    flexWrap = FlexWrap.wrap

                    StyleAttr.layoutSwitchMediaQuery {
                        flexDirection = FlexDirection.row
                        borderLeft = Border(0.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                        borderTop = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                    }

                    div {
                        height = StyleAttr.Main.AppControls.size
                        minWidth = StyleAttr.Main.AppControls.size
                        backgroundColor = StyleAttr.Main.AppControls.BgColor.get()
                        padding = StyleAttr.Main.AppControls.iconPadding
                        cursor = Cursor.pointer
                        textAlign = TextAlign.center
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignItems = AlignItems.center
                        color = StyleAttr.Main.AppControls.FgColor.get()

                        borderBottom = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())

                        StyleAttr.layoutSwitchMediaQuery {
                            borderBottom = Border(0.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                            borderRight = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
                        }

                        img {
                            height = StyleAttr.Main.AppControls.iconSize
                            filter = StyleAttr.Main.AppControls.iconFilter.get()
                        }
                    }
                }

                div {
                    title = "Hide/Show Menu"
                    img {
                        src = StyleAttr.Icons.bars
                    }
                    onClick = {
                        setShowMenu(!showMenu)
                    }
                }

                div {
                    title = "Switch between ${StyleAttr.Mode.entries.joinToString(" ") { it.name }}"
                    img {
                        src = when (StyleAttr.mode) {
                            StyleAttr.Mode.LIGHT -> {
                                StyleAttr.Icons.lightmode
                            }

                            StyleAttr.Mode.DARK -> {
                                StyleAttr.Icons.darkmode
                            }
                        }
                    }
                    onClick = {
                        val newMode = if (mode.ordinal < StyleAttr.Mode.entries.size - 1) {
                            StyleAttr.Mode.entries[mode.ordinal + 1]
                        } else {
                            StyleAttr.Mode.entries[0]
                        }
                        localStorage.setItem(StorageKey.THEME, newMode.ordinal.toString())
                        setMode(newMode)
                    }
                }
                div {
                    title = "clear localStorage"
                    img {
                        alt = "clear local storage"
                        src = StyleAttr.Icons.clear_storage
                    }
                    onClick = {
                        val response = window.confirm("Do you really want to delete your entire local storage?\nThis will result in the loss of all settings as well as assembler files!")
                        if (response) {
                            localStorage.clear()
                            console.log("localStorage cleared!")
                        }
                    }
                }

                div {
                    title = "architecture settings"
                    img {
                        alt = "architecture settings"
                        src = StyleAttr.Icons.settings
                    }
                    onClick = {
                        setShowSettings(!showSettings)
                    }
                }

                for (feature in visibleFeatures) {
                    div {
                        title = "${archState.component1().getDescription().name}-${StorageKey.ARCH_FEATURE}-${feature.id}-${feature.name}"
                        css {
                            if (!feature.isActive()) {
                                backgroundColor = important(StyleAttr.Main.AppControls.BgColorDeActivated.get())
                            }
                        }
                        a {
                            css {
                                if (!feature.isActive()) {
                                    color = important(StyleAttr.Main.AppControls.FgColorDeActivated.get())
                                }
                            }
                            +feature.name
                        }
                        onClick = {
                            if (feature.isActive()) {
                                for (featToUpdate in archState.component1().getAllFeatures()) {
                                    if (featToUpdate.enableIDs.contains(feature.id)) {
                                        featToUpdate.deactivate()
                                    }
                                }
                            } else {
                                for (id in feature.enableIDs) {
                                    archState.component1().getAllFeatures().firstOrNull { it.id == id }?.activate()
                                }
                            }
                            feature.switch()
                            localStorage.setItem("${archState.component1().getDescription().name}-${StorageKey.ARCH_FEATURE}-${feature.id}", feature.isActive().toString())
                            setVisibleFeatures(archState.component1().getAllFeatures().filter { !it.invisible })
                            archState.component1().exeReset()
                        }
                    }
                }
            }
        }
        div {
            id = "bcontainer"
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                position = Position.relative
                //gap = StyleAttr.paddingSize
                flexWrap = FlexWrap.nowrap
            }

            input {
                type = InputType.range

                css {
                    accentColor = StyleAttr.Main.AppControls.BgColor.get()
                    StyleAttr.layoutSwitchMediaQuery {
                        visibility = Visibility.hidden
                    }
                    margin = StyleAttr.paddingSize
                }

                min = 0.0
                max = 100.0
                defaultValue = 40.0
                step = 10.0

                onChange = { event ->
                    setLPct(event.currentTarget.valueAsNumber.toInt())
                }
            }

            InfoView {
                this.archState = archState
                this.footerRef = footerRef
                this.compileEventState = compileEventState
                this.exeEventState = exeEventState
                this.fileChangeEvent = fileChangeEvent
            }
        }
    }
    if (showSettings) {
        div {
            className = ClassName(StyleAttr.Header.CLASS_OVERLAY)

            img {
                src = StyleAttr.Icons.cancel
                onClick = {
                    setShowSettings(false)
                }
            }

            for (setting in archState.component1().getAllSettings()) {
                div {
                    className = ClassName(StyleAttr.Header.CLASS_OVERLAY_LABELEDINPUT)
                    ReactHTML.label {
                        htmlFor = "setting${setting.name}"
                        +setting.name
                    }
                    input {
                        id = "setting${setting.name}"
                        when (setting) {
                            is ArchSetting.BoolSetting -> {
                                TODO()
                            }

                            is ArchSetting.ImmSetting -> {
                                type = InputType.text
                                pattern = "[0-9a-fA-F]+"
                                placeholder = Settings.PRESTRING_HEX
                                defaultValue = setting.value.get().toHex().getRawHexStr()

                                onChange = {
                                    val hex = Variable.Value.Hex(it.currentTarget.value, RV64.XLEN)
                                    if (hex.checkResult.valid) {
                                        setting.value.set(hex)
                                        localStorage.setItem("${archState.component1().getDescription().name}-${StorageKey.ARCH_SETTING}-${setting.name}", setting.value.get().toHex().getHexStr())
                                    } else {
                                        it.currentTarget.value = setting.value.get().toHex().getRawHexStr()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    footer {
        ref = footerRef
        css {
            backgroundColor = StyleAttr.Footer.BgColor.get()
            color = StyleAttr.Footer.FgColor.get()
            borderTop = Border(1.px, LineStyle.solid, StyleAttr.Main.LineColor.get())
        }
        FooterView {

        }
    }

    useEffect(mode) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) Theme")
        }
        StyleAttr.mode = mode
        compileEventState.component2().invoke(!compileEventState.component1())
    }
    useEffect(archState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Switch to " + archState.component1().getDescription().fullName)
        }
        for (feature in archState.component1().getAllFeatures()) {
            localStorage.getItem("${archState.component1().getDescription().name}-${StorageKey.ARCH_FEATURE}-${feature.id}")?.toBooleanStrictOrNull()?.let {
                if(it) feature.activate() else feature.deactivate()
            }
        }
        setVisibleFeatures(archState.component1().getAllFeatures().filter { !it.invisible })
        for (setting in archState.component1().getAllSettings()) {
            when (setting) {
                is ArchSetting.BoolSetting -> {
                    TODO()
                }

                is ArchSetting.ImmSetting -> {
                    localStorage.getItem("${archState.component1().getDescription().name}-${StorageKey.ARCH_SETTING}-${setting.name}")?.let {
                        val value = Variable.Value.Hex(it, RV64.XLEN)
                        if (value.checkResult.valid) {
                            setting.value.set(value)
                        }
                    }
                }
            }
        }
    }

}