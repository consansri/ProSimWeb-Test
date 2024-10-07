
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import ui.ProSimApp

@OptIn(ExperimentalComposeUiApi::class)
fun main(){


    CanvasBasedWindow(title = Constants.TITLE,canvasElementId = "ComposeTarget") {
        ProSimApp.launch()
    }

}