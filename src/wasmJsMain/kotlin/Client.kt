import kotlinx.browser.document

import org.w3c.dom.HTMLElement

fun main() {
    val root = document.getElementById("root") as HTMLElement
    root.style.width = "100vw"
    root.style.height = "100vh"

}

