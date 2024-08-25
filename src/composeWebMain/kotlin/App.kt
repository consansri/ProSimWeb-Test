import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import ui.WindowManager.Test

@OptIn(ExperimentalComposeUiApi::class)
fun main(){
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        Test()
    }
}