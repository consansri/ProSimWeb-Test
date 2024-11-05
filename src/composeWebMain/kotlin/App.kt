import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import config.BuildConfig
import ui.ProSimApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    CanvasBasedWindow(title = "${BuildConfig.NAME} - ${BuildConfig.VERSION}", canvasElementId = "ComposeTarget") {
        ProSimApp.launch()
    }

}