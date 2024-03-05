import react.create
import react.dom.client.createRoot
import web.dom.*
import debug.DebugTools
import io.nacular.doodle.core.Internal

@OptIn(Internal::class)
fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val app = App.create()

    document.title = "${Constants.NAME} ${Constants.VERSION}"

    /*application(root = root) {
        DoodleApp(instance(), instance())
    }*/

    createRoot(root).render(app)
}


