import extendable.components.connected.FileHandler
import kotlinx.browser.document
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

    val fileCount = localStorage.getItem(StorageKey.FILE_COUNT)?.toIntOrNull() ?: 0
    val files = mutableListOf<FileHandler.File>()
    if (fileCount != 0) {
        for (index in 0 until fileCount) {
            val filename = localStorage.getItem(StorageKey.FILE_NAME + index)
            val filecontent = localStorage.getItem(StorageKey.FILE_CONTENT + index)
            if (filename != null && filecontent != null) {
                files.add(FileHandler.File(filename, filecontent))
            }
        }
    }
    appLogic.getArch().getFileHandler().initFiles(files)


    val (reloadUI, setReloadUI) = useState(false)

    val navRef = useRef<HTMLDivElement>()
    val mainRef = useRef<HTMLElement>()
    val footerRef = useRef<HTMLElement>()

    fun updateAppLogic(newData: AppLogic) {
        console.log("update App from Child Component")
        setAppLogic(newData)
        setReloadUI(!reloadUI)
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