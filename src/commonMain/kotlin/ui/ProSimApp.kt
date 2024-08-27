package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.uilib.UIState
import ui.uilib.button.CButton
import ui.uilib.layout.BorderLayout
import ui.uilib.layout.ResizableBorderPanels
import ui.uilib.styled.CLabel
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

            is Screen.ProjectView -> ProjectViewScreen(screen.projectPath) {
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
    fun ProjectSelectionScreen(onProjectSelected: (String) -> Unit, onCreateNewProject: () -> Unit) {
        val projects = listOf("/", "/projectA", "/projectB", "/projectC") // Example paths

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
                }, "Theme ${theme.name}")
            },
            centerContent = {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Column(
                        modifier = Modifier
                            .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                    ) {
                        CLabel("Select a Project:", modifier = Modifier.widthIn())
                        Spacer(modifier = Modifier.height(8.dp))

                        projects.forEach { project ->
                            CButton(
                                onClick = {
                                    onProjectSelected(project)
                                },
                                text = project,
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
    fun CreateNewProjectScreen(onProjectCreated: (String) -> Unit, onCancel: () -> Unit) {
        var projectName by remember { mutableStateOf("") }
        var invalidProjectName by remember { mutableStateOf<Boolean>(true) }

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
                }, "Theme ${theme.name}")
            },
            centerContent = {
                Box(modifier = Modifier.align(Alignment.Center).padding(UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                    ) {
                        CLabel("Create New Project", modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        CTextField(
                            value = projectName,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = {
                                projectName = it
                                invalidProjectName = it.isBlank()
                            },
                            singleLine = true,
                            error = invalidProjectName
                        ) {

                            Box(modifier = Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                                it()
                            }
                        }

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))


                        Row(modifier = Modifier.fillMaxWidth()) {
                            CButton(
                                onClick = {
                                    if (projectName.isNotBlank()) {
                                        onProjectCreated(projectName)
                                    }
                                }, "Create",
                                active = !invalidProjectName,
                                modifier = Modifier.weight(1.0f)
                            )

                            Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))

                            CButton(onClick = onCancel, "Cancel", modifier = Modifier.weight(1.0f))
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun ProjectViewScreen(projectId: String, onBack: () -> Unit) {
        val theme = UIState.Theme.value

        BorderLayout(Modifier.fillMaxSize().background(theme.COLOR_BG_0),
            topContent = {
                CLabel("Viewing $projectId")
                Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))
                CButton(onClick = onBack, "Close Project")
                Spacer(modifier = Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))
                CButton(onClick = {
                    if (theme == LightTheme) {
                        UIState.Theme.value = DarkTheme
                    } else {
                        UIState.Theme.value = LightTheme
                    }
                }, "Theme ${theme.name}")
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
        data class ProjectView(val projectPath: String) : Screen()
    }

}