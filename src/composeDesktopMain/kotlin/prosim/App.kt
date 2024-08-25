package prosim

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.ProSimApp

fun main(){
    application {

        Window(::exitApplication, title = "ProSim"){
            ProSimApp.launch()
        }
    }
}