package ui.uilib

import androidx.compose.runtime.*
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



}