package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cengine.lang.RunConfiguration
import cengine.lang.cown.CownLang
import cengine.project.Project
import cengine.project.ProjectState
import ui.uilib.UIState
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.ResizableBorderPanels

@Composable
fun ProjectViewScreen(state: ProjectState, close: () -> Unit) {
    val theme = UIState.Theme.value

    val project = Project(state, CownLang())

    BorderLayout(
        Modifier.fillMaxSize().background(theme.COLOR_BG_0),
        topContent = {
            TopBar(project, onClose = { close() }, project.services.flatMap { it.runConfigurations.filterIsInstance<RunConfiguration.ProjectRun<*>>() })
        },
        centerContent = {
            ResizableBorderPanels(
                Modifier.fillMaxSize(),
                leftContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_1)) {
                        // Left content
                    }
                },
                centerContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_0)) {
                        // Center content
                    }
                },
                rightContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_1)) {
                        // Right content
                    }
                },
                bottomContent = {
                    Box(modifier = Modifier.fillMaxSize().background(UIState.Theme.value.COLOR_BG_1)) {
                        // Bottom content
                    }
                },
            )
        })

}