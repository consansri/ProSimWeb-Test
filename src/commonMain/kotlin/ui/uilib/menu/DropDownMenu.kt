package ui.uilib.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.uilib.UIState
import ui.uilib.label.CLabel

@Composable
fun DropDownMenu(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val backgroundColor = UIState.Theme.value.COLOR_BG_OVERLAY

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .clickable { expanded = !expanded }
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Column {
            CLabel(text = selectedItem)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(backgroundColor)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(onClick = {
                        onItemSelected(item)
                        expanded = false
                    }) {
                        CLabel(text = item)
                    }
                }
            }
        }
    }

}