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
import visual.*
import visual.ProcessorView
import web.cssom.*
import web.dom.document


val App = FC<Props> { props ->

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    val (mode, setMode) = useState<StyleAttr.Mode>(StyleAttr.mode)

    val (lPercentage, setLPct) = useState<Int>(40)
    val (showMenu, setShowMenu) = useState(true)

    val archState = useState(Link.entries[localStorage.getItem(StorageKey.ARCH_TYPE)?.toIntOrNull() ?: 0].architecture)
    val (visibleFeatures, setVisibleFeatures) = useState(archState.component1().getAllFeatures().filter { !it.invisible })


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
                gap = StyleAttr.paddingSize
                flexWrap = FlexWrap.nowrap
                padding = StyleAttr.paddingSize
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.stretch
                alignItems = AlignItems.stretch

                StyleAttr.layoutSwitchMediaQuery {
                    flexDirection = FlexDirection.column
                }
            }

            div {
                id = "lcontainer"
                css {
                    flex = lPercentage.pct
                    position = Position.relative
                    minHeight = max(50.vh, (StyleAttr.Main.Editor.TextField.lineHeight * 50).px)
                    if (lPercentage == 0) {
                        visibility = Visibility.hidden
                    }
                    StyleAttr.layoutSwitchMediaQuery {
                        flex = 100.pct
                        display = Display.block

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
                    padding = StyleAttr.paddingSize
                    position = Position.relative
                    border = Border(3.px, LineStyle.solid, StyleAttr.Main.Processor.BorderColor.get())
                    background = StyleAttr.Main.Processor.BgColor.get()
                    borderRadius = StyleAttr.borderRadius
                    color = StyleAttr.Main.FgColor.get()
                    /*boxShadow = StyleAttr.Main.elementShadow*/


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
                    gap = StyleAttr.paddingSize
                    justifyContent = JustifyContent.start
                    alignItems = AlignItems.start

                    StyleAttr.layoutSwitchMediaQuery {
                        flexDirection = FlexDirection.row
                    }

                    div {
                        height = StyleAttr.Main.AppControls.size
                        width = StyleAttr.Main.AppControls.size
                        backgroundColor = StyleAttr.Main.AppControls.BgColor.get()
                        padding = StyleAttr.Main.AppControls.iconPadding
                        borderRadius = StyleAttr.borderRadius
                        boxShadow = BoxShadow(0.px, 0.px, 0.5.rem, Color("#000000A0"))
                        cursor = Cursor.pointer
                        textAlign = TextAlign.center
                        display = Display.flex
                        justifyContent = JustifyContent.center
                        alignItems = AlignItems.center
                        color = StyleAttr.Main.AppControls.FgColor.get()


                        img {
                            height = StyleAttr.Main.AppControls.iconSize
                            width = StyleAttr.Main.AppControls.iconSize
                            filter = StyleAttr.iconFilter
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
                        StyleAttr.mode = newMode
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

                for (feature in visibleFeatures) {
                    div {
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
                            feature.switch()
                            setVisibleFeatures(archState.component1().getAllFeatures().filter { !it.invisible })
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
                gap = StyleAttr.paddingSize
                flexWrap = FlexWrap.nowrap
                padding = StyleAttr.paddingSize
            }

            input {
                type = InputType.range

                css {
                    accentColor = StyleAttr.Main.AppControls.BgColor.get()
                    StyleAttr.layoutSwitchMediaQuery {
                        visibility = Visibility.hidden
                    }
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

    footer {
        ref = footerRef
        css {
            backgroundColor = StyleAttr.Footer.BgColor.get()
            color = StyleAttr.Footer.FgColor.get()
        }
        FooterView {

        }
    }

    useEffect(mode) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) Theme")
        }
    }
    useEffect(archState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Switch to " + archState.component1().getDescription().fullName)
        }
    }

}