package ui.uilib.emulator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import emulator.kit.Architecture
import ui.uilib.label.CLabel

@Composable
fun ArchitectureOverview(arch: Architecture?) {


    if (arch != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CLabel(text = "${arch.description.name} Selected!")
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CLabel(text = "No Architecture Selected!")
        }
    }


}
