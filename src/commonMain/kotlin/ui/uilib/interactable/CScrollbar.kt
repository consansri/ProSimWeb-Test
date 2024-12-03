package ui.uilib.interactable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import ui.uilib.UIState

@Composable
fun CVerticalScrollBar(scrollState: ScrollState, modifier: Modifier = Modifier, drawScope: DrawScope.() -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()

    val containerHeight = scrollState.maxValue.toFloat() + scrollState.viewportSize.toFloat()
    val vScrollRatio = if (containerHeight == 0f) 0f else scrollState.value.toFloat() / containerHeight

    // Calculate scrollbar thumb height based on the content height and viewport
    val thumbHeightRatio = if (containerHeight == 0f) 1f else scrollState.viewportSize.toFloat() / containerHeight
    val thumbHeightPx = thumbHeightRatio * scrollState.viewportSize.toFloat()

    Canvas(
        modifier.fillMaxHeight().width(UIState.Scale.value.SIZE_CONTROL_MEDIUM)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Calculate new scroll position based on drag amount
                    val newScroll = (scrollState.value + (dragAmount / thumbHeightRatio)).toInt().coerceIn(0, scrollState.maxValue)
                    coroutineScope.launch {
                        scrollState.scrollTo(newScroll)
                    }
                }
            }) {
        // Draw the scrollbar thumb
        drawRect(
            color = UIState.Theme.value.COLOR_FG_1,
            style = Fill,
            topLeft = Offset(x = size.width / 2, y = vScrollRatio * size.height),
            size = size.copy(width = size.width / 2, height = thumbHeightPx)
        )

        drawScope()
    }

}

@Composable
fun CHorizontalScrollBar(scrollState: ScrollState, modifier: Modifier = Modifier, drawScope: DrawScope.() -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()

    val containerWidth = scrollState.maxValue.toFloat() + scrollState.viewportSize.toFloat()
    val vScrollRatio = if (containerWidth == 0f) 0f else scrollState.value.toFloat() / containerWidth

    // Calculate scrollbar thumb height based on the content height and viewport
    val thumbWidthRatio = if (containerWidth == 0f) 1f else scrollState.viewportSize.toFloat() / containerWidth
    val thumbWidthPx = thumbWidthRatio * scrollState.viewportSize.toFloat()

    Canvas(
        modifier.fillMaxWidth().height(UIState.Scale.value.SIZE_CONTROL_MEDIUM)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // Calculate new scroll position based on drag amount
                    val newScroll = (scrollState.value + (dragAmount / thumbWidthRatio)).toInt().coerceIn(0, scrollState.maxValue)
                    coroutineScope.launch {
                        scrollState.scrollTo(newScroll)
                    }
                }
            }) {
        // Draw the scrollbar thumb
        drawRect(
            color = UIState.Theme.value.COLOR_FG_1,
            style = Fill,
            topLeft = Offset(vScrollRatio * size.width, size.height / 2),
            size = size.copy(thumbWidthPx, size.height / 2)
        )

        drawScope()
    }

}
