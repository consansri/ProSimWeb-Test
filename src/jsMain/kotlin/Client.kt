import react.create
import react.dom.client.createRoot
import web.dom.*
import tools.DebugTools

fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val app = App.create()

    createRoot(root).render(app)
}


