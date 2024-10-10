package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import kotlinx.serialization.Serializable
import ui.uilib.UIState


@Composable
fun ProjectViewScreen(state: ProjectState, close: () -> Unit) {

    val project = Project(state, CownLang())

    val viewType = remember { mutableStateOf(ProjectStateManager.appState.viewType) }

    when (viewType.value) {
        ViewType.IDE -> IDEView(project, viewType, close)
        ViewType.EMU -> EmulatorView(project, viewType, close)
    }

    LaunchedEffect(viewType.value) {
        ProjectStateManager.saveState(ProjectStateManager.appState.copy(viewType = viewType.value))
    }
}

@Serializable
enum class ViewType(val icon: ImageVector) {
    IDE(UIState.Icon.value.console),
    EMU(UIState.Icon.value.processor);

    fun next(): ViewType {
        val length = ViewType.entries.size
        val currIndex = ViewType.entries.indexOf(this)
        val nextIndex = (currIndex + 1) % length
        return ViewType.entries[nextIndex]
    }
}

