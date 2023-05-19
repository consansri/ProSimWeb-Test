package views

import AppData
import csstype.ClassName
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav
import react.useRef
import react.useState

const val CLASS_NAV_IMG = "nav-img"
const val CLASS_NAV_ACTIVE = "active"

external interface MenuProps : Props {
    var appData: AppData
    var update: Boolean
    var updateParent: (newData: AppData) -> Unit
}

val Menu = FC<MenuProps>(){props ->

    val data by useState(props.appData)
    val (change, setChange) = useState(props.update)

    val navRef = useRef<HTMLElement>()

    fun showNavbar(){
        navRef.current?.let {
            it.classList.toggle("responsive_nav")
        }
    }


    header{
        h3 {
            +"ProSimWeb"
        }

        nav {
            ref = navRef
            a {
                href = "#home"
                className = ClassName(CLASS_NAV_ACTIVE)
                ReactHTML.img {
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
                                props.updateParent(data)
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
                    alt = "Upload"
                    src = "icons/upload.svg"
                }
            }

            a {
                href = "#"
                img {
                    alt = "Download"
                    src = "icons/download.svg"
                }
            }

            button{
                img {
                    src = "icons/times.svg"
                }
            }
        }

        button {
            img {
                src = "icons/bars.svg"
            }
        }

    }




}