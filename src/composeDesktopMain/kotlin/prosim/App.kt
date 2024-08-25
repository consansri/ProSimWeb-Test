package prosim

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.WindowManager.Test

fun main(){
    application {
        Window(::exitApplication, title = "Test Window"){
            Test()
        }
    }
}