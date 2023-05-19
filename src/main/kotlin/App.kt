import csstype.ClassName
import org.w3c.dom.HTMLDivElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import views.CodeEditor
import views.Menu
import views.ProcessorView
import views.ProcessorViewProps

const val CLASS_NAV_IMG = "nav-img"
const val CLASS_NAV_ACTIVE = "active"

val App = FC<Props> { props ->

    var (data, setData) = useState(AppData())

    var (reloadUI, setReloadUI) = useState(false)

    val nav = useRef<HTMLDivElement>()
    val codeEditorRef = useRef("")
    val processorViewRef = useRef<ProcessorViewProps>()

    fun update(newData: AppData) {
        console.log("update App from Child Component")
        setData(newData)
        setReloadUI(!reloadUI)
    }

    fun resizeNav() {
        nav.current?.let {
            if (it.className == "navbar") {
                it.className = "responsive"
            } else {
                it.className = "navbar"
            }
        }
    }

    Menu {
        appData = data
        update = reloadUI
        updateParent = ::update
    }

/*    header {

        div {
            className = ClassName("topnav")
            id = "myTopnav"
            ref = nav

            a {
                href = "#home"
                className = ClassName(CLASS_NAV_ACTIVE)
                img {
                    className = ClassName(CLASS_NAV_IMG)
                    src = "icons/home.svg"
                }
            }

            div {
                className = ClassName("dropdown")

                button {
                    className = ClassName("dropbtn")

                    img {
                        className = ClassName(CLASS_NAV_IMG)
                        alt = "Architecture"
                        src = "icons/cpu.svg"
                    }
                }

                div {
                    className = ClassName("dropdown-content")
                    id = "arch-container"

                    for (id in data.getArchList().indices) {
                        a {
                            href = "#${data.getArchList()[id].name}"
                            onClick = {
                                data.selID = id
                                setData { data }
                                setReloadUI { !reloadUI }
                                console.log("Load " + data.getArch().name)
                            }
                            +data.getArchList()[id].name
                        }
                    }
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName(CLASS_NAV_IMG)
                    alt = "Upload"
                    src = "icons/upload.svg"
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName(CLASS_NAV_IMG)
                    alt = "Download"
                    src = "icons/download.svg"
                }
            }

            a {
                href = "#"
                id = "logo"

                img {
                    className = ClassName(CLASS_NAV_IMG)
                    alt = "ProSim Web"
                    src = "icons/logo.svg"
                }
            }

            a {

            }
        }
    }*/

    /*ReactHTML.main {
        div {
            id = "lcontainer"
            CodeEditor {
                appData = data
                update = reloadUI
                updateParent = ::update
            }
        }

        div {
            id = "rcontainer"
            ProcessorView {
                appData = data
                update = reloadUI
                updateParent = ::update
            }
        }
    }*/

    footer {

    }

    useEffect(reloadUI) {
        console.log("Reload")
    }

}