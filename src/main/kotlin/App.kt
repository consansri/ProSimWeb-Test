import kotlinx.browser.localStorage
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import tools.DebugTools
import views.*

val App = FC<Props> { props ->

    val (appLogic, setAppLogic) = useState<AppLogic>(AppLogic())

    localStorage.getItem(StorageKey.ARCH_TYPE)?.let {
        val loaded = it.toInt()
        if (loaded in 0 until appLogic.getArchList().size) {
            appLogic.selID = loaded
        }
    }

    val (reloadUI, setReloadUI) = useState(false)
    val (resetUI, setResetUI) = useState(false)

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    fun updateAppLogic(newData: AppLogic) {
        console.log("update App from Child Component")

        setAppLogic(newData)
        setReloadUI(!reloadUI)
    }

    fun updateApp() {
        setReloadUI(!reloadUI)
    }

    fun resizeNav() {
        navRef.current?.let {
            if (it.className == "navbar") {
                it.className = "responsive"
            } else {
                it.className = "navbar"
            }
        }
    }

    Menu {
        this.appLogic = appLogic
        update = useState(reloadUI)
        updateParent = ::updateAppLogic

    }

    ReactHTML.main {
        ref = mainRef
        div {
            id = "tcontainer"
            div {
                id = "lcontainer"
                CodeEditor {
                    this.appLogic = appLogic
                    update = useState(reloadUI)
                    updateParent = ::updateAppLogic
                }
            }

            div {
                id = "rcontainer"
                ProcessorView {
                    this.appLogic = appLogic
                    update = useState(reloadUI)
                    updateAppLogic = ::updateAppLogic
                }
            }

        }
        div {
            id = "bcontainer"
            InfoView {
                this.appLogic = appLogic
                this.update = useState(reloadUI)
                this.updateParent = ::updateAppLogic
                this.footerRef = footerRef
            }
        }


    }


    footer {
        ref = footerRef
        FooterView {

        }
    }

    useEffect(reloadUI) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) App")
        }
    }

}