package ui.uilib.interactable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ui.uilib.UIState


@Composable
fun <T> Selector(
    items: Collection<T>,
    onSelectionChanged: (T) -> Unit,
    itemContent: @Composable (isSelected: Boolean, value: T) -> Unit
) {
// Track the selected item, default to the first one
    var selectedItem by remember { mutableStateOf(items.firstOrNull()) }

    // Update the selection when a new item is clicked
    LaunchedEffect(selectedItem) {
        selectedItem?.let(onSelectionChanged)
    }

    Column {
        items.forEach { item ->
            val isSelected = item == selectedItem

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) UIState.Theme.value.COLOR_SELECTION else Color.Transparent)
                    .clickable {
                        selectedItem = item
                    }
                    .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
            ) {
                itemContent(isSelected, item)
            }
        }
    }
}
