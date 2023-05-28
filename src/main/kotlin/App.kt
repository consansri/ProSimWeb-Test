import kotlinx.browser.localStorage
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import views.*

const val CLASS_NAV_IMG = "nav-img"
const val CLASS_NAV_ACTIVE = "active"

val App = FC<Props> { props ->

    var (data, setData) = useState(AppLogic())

    localStorage.getItem(StorageKey.ARCH_TYPE)?.let{
        val loaded = it.toInt()
        if(loaded in 0 until data.getArchList().size){
            data.selID = loaded
        }
    }

    var (reloadUI, setReloadUI) = useState(false)

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()
    val codeEditorRef = useRef("")
    val processorViewRef = useRef<ProcessorViewProps>()

    fun update(newData: AppLogic) {
        console.log("update App from Child Component")
        setData(newData)
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
        appLogic = data
        update = useState(reloadUI)
        updateParent = ::update
        this.mainRef = mainRef
        this.footerRef = footerRef

    }

    ReactHTML.main {
        ref = mainRef
        div {
            id = "lcontainer"
            CodeEditor {
                appLogic = data
                update = useState(reloadUI)
                updateParent = ::update
            }
        }

        div {
            id = "rcontainer"
            ProcessorView {
                appLogic = data
                update = useState(reloadUI)
                updateParent = ::update
            }
        }
    }

    footer {
        ref = footerRef
    }

    useEffect(reloadUI) {
        console.log("(update) App")
    }

}