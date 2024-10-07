package prosim

import Constants
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.ProSimApp
import ui.uilib.UIState

fun main(){
    application {

        val iconPainter = rememberVectorPainter(UIState.Icon.value.appLogo)

        Window(::exitApplication, title = Constants.TITLE, icon = iconPainter){
            ProSimApp.launch()
        }

    }
}