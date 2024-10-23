package ui.uilib.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import ui.uilib.UIState
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import ui.uilib.params.FontType

@Composable
fun ArchitectureOverview(arch: Architecture?) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val vScrollState = rememberScrollState()

    if (arch != null) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(theme.COLOR_BG_1),
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "Architecture: ${arch.description.name}", fontType = FontType.LARGE)
            }

            Row(
                Modifier.fillMaxSize()
                    .verticalScroll(vScrollState),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                arch.settings.forEach {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CLabel(text = it.name)
                        when (it) {
                            is SetupSetting.Any -> TODO()
                            is SetupSetting.Bool -> TODO()
                            is SetupSetting.Enumeration -> {
                                Selector<Enum<*>>(
                                    it.enumValues, initial = it.state.value as? Enum<*>, itemContent = { isSelected, value ->
                                        CLabel(text = value.name)
                                    },
                                    onSelectionChanged = { newVal ->
                                        it.loadFromString(arch, newVal.name)
                                    }
                                )
                            }
                        }

                    }
                }
            }
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
