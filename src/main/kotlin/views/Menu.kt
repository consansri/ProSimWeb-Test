package views

import AppLogic
import StyleConst
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav


external interface MenuProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}

val Menu = FC<MenuProps>() { props ->

    val data by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val updateParent = props.updateParent
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
                    src = StyleConst.Icons.home

                }
            }

            a{
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
    }

    useEffect(update){
        console.log("(update) Menu")
    }

}