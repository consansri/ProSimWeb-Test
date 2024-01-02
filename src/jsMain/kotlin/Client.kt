import react.create
import react.dom.client.createRoot
import web.dom.*
import debug.DebugTools

fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val app = App.create()

    document.title = "${Constants.NAME} ${Constants.VERSION}"
    createRoot(root).render(app)
}


