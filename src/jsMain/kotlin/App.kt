import emotion.react.css
import emulator.Emulator
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
import visual.*
import visual.ProcessorView
import web.cssom.*


val App = FC<Props> { props ->

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    val (appLogic, setAppLogic) = useState<Emulator>(Emulator())
    val (mode, setMode) = useState<StyleAttr.Mode>(StyleAttr.mode)
    val (reloadUI, setReloadUI) = useState(false)
    val (lPercentage, setLPct) = useState<Int>(40)

    localStorage.getItem(StorageKey.ARCH_TYPE)?.let {
        val loaded = it.toInt()
        if (loaded in 0 until appLogic.getArchList().size) {
            appLogic.selID = loaded
        }
    }



    AppStyle {}

    fun updateApp() {
        console.log("update App from Child Component")
        setReloadUI(!reloadUI)
    }

    Menu {
        this.emulator = appLogic
        update = reloadUI
        updateParent = ::updateApp
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
                    if (lPercentage == 0) {
                        visibility = Visibility.hidden
                    }
                    StyleAttr.layoutSwitchMediaQuery {
                        flex = 100.pct
                        display = Display.block
                        minHeight = max(50.vh, (StyleAttr.Main.Editor.TextField.lineHeight * 10).px)
                    }
                }
                CodeEditor {
                    this.emulator = appLogic
                    update = reloadUI
                    updateParent = ::updateApp
                }

            }

            div {
                id = "rcontainer"
                css {
                    flex = (100 - lPercentage).pct
                    if (100 - lPercentage == 0) {
                        visibility = Visibility.hidden
                    }
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = StyleAttr.paddingSize
                    padding = StyleAttr.paddingSize
                    position = Position.relative
                    backgroundColor = StyleAttr.Main.Processor.BgColor.get()
                    color = StyleAttr.Main.Processor.FgColor.get()
                    boxShadow = StyleAttr.Main.elementShadow
                    borderRadius = StyleAttr.borderRadius

                    StyleAttr.layoutSwitchMediaQuery {
                        flex = 100.pct
                    }
                }

                ProcessorView {
                    this.emulator = appLogic
                    update = useState(reloadUI)
                    updateAppLogic = ::updateApp
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

                    a {
                        backgroundColor = StyleAttr.Main.AppControls.BgColor.get()
                        padding = StyleAttr.Main.AppControls.iconPadding
                        borderRadius = StyleAttr.borderRadius
                        boxShadow = BoxShadow(0.px, 0.px, 0.5.rem, Color("#000000A0"))
                        height = StyleAttr.Main.AppControls.size
                        width = StyleAttr.Main.AppControls.size
                        cursor = Cursor.pointer

                        img {
                            height = StyleAttr.Main.AppControls.iconSize
                            width = StyleAttr.Main.AppControls.iconSize
                            filter = StyleAttr.iconFilter
                        }
                    }
                }

                a {
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
                a {
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
                this.emulator = appLogic
                this.update = useState(reloadUI)
                this.updateParent = ::updateApp
                this.footerRef = footerRef
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


    useEffect(reloadUI) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) App")
        }
    }

    useEffect(mode) {

    }

}