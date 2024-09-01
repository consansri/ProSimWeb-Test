package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.text.CTextField
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.LightTheme

object ProSimApp {

    @Composable
    fun launch() {
        UIState.Theme.value = ProjectStateManager.appState.getTheme()
        UIState.Scale.value = ProjectStateManager.appState.getScaling()

        UIState.StateUpdater()

        ProSimApp()
    }

    @Composable
    fun ProSimApp() {
        val currentlyOpened = ProjectStateManager.appState.currentlyOpened
        val initialScreen = if (currentlyOpened != null) {
            Screen.ProjectView(ProjectStateManager.appState.projectStates[currentlyOpened])
        } else {
            Screen.ProjectSelection
        }

        var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }

        when (val screen = currentScreen) {
            is Screen.ProjectSelection -> ProjectSelectionScreen(
                onProjectSelected = { selectedProject ->
                    ProjectStateManager.appState = ProjectStateManager.appState.copy(currentlyOpened = ProjectStateManager.projects.indexOf(selectedProject))
                    currentScreen = Screen.ProjectView(selectedProject)
                },
                onCreateNewProject = {
                    currentScreen = Screen.CreateNewProject
                }
            )

            is Screen.ProjectView -> ProjectViewScreen(screen.state) {
                ProjectStateManager.appState = ProjectStateManager.appState.copy(currentlyOpened = null)
                currentScreen = Screen.ProjectSelection
            }

            is Screen.CreateNewProject -> CreateNewProjectScreen(
                onProjectCreated = { newProjectPath ->
                    currentScreen = Screen.ProjectView(newProjectPath)
                },
                onCancel = {
                    currentScreen = Screen.ProjectSelection
                }
            )
        }
    }

    @Composable
    fun ProjectSelectionScreen(onProjectSelected: (ProjectState) -> Unit, onCreateNewProject: () -> Unit) {

        val theme = UIState.Theme.value

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            top = {
                Spacer(Modifier.weight(2.0f))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, icon = theme.icon)
            },
            center = {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Column(
                        modifier = Modifier
                            .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                    ) {
                        CLabel(text = "Select a Project:", modifier = Modifier.widthIn())
                        Spacer(modifier = Modifier.height(8.dp))

                        ProjectStateManager.projects.forEach {
                            CButton(
                                onClick = { onProjectSelected(it) },
                                icon = UIState.Icon.value.chevronRight,
                                text = it.absRootPath
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        CButton(onClick = onCreateNewProject, text = "Create New Project", modifier = Modifier.widthIn())
                    }
                }
            }
        )
    }

    @Composable
    fun CreateNewProjectScreen(onProjectCreated: (ProjectState) -> Unit, onCancel: () -> Unit) {
        var pathField by remember { mutableStateOf("New") }
        var target by remember { mutableStateOf<TargetSpec>(RV32Spec) }

        val path = ProjectStateManager.createPath(pathField)
        var invalidProjectPath = !ProjectStateManager.isPathValid(path)

        nativeLog("$pathField -> $path -> invalid=$invalidProjectPath")

        val theme = UIState.Theme.value
        val scale = UIState.Scale.value

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            top = {
                Spacer(Modifier.weight(2.0f))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, icon = theme.icon)
            },
            center = {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .border(scale.SIZE_BORDER_THICKNESS, theme.COLOR_BORDER, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                        .padding(scale.SIZE_INSET_MEDIUM)
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 250.dp)

                    ) {
                        CLabel(text = "Create New Project", modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER))

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(Modifier) {
                            CLabel(text = "Project Path:", modifier = Modifier.weight(1.0f), textAlign = TextAlign.Left)

                            Spacer(Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                            CTextField(
                                value = pathField,
                                modifier = Modifier.weight(1.0f),
                                onValueChange = {
                                    pathField = it
                                    invalidProjectPath = !ProjectStateManager.isPathValid(path)
                                },
                                singleLine = true,
                                error = invalidProjectPath
                            ) {
                                Box(modifier = Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                                    it()
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Selector(TargetSpec.specs, {
                            target = it
                        }, { isSelected, value ->
                            CLabel(text = value.name)
                        })

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            CButton(
                                onClick = {
                                    if (!invalidProjectPath) {
                                        val state = ProjectState(ProjectStateManager.toAbsRootPath(path), target.name)
                                        ProjectStateManager.projects += state
                                        onProjectCreated(state)
                                    }
                                }, text = "Create",
                                active = !invalidProjectPath,
                                modifier = Modifier.weight(1.0f)
                            )

                            Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                            CButton(onClick = onCancel, text = "Cancel", modifier = Modifier.weight(1.0f))
                        }
                    }
                }
            }
        )
    }


    sealed class Screen {
        object ProjectSelection : Screen()
        object CreateNewProject : Screen()
        data class ProjectView(val state: ProjectState) : Screen()
    }

}