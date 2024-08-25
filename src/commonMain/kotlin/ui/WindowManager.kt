package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

object WindowManager {

    fun launch(){

    }

    @Composable
    fun Test(){
        var clickCount by remember { mutableStateOf(0) }

        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = {clickCount++},
                modifier = Modifier.align(Alignment.Center)
            ){
                Text("Click: $clickCount")
            }
        }
    }


}