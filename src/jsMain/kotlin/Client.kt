
import debug.DebugTools
import react.create
import react.dom.client.createRoot
import web.dom.document

fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val composeTarget = document.getElementById("ComposeTarget")

    composeTarget?.let {
        document.body.removeChild(it)
    }

    val app = App.create()

    document.title = "${Constants.NAME} ${Constants.VERSION}"

    createRoot(root).render(app)

    // JSTools.setupEventLogging() // for Debugging
}


