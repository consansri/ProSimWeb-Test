
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import ui.ProSimApp

@OptIn(ExperimentalComposeUiApi::class)
fun main(){
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        ProSimApp.launch()
    }


}