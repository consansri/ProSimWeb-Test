package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.ResizableBorderPanels
import ui.uilib.text.CTextField
import ui.uilib.theme.DarkTheme
import ui.uilib.theme.LightTheme

object ProSimApp {

    @Composable
    fun launch() {
        ProSimApp()
    }

    @Composable
    fun ProSimApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.ProjectSelection) }

        when (val screen = currentScreen) {
            is Screen.ProjectSelection -> ProjectSelectionScreen(
                onProjectSelected = { selectedProjectPath ->
                    currentScreen = Screen.ProjectView(selectedProjectPath)
                },
                onCreateNewProject = {
                    currentScreen = Screen.CreateNewProject
                }
            )

            is Screen.ProjectView -> ProjectViewScreen(screen.state) {
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
            topContent = {
                Spacer(Modifier.weight(2.0f))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, icon = theme.icon)
            },
            centerContent = {
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
                                text = it.name
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
        var projectName by remember { mutableStateOf("NewPoject") }
        var projectPath by remember { mutableStateOf("/NewProject") }
        var target by remember { mutableStateOf<TargetSpec>(RV32Spec) }

        var invalidProjectName by remember { mutableStateOf<Boolean>(false) }
        var invalidProjectPath by remember { mutableStateOf<Boolean>(false) }

        val theme = UIState.Theme.value

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            topContent = {
                Spacer(Modifier.weight(2.0f))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, icon = theme.icon)
            },
            centerContent = {
                Box(modifier = Modifier.align(Alignment.Center).padding(UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                    ) {
                        CLabel(text = "Create New Project", modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                            CLabel(modifier = Modifier.weight(1.0f), text = "Project Name:")

                            Spacer(Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                            CTextField(
                                value = projectName,
                                modifier = Modifier.weight(1.0f),
                                onValueChange = {
                                    projectName = it
                                    invalidProjectName = it.isBlank()
                                },
                                singleLine = true,
                                error = invalidProjectName
                            ) {
                                it()
                            }
                        }

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                            CLabel(text = "Project Path:", modifier = Modifier.weight(1.0f))

                            Spacer(Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                            CTextField(
                                value = projectPath,
                                modifier = Modifier.weight(1.0f),
                                onValueChange = {
                                    projectPath = it
                                    invalidProjectPath = it.isBlank()
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
                            if (isSelected) {
                                CLabel(icon = UIState.Icon.value.chevronRight, text = value.name)
                            } else {
                                CLabel(text = value.name)
                            }
                        })

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            CButton(
                                onClick = {
                                    if (projectName.isNotBlank() && projectPath.isNotBlank()) {
                                        val state = ProjectState(projectName, projectPath, target.name)
                                        ProjectStateManager.projects += state
                                        onProjectCreated(state)
                                    }
                                }, text = "Create",
                                active = !invalidProjectName,
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

    @Composable
    fun ProjectViewScreen(state: ProjectState, onBack: () -> Unit) {
        val theme = UIState.Theme.value

        BorderLayout(Modifier.fillMaxSize().background(theme.COLOR_BG_0),
            topContent = {
                CLabel(text = "Viewing ${state.name}:${state.target}", modifier = Modifier.weight(1.0f))
                Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))
                CButton(onClick = onBack, text = "Close Project", modifier = Modifier.weight(1.0f))
                Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, icon = theme.icon, modifier = Modifier.weight(1.0f))
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

    sealed class Screen {
        object ProjectSelection : Screen()
        object CreateNewProject : Screen()
        data class ProjectView(val state: ProjectState) : Screen()
    }

}