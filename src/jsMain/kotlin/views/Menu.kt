package views

import AppLogic
import StyleConst
import csstype.*
import emotion.react.css
import extendable.components.connected.FileHandler
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.files.Blob
import org.w3c.files.File
import org.w3c.files.FileReader
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.nav
import tools.DebugTools


external interface MenuProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}

val Menu = FC<MenuProps>() { props ->

    val data by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val (navHidden, setNavHidden) = useState(true)
    val (archsHidden, setArchsHidden) = useState(true)
    val (importHidden, setImportHidden) = useState(true)

    val navRef = useRef<HTMLElement>()
    val archsRef = useRef<HTMLDivElement>()
    val importRef = useRef<HTMLInputElement>()


    fun showNavbar(state: Boolean) {
        navRef.current?.let {
            if (state) {
                it.classList.add("responsive_nav")
            } else {
                it.classList.remove("responsive_nav")
            }
            setNavHidden(!state)
        }
    }

    fun showArchs(state: Boolean) {
        archsRef.current?.let {
            if (state) {
                it.classList.add("nav-dropdown-open")
            } else {
                it.classList.remove("nav-dropdown-open")
            }
            setArchsHidden(!state)
        }
    }

    fun importFile(file: dynamic) {
        val reader = FileReader()
        reader.readAsText(file as Blob, "UTF-8")

        reader.onloadend = {
            console.log("read ${reader.result}")
            data.getArch().getFileHandler().import(FileHandler.File(file.name as String, reader.result as String))
        }
    }

    header {
        h3 {
            +"ProSimWeb"
        }

        nav {
            ref = navRef
            a {
                href = "#home"
                onClick = {
                    console.log("#home clicked")
                }

                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.home

                }
            }

            a {
                href = "#"
                onClick = {
                    showArchs(true)
                }
                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.processor
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Upload"
                    src = StyleConst.Icons.import

                }

                onClick = {
                    setImportHidden(!importHidden)
                }


            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Download"
                    src = StyleConst.Icons.export

                }
            }

            button {
                className = ClassName("nav-btn nav-close-btn")

                onClick = {
                    showNavbar(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"

                }
            }
        }

        button {

            className = ClassName("nav-btn")

            onClick = {
                showNavbar(true)
            }

            img {
                className = ClassName("nav-img")
                src = "icons/bars.svg"

            }
        }

        div {
            className = ClassName("nav-dropdown")
            ref = archsRef

            for (id in data.getArchList().indices) {
                a {
                    href = "#${data.getArchList()[id].getName()}"

                    onClick = { event ->
                        showArchs(false)
                        val newData = data
                        newData.selID = id
                        localStorage.setItem(StorageKey.ARCH_TYPE, "$id")
                        console.log("Load " + data.getArch().getName())
                        event.currentTarget.classList.toggle("nav-arch-active")
                        //updateParent(newData)
                        document.location?.reload()
                    }

                    +data.getArchList()[id].getName()
                }
            }

            a {
                onClick = {
                    showArchs(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"

                }
            }
        }

        if (!importHidden) {
            div {
                css {
                    position = Position.fixed
                    bottom = 0.px
                    left = 0.px
                    width = 100.vw
                    zIndex = integer(1000)
                    padding = 1.rem
                    backgroundColor = Color("#5767aa")

                    display = Display.flex
                    justifyContent = JustifyContent.center
                    gap = 2.rem
                    alignContent = AlignContent.spaceEvenly
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/cancel.svg"
                    }
                    onClick = {
                        setImportHidden(true)
                    }
                }

                input {
                    ref = importRef
                    type = InputType.file
                    multiple = true
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/upload.svg"
                    }
                    onClick = {
                        val files = importRef.current?.files?.asList() ?: emptyList<File>()

                        if (!files.isEmpty()) {
                            for (file in files) {
                                importFile(file)
                            }
                        }
                        setImportHidden(true)
                    }
                }

            }
        }
    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) Menu")
        }
    }

}