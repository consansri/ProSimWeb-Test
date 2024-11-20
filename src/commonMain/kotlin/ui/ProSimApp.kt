package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.project.ProjectState
import cengine.project.ProjectStateManager
import cengine.system.AppTarget.DESKTOP
import cengine.system.AppTarget.WEB
import cengine.system.appTarget
import cengine.system.downloadDesktopApp
import cengine.system.isAbsolutePathValid
import config.BuildConfig
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.launch
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import ui.uilib.layout.BorderLayout
import ui.uilib.params.IconType
import ui.uilib.scale.Scaling
import ui.uilib.text.CTextField
import ui.uilib.theme.Theme

object ProSimApp {

    @Composable
    fun launch() {
        UIState.Theme.value = ProjectStateManager.appState.getTheme()
        UIState.Scale.value = ProjectStateManager.appState.getScaling()

        UIState.StateUpdater()

        UIState.launch {
            ProSimApp()
        }
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
                },
                onShowAbout = {
                    currentScreen = Screen.About
                }
            )

            is Screen.About -> {
                AboutScreen {
                    currentScreen = Screen.ProjectSelection
                }
            }

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
    fun AboutScreen(onCloseAbout: () -> Unit) {

        val theme = UIState.Theme.value
        val scale = UIState.Scale.value

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            top = {
                Spacer(Modifier.weight(2.0f))
                Scaling.Scaler()
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Theme.Switch()
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                CButton(onClick = {
                    onCloseAbout()
                }, icon = UIState.Icon.value.close)
            },
            center = {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CLabel(icon = UIState.Icon.value.reportBug, iconType = IconType.LARGE)
                    CLabel(text = BuildConfig.NAME, textStyle = UIState.BaseLargeStyle.current, color = theme.COLOR_FG_0)
                    CLabel(text = "v${BuildConfig.VERSION}", textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                    if (appTarget() == WEB) {
                        CButton(text = "Download for Desktop", textStyle = UIState.BaseStyle.current, onClick = {
                            // Download Desktop Version from resources "${BuildConfig.FILENAME}.jar"
                            downloadDesktopApp(".jar")
                        })
                    }
                    CLabel(text = "Copyright @ ${BuildConfig.YEAR} ${BuildConfig.ORG}", textStyle = UIState.BaseStyle.current, color = theme.COLOR_FG_0)
                    CLabel(text = "Developed by ${BuildConfig.DEV}", textStyle = UIState.BaseSmallStyle.current, color = theme.COLOR_FG_0)
                }
            }
        )

    }

    @Composable
    fun ProjectSelectionScreen(onProjectSelected: (ProjectState) -> Unit, onCreateNewProject: () -> Unit, onShowAbout: () -> Unit) {

        val theme = UIState.Theme.value
        val scale = UIState.Scale.value

        var currentProjects by remember { mutableStateOf(ProjectStateManager.projects) }

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            top = {
                Spacer(Modifier.weight(2.0f))
                CButton(onClick = {
                    onShowAbout()
                }, icon = UIState.Icon.value.info)
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Scaling.Scaler()
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Theme.Switch()
            },
            center = {
                Box(modifier = Modifier.align(Alignment.Center).width(500.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(UIState.Scale.value.SIZE_INSET_MEDIUM),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CLabel(text = "Select a Project:", modifier = Modifier, textStyle = UIState.BaseStyle.current)
                        Spacer(modifier = Modifier.height(8.dp))

                        currentProjects.forEach {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()

                            Row(
                                Modifier
                                    /*.background(if (isHovered) theme.COLOR_SELECTION else Color.Transparent, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))*/
                                    .fillMaxWidth()
                                    .clickable {
                                        onProjectSelected(it)
                                    }
                                    .hoverable(interactionSource),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(UIState.Icon.value.chevronRight, ">", Modifier.size(scale.SIZE_CONTROL_MEDIUM), tint = theme.COLOR_FG_1)

                                CLabel(
                                    modifier = Modifier.weight(1.0f),
                                    text = it.absRootPath,
                                    textAlign = TextAlign.Left,
                                    horizontalArrangement = Arrangement.Start,
                                    softWrap = false
                                )

                                CLabel(
                                    modifier = Modifier.weight(0.5f),
                                    icon = UIState.Icon.value.processor,
                                    text = it.target,
                                    textAlign = TextAlign.Left,
                                    textStyle = UIState.BaseSmallStyle.current,
                                    horizontalArrangement = Arrangement.Start,
                                    softWrap = false
                                )

                                CButton(
                                    icon = UIState.Icon.value.close,
                                    iconType = IconType.SMALL,
                                    onClick = {
                                        currentProjects -= it
                                    },
                                    iconTint = theme.COLOR_FG_1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        CButton(onClick = onCreateNewProject, text = "Create New Project", modifier = Modifier)
                    }
                }
            }
        )

        LaunchedEffect(currentProjects) {
            ProjectStateManager.projects = currentProjects
        }
    }

    @Composable
    fun CreateNewProjectScreen(onProjectCreated: (ProjectState) -> Unit, onCancel: () -> Unit) {
        val pickerScope = rememberCoroutineScope()
        var pathField by remember { mutableStateOf("new-project") }
        var target by remember { mutableStateOf<TargetSpec<*>>(RV32Spec) }

        var invalidProjectPath by remember { mutableStateOf(false) }

        val theme = UIState.Theme.value
        val scale = UIState.Scale.value

        BorderLayout(
            modifier = Modifier.background(theme.COLOR_BG_1),
            topBg = theme.COLOR_BG_0,
            top = {
                Spacer(Modifier.weight(2.0f))
                Scaling.Scaler()
                Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))
                Theme.Switch()
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
                        CLabel(text = "Create New Project", softWrap = false, modifier = Modifier.fillMaxWidth(), textStyle = UIState.BaseStyle.current)

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_BORDER_THICKNESS).background(theme.COLOR_BORDER))

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(Modifier) {
                            CLabel(text = "Project Path:", softWrap = false, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Left, textStyle = UIState.BaseStyle.current)

                            Spacer(Modifier.width(UIState.Scale.value.SIZE_INSET_MEDIUM))


                            when (appTarget()) {
                                WEB -> {
                                    CTextField(
                                        value = pathField,
                                        modifier = Modifier.weight(1.0f),
                                        onValueChange = {
                                            pathField = it
                                            invalidProjectPath = !isAbsolutePathValid(it)
                                        },
                                        singleLine = true,
                                        error = invalidProjectPath,
                                        textColor = theme.COLOR_FG_0
                                    ) {
                                        Box(modifier = Modifier.padding(horizontal = UIState.Scale.value.SIZE_INSET_MEDIUM)) {
                                            it()
                                        }
                                    }
                                }

                                DESKTOP -> {
                                    CButton(text = pathField, onClick = {
                                        pickerScope.launch {
                                            val path = FileKit.pickDirectory()?.path
                                            if (path != null) {
                                                pathField = path
                                            }
                                        }
                                    })
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Selector(TargetSpec.specs, onSelectionChanged = {
                            target = it
                        }, itemContent = { isSelected, value ->
                            CLabel(text = value.name, textStyle = UIState.BaseStyle.current)
                        })

                        Spacer(modifier = Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            CButton(
                                onClick = {
                                    if (!invalidProjectPath) {
                                        val state = ProjectState(pathField, target.name)
                                        ProjectStateManager.appState = ProjectStateManager.appState.copy(
                                            projectStates = listOf(state) + ProjectStateManager.projects,
                                            currentlyOpened = 0
                                        )
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
        data object ProjectSelection : Screen()
        data object CreateNewProject : Screen()
        data object About : Screen()
        data class ProjectView(val state: ProjectState) : Screen()
    }

}