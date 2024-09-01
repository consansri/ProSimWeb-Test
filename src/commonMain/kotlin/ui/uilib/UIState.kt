package ui.uilib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import cengine.project.ProjectStateManager
import ui.uilib.resource.BenIcons
import ui.uilib.resource.Icons
import ui.uilib.scale.Scaling
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.Theme

object UIState {

    val Theme = mutableStateOf<Theme>(DarkTheme)
    val Icon = mutableStateOf<Icons>(BenIcons)
    val Scale = mutableStateOf<Scaling>(Scaling())

    @Composable
    fun StateUpdater() {
        ProjectStateManager.appState = ProjectStateManager.appState.copy(scale = Scale.value.scale, theme = Theme.value.name)
    }

}