import emotion.react.css
import js.errors.TypeError
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
import tools.DebugTools
import views.*
import web.cssom.*


val App = FC<Props> { props ->

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    val (appLogic, setAppLogic) = useState<AppLogic>(AppLogic())
    val (mode, setMode) = useState<StyleConst.Mode>(StyleConst.mode)
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
        this.appLogic = appLogic
        update = useState(reloadUI)
        updateParent = ::updateApp
    }

    ReactHTML.main {
        ref = mainRef
        css {
            backgroundColor = StyleConst.Main.BgColor.get()
            color = StyleConst.Main.FgColor.get()
        }

        div {
            id = "tcontainer"
            css {
                display = Display.flex
                position = Position.relative
                gap = StyleConst.paddingSize
                flexWrap = FlexWrap.nowrap
                padding = StyleConst.paddingSize
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.stretch
                alignItems = AlignItems.stretch

                StyleConst.layoutSwitchMediaQuery {
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
                    StyleConst.layoutSwitchMediaQuery {
                        flex = 100.pct
                        display = Display.block
                        minHeight = max(50.vh, (StyleConst.Main.Editor.TextField.lineHeight * 10).px)
                    }
                }
                CodeEditor {
                    this.appLogic = appLogic
                    update = useState(reloadUI)
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
                    gap = StyleConst.paddingSize
                    padding = StyleConst.paddingSize
                    position = Position.relative
                    backgroundColor = StyleConst.Main.Processor.BgColor.get()
                    color = StyleConst.Main.Processor.FgColor.get()
                    boxShadow = StyleConst.Main.elementShadow
                    borderRadius = StyleConst.borderRadius

                    StyleConst.layoutSwitchMediaQuery {
                        flex = 100.pct
                    }
                }

                ProcessorView {
                    this.appLogic = appLogic
                    update = useState(reloadUI)
                    updateAppLogic = ::updateApp
                }
            }

            div {
                id = "controlsContainer"

                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = StyleConst.paddingSize
                    justifyContent = JustifyContent.start
                    alignItems = AlignItems.start

                    StyleConst.layoutSwitchMediaQuery {
                        flexDirection = FlexDirection.row
                    }

                    a {
                        backgroundColor = StyleConst.Main.AppControls.BgColor.get()
                        padding = StyleConst.Main.AppControls.iconPadding
                        borderRadius = StyleConst.borderRadius
                        boxShadow = BoxShadow(0.px, 0.px, 0.5.rem, Color("#000000A0"))
                        height = StyleConst.Main.AppControls.size
                        width = StyleConst.Main.AppControls.size
                        cursor = Cursor.pointer

                        img {
                            height = StyleConst.Main.AppControls.iconSize
                            width = StyleConst.Main.AppControls.iconSize
                            filter = StyleConst.iconFilter
                        }
                    }
                }

                a {
                    title = "Switch between ${StyleConst.Mode.entries.joinToString(" ") { it.name }}"
                    img {
                        src = when (StyleConst.mode) {
                            StyleConst.Mode.LIGHT -> {
                                StyleConst.Icons.lightmode
                            }

                            StyleConst.Mode.DARK -> {
                                StyleConst.Icons.darkmode
                            }
                        }
                    }
                    onClick = {
                        val newMode = if (mode.ordinal < StyleConst.Mode.entries.size - 1) {
                            StyleConst.Mode.entries[mode.ordinal + 1]
                        } else {
                            StyleConst.Mode.entries[0]
                        }
                        StyleConst.mode = newMode
                        setMode(newMode)
                    }
                }
                a {
                    title = "clear localStorage"
                    img {
                        alt = "clear local storage"
                        src = "icons/eraser.svg"
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
                gap = StyleConst.paddingSize
                flexWrap = FlexWrap.nowrap
                padding = StyleConst.paddingSize
            }

            input {
                type = InputType.range

                css {
                    accentColor = StyleConst.Main.AppControls.BgColor.get()
                    StyleConst.layoutSwitchMediaQuery {
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
                this.appLogic = appLogic
                this.update = useState(reloadUI)
                this.updateParent = ::updateApp
                this.footerRef = footerRef
            }
        }
    }

    footer {
        ref = footerRef
        css {
            backgroundColor = StyleConst.Footer.BgColor.get()
            color = StyleConst.Footer.FgColor.get()
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