import react.create
import react.dom.client.createRoot
import web.dom.*
import tools.DebugTools
import web.cssom.StyleSheet

fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val app = App.create()

    document.title = "${Constants.name} ${Constants.version}"
    createRoot(root).render(app)
}


