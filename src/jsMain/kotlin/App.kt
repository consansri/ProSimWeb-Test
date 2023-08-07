import emotion.css.cx
import emotion.react.css
import kotlinx.browser.localStorage
import web.html.*
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.img
import tools.DebugTools
import views.*
import web.cssom.*


val App = FC<Props> { props ->

    val (appLogic, setAppLogic) = useState<AppLogic>(AppLogic())
    val (mode, setMode) = useState<StyleConst.Mode>(StyleConst.mode)

    localStorage.getItem(StorageKey.ARCH_TYPE)?.let {
        val loaded = it.toInt()
        if (loaded in 0 until appLogic.getArchList().size) {
            appLogic.selID = loaded
        }
    }

    val (reloadUI, setReloadUI) = useState(false)

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    fun updateApp() {
        console.log("update App from Child Component")
        setReloadUI(!reloadUI)
    }

    div {



        Menu {
            this.appLogic = appLogic
            update = useState(reloadUI)
            updateParent = ::updateApp
        }

        ReactHTML.main {
            ref = mainRef
            css {
                backgroundColor = StyleConst.mainBgColor.get()
                color = StyleConst.mainFgColor.get()
            }

            div {
                id = "tcontainer"
                div {
                    id = "lcontainer"
                    CodeEditor {
                        this.appLogic = appLogic
                        update = useState(reloadUI)
                        updateParent = ::updateApp
                    }
                }

                div {
                    id = "rcontainer"
                    ProcessorView {
                        this.appLogic = appLogic
                        update = useState(reloadUI)
                        updateAppLogic = ::updateApp
                    }
                }

                div {
                    id = "controlsContainer"

                    a {
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
                }
            }
            div {
                id = "bcontainer"
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
            FooterView {

            }
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