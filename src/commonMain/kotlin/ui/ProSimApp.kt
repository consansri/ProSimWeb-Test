package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.uilib.resource.BenIcons
import ui.uilib.resource.Icons
import ui.uilib.theme.LightTheme
import ui.uilib.theme.Theme

object ProSimApp {

    @Composable
    fun launch() {
        val theme: Theme by remember { mutableStateOf<Theme>(LightTheme) }
        val icons: Icons by remember { mutableStateOf<Icons>(BenIcons) }

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

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Select a Project:")
            Spacer(modifier = Modifier.height(8.dp))

            projects.forEach { project ->
                Text(
                    text = project,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            onProjectSelected(project)
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onCreateNewProject) {
                Text("Create New Project")
            }
        }
    }

    @Composable
    fun CreateNewProjectScreen(onProjectCreated: (String) -> Unit, onCancel: () -> Unit) {
        var projectPath by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create New Project")
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = projectPath,
                onValueChange = {
                    projectPath = it
                    errorMessage = if (it.isBlank()) "Path cannot be empty" else null
                },
                label = { Text("Enter project path") },
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colors.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    if (projectPath.isNotBlank()) {
                        onProjectCreated(projectPath)
                    } else {
                        errorMessage = "Path cannot be empty"
                    }
                }) {
                    Text("Create")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }

    @Composable
    fun ProjectViewScreen(projectId: String, onBack: () -> Unit) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Viewing $projectId")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }

    @Composable
    fun Test() {
        var clickCount by remember { mutableStateOf(0) }

        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { clickCount++ },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Click: $clickCount")
            }
        }
    }

    sealed class Screen {
        object ProjectSelection : Screen()
        object CreateNewProject : Screen()
        data class ProjectView(val projectPath: String) : Screen()
    }

}