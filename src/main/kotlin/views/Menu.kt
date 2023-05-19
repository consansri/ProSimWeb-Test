package views

import AppData
import csstype.ClassName
import csstype.px
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav

const val CLASS_NAV_IMG = "nav-img"
const val CLASS_NAV_ACTIVE = "active"

external interface MenuProps : Props {
    var appData: AppData
    var update: Boolean
    var updateParent: (newData: AppData) -> Unit
    var mainRef: MutableRefObject<HTMLElement>
    var footerRef: MutableRefObject<HTMLElement>
}

val Menu = FC<MenuProps>() { props ->

    val data by useState(props.appData)
    val (change, setChange) = useState(props.update)
    val (navHidden, setNavHidden) = useState(true)
    val (archsHidden, setArchsHidden) = useState(true)

    val navRef = useRef<HTMLElement>()
    val archsRef = useRef<HTMLDivElement>()


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
                    src = "icons/home.svg"

                }
            }

            a{
                onClick = {
                    showArchs(true)
                }
                img {
                    className = ClassName("nav-img")
                    src = "icons/cpu.svg"
                }
            }


            /*div {

                button {

                    img {
                        alt = "Architecture"
                        src = "icons/cpu.svg"
                        width = 24.0
                        height = 24.0
                    }
                }

                div {
                    id = "arch-container"

                    for (id in data.getArchList().indices) {
                        a {
                            href = "#${data.getArchList()[id].name}"
                            onClick = {
                                data.selID = id
                                props.updateParent(data)
                                console.log("Load " + data.getArch().name)
                            }
                            +data.getArchList()[id].name
                        }
                    }
                }
            }*/

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Upload"
                    src = "icons/upload.svg"

                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Download"
                    src = "icons/download.svg"

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
                    href = "#${data.getArchList()[id].name}"

                    onClick = { event ->
                        showArchs(false)
                        data.selID = id
                        props.updateParent(data)
                        console.log("Load " + data.getArch().name)
                        event.currentTarget?.let {
                            it.classList.toggle("nav-arch-active")
                        }
                    }

                    +data.getArchList()[id].name
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

    }


}