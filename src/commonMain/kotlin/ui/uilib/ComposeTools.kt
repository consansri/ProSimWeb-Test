package ui.uilib

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ComposeTools {

    fun <T: Any> Collection<T>.sumDp(reducer: (T)-> Dp): Dp{
        var sum = 0.dp
        this.forEach {
            sum += reducer(it)
        }
        return sum
    }

    fun <T: Any> Collection<T>.sumOf(reducer: (T)-> Float): Float{
        var sum = 0f
        this.forEach {
            sum += reducer(it)
        }
        return sum
    }

    @Composable
    fun <T> TrackStateChanges(
        state: T,
        onStateChanged: (oldValue: T, newValue: T) -> Unit
    ) {
        var previousState by remember { mutableStateOf(state) }

        LaunchedEffect(state) {
            if (previousState != state) {
                onStateChanged(previousState, state)
                previousState = state
            }
        }
    }

    suspend fun PointerInputScope.detectHover(onHover: (Offset) -> Unit){
        awaitPointerEventScope {
            while(true){
                val event = awaitPointerEvent()
                val position = event.changes.first().position
                if(event.changes.first().changedToHovered()){
                    onHover(position)
                }
            }
        }
    }

    private fun PointerInputChange.changedToHovered(): Boolean{
        return changedToDown() || changedToUp()
    }



}